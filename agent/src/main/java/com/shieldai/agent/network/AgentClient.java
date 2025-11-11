package com.shieldai.agent.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shieldai.shared.DetectionReport;
import com.shieldai.shared.offline.OfflineLogger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AgentClient - integrated with OfflineLogger and a sendJsonLine(...) that returns boolean.
 *
 * - Uses DetectionReport.toJsonLine() where possible (JSON-safe, single-line).
 * - Uses shared OfflineLogger for deduped offline storage and safeResend.
 * - sendJsonLine(String) returns true on 2xx responses (used by OfflineLogger.safeResend).
 */
public class AgentClient {

    private static final String BACKEND_URL = System.getenv().getOrDefault(
            "SHIELDAI_BACKEND_URL",
            System.getProperty("shieldai.backend.url", "http://localhost:8080/api/agent/detections")
    );

    private static final Path OFFLINE_PATH = Paths.get("offline_detections.jsonl");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // OfflineLogger instance (best-effort). If construction fails, fall back to file-based writes.
    private static final OfflineLogger offlineLogger;
    static {
        OfflineLogger ol = null;
        try {
            ol = new OfflineLogger(OFFLINE_PATH, 1024);
        } catch (Throwable t) {
            System.err.println("[ShieldAI] Failed to init OfflineLogger: " + t.getMessage());
        }
        offlineLogger = ol;
    }


    /**
     * Sends a DetectionReport to the backend or stores locally if backend is unavailable.
     */
    public static void sendDetection(DetectionReport report) {
        if (!isBackendAvailable()) {
            System.err.println("[ShieldAI] Backend unavailable, saving detection locally...");
            saveDetectionLocally(report);
            return;
        }

        // First, try to resend any previously saved detections (use OfflineLogger.safeResend if available)
        try {
            if (offlineLogger != null) {
                offlineLogger.safeResend(line -> sendJsonLine(line));
            } else {
                // fallback: old file-based resend implementation
                resendOfflineDetectionsLegacy();
            }
        } catch (Throwable t) {
            String msg = t.getMessage();
            if (msg != null && !msg.trim().isEmpty()) {
                System.err.println("[ShieldAI] Resend attempt failed: " + msg);
            }
        }

        // Send current detection (with retry)
        sendToBackendWithRetry(report);
    }

    /**
     * Send a single-line JSON to the backend once. Returns true on success (2xx).
     * Lightweight method intended for OfflineLogger.safeResend use.
     */
    public static boolean sendJsonLine(String jsonLine) {
        if (jsonLine == null || jsonLine.isBlank()) return false;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BACKEND_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            byte[] data = jsonLine.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", Integer.toString(data.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(data);
            }

            int rc = conn.getResponseCode();
            return rc >= 200 && rc < 300;
        } catch (Throwable t) {
            // caller (OfflineLogger) will keep the line if false
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static void sendToBackendWithRetry(DetectionReport report) {
        int maxRetries = 5; // Increased retries
        long backoff = 500; // Start with 500ms

        String json;
        try {
            // prefer report.toJsonLine() (already sanitized and single-line)
            json = report.toJsonLine();
        } catch (Throwable t) {
            // fallback to ObjectMapper
            try {
                json = objectMapper.writeValueAsString(report);
            } catch (Exception ex) {
                System.err.println("[ShieldAI] Failed to serialize report: " + ex.getMessage());
                saveDetectionLocally(report);
                return;
            }
        }

        // Prepare truncated preview for logs to avoid huge output
        String preview = json.length() > 1000 ? json.substring(0, 1000) + "...(truncated)" : json;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpURLConnection conn = null;
            try {
                System.out.println("[ShieldAI] Sending JSON (attempt " + attempt + "): " + preview);

                conn = (HttpURLConnection) new URL(BACKEND_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5_000); // Faster timeout
                conn.setReadTimeout(10_000);

                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                    os.flush();
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    System.out.println("[ShieldAI] Detection sent successfully (Attempt " + attempt + ")");
                    return;
                } else {
                    String respBody = "";
                    try (InputStream err = conn.getErrorStream()) {
                        if (err != null) respBody = new String(err.readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Throwable ignored) {}
                    System.err.println("[ShieldAI] Attempt " + attempt + " failed: HTTP " + code +
                            (respBody.isEmpty() ? "" : " - " + (respBody.length() > 500 ? respBody.substring(0, 500) + "...(truncated)" : respBody)));
                    // fall through to retry/backoff
                    if (attempt < maxRetries) {
                        try { 
                            // Exponential backoff with jitter
                            long jitter = (long) (Math.random() * 200);
                            Thread.sleep(backoff + jitter); 
                            backoff = Math.min(backoff * 2, 8000); // Cap at 8 seconds
                        } catch (InterruptedException ignored) {}
                    } else {
                        System.err.println("[ShieldAI] All retries failed. Storing locally...");
                        saveDetectionLocally(report);
                    }
                }

            } catch (Exception e) {
                System.err.println("[ShieldAI] Attempt " + attempt + " error: " + e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(backoff); backoff *= 2; } catch (InterruptedException ignored) {}
                } else {
                    System.err.println("[ShieldAI] All retries failed. Storing locally...");
                    saveDetectionLocally(report);
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
    }

    /**
     * Saves detection locally using OfflineLogger when available (deduped),
     * otherwise falls back to appending a JSON line to a local file.
     */
    private static void saveDetectionLocally(DetectionReport report) {
        // Ensure report has sanitized fields if possible
        try {
            report.sanitizeFields();
        } catch (Throwable ignored) {}

        if (offlineLogger != null) {
            try {
                boolean written = offlineLogger.appendIfNew(report);
                if (written) {
                    System.out.println("[ShieldAI] Detection saved to offline logger.");
                } else {
                    System.out.println("[ShieldAI] Detection deduplicated; not saved.");
                }
                return;
            } catch (IOException e) {
                System.err.println("[ShieldAI] OfflineLogger append failed: " + e.getMessage());
                // fall through to legacy file write
            }
        }

        // Legacy fallback: avoid appending duplicate JSON lines
        try {
            String json;
            try {
                json = report.toJsonLine();
            } catch (Throwable t) {
                json = objectMapper.writeValueAsString(report);
            }

            Files.createDirectories(OFFLINE_PATH.getParent() == null ? Paths.get(".") : OFFLINE_PATH.getParent());

            boolean shouldWrite = true;
            if (Files.exists(OFFLINE_PATH)) {
                try (BufferedReader br = Files.newBufferedReader(OFFLINE_PATH, StandardCharsets.UTF_8)) {
                    String last = null, line;
                    while ((line = br.readLine()) != null) {
                        if (!line.isBlank()) last = line;
                    }
                    if (json.equals(last)) shouldWrite = false;
                } catch (IOException ignored) {}
            }

            if (shouldWrite) {
                try (BufferedWriter bw = Files.newBufferedWriter(OFFLINE_PATH, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    bw.write(json);
                    bw.newLine();
                }
                System.out.println("[ShieldAI] Detection saved locally for later sync.");
            } else {
                System.out.println("[ShieldAI] Duplicate detection not written to offline file.");
            }
        } catch (IOException e) {
            System.err.println("[ShieldAI] Failed to save detection locally: " + e.getMessage());
        }
    }

    // quick heuristic to avoid attempting to parse clearly non-json lines
    private static boolean isPlausibleJsonLine(String line) {
        if (line == null) return false;
        String t = line.trim();
        if (!t.startsWith("{") || !t.endsWith("}")) return false;
        return t.contains("\"candidateId\"") || t.contains("\"toolName\"") || t.contains("\"processDetails\"");
    }

    /**
     * Legacy resend implementation (used only if OfflineLogger isn't available).
     * Reads offline file lines, attempts to resend, and rewrites file with remaining failures.
     * Skips obviously-broken/non-JSON lines to avoid repeated Jackson parse errors.
     */
    private static void resendOfflineDetectionsLegacy() {
        File offlineFile = OFFLINE_PATH.toFile();
        if (!offlineFile.exists()) return;

        List<String> allLines;
        try {
            allLines = Files.readAllLines(offlineFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[ShieldAI] Failed to read offline detections: " + e.getMessage());
            return;
        }

        if (allLines.isEmpty()) return;

        List<String> keep = new ArrayList<>();
        for (String raw : allLines) {
            if (raw == null) continue;
            String line = raw.trim();
            if (!isPlausibleJsonLine(line)) {
                // skip legacy/corrupt lines (log a short preview)
                System.err.println("[ShieldAI] Skipping invalid offline line (not JSON): " +
                        (line.length() > 120 ? line.substring(0, 120) + "..." : line));
                continue;
            }

            boolean sent = false;
            try {
                // try fast path: send raw JSON line
                sent = sendJsonLine(line);
                if (!sent) {
                    // fallback: parse and use existing retry flow which has retries/backoff
                    DetectionReport report = objectMapper.readValue(line, DetectionReport.class);
                    sendToBackendWithRetry(report);
                    sent = true;
                }
            } catch (Throwable t) {
                sent = false;
                System.err.println("[ShieldAI] Resend line failed (kept for retry): " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
            }

            if (!sent) keep.add(line);
        }

        // atomically replace offline file with remaining failures (or delete)
        try {
            Path path = offlineFile.toPath();
            if (keep.isEmpty()) {
                Files.deleteIfExists(path);
            } else {
                Path tmp = Files.createTempFile(path.getParent(), "offline-clean-", ".tmp");
                Files.write(tmp, keep, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException e) {
            System.err.println("[ShieldAI] Failed to rewrite offline file after resend: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private static boolean brContainsAll(File file, List<String> toRemove) throws IOException {
        List<String> remaining = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!toRemove.contains(line)) remaining.add(line);
            }
        }
        return remaining.size() > 0;
    }

    private static boolean isBackendAvailable() {
        try {
            String healthUrl = BACKEND_URL.replace("/api/agent/detections", "/api/health");
            HttpURLConnection conn = (HttpURLConnection) new URL(healthUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int code = conn.getResponseCode();
            conn.disconnect();
            return code >= 200 && code < 300;

        } catch (Exception e) {
            return false;
        }
    }
}
