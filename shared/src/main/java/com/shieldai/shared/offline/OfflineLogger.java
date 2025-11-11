package com.shieldai.shared.offline;

import com.shieldai.shared.DetectionReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/**
 * OfflineLogger:
 * - appends single-line JSON (from DetectionReport.toJsonLine)
 * - in-memory LRU dedupe to avoid repeated writes
 * - safeResend reads lines, validates minimally, attempts send via provided sender, and rewrites file with failures
 */
public final class OfflineLogger {

    private final Path logFile;
    private final LinkedHashMap<String, Boolean> recentFingerprints; // LRU keys only (value unused)
    private final int maxInMemory;

    public OfflineLogger(Path logFile, int maxInMemory) {
        this.logFile = logFile;
        this.maxInMemory = Math.max(64, maxInMemory);
        this.recentFingerprints = new LinkedHashMap<String, Boolean>(this.maxInMemory, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > OfflineLogger.this.maxInMemory;
            }
        };
    }

    /** Append a detection if not recently seen. Returns true if written. */
    public synchronized boolean appendIfNew(DetectionReport dr) throws IOException {
        String fp = dr.fingerprint();
        if (recentFingerprints.containsKey(fp)) {
            // already logged recently
            return false;
        }
        String line = dr.toJsonLine();
        ensureParentExists();
        Files.write(logFile, Collections.singletonList(line), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        recentFingerprints.put(fp, Boolean.TRUE);
        return true;
    }

    private void ensureParentExists() throws IOException {
        Path parent = logFile.getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    /**
     * Safe resend:
     * - sender: Function<String,Boolean> returns true on successful send
     * - skips invalid/corrupted lines (they are dropped)
     * - writes back only lines that failed to send
     */
    public synchronized void safeResend(Function<String, Boolean> sender) throws IOException {
        if (!Files.exists(logFile)) return;

        List<String> all = Files.readAllLines(logFile, StandardCharsets.UTF_8);
        if (all.isEmpty()) return;

        List<String> remaining = new ArrayList<>();
        for (String raw : all) {
            String line = raw == null ? "" : raw.trim();
            if (!isPlausibleJsonLine(line)) {
                // skip corrupted/invalid lines
                continue;
            }
            boolean sent = false;
            try {
                sent = Boolean.TRUE.equals(sender.apply(line));
            } catch (Throwable t) {
                sent = false;
            }
            if (!sent) {
                remaining.add(line);
            }
        }

        // Atomic replace with a temp file then move
        Path tmp = Files.createTempFile(logFile.getParent(), "offline-log-", ".tmp");
        if (!remaining.isEmpty()) {
            Files.write(tmp, remaining, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, logFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } else {
            // nothing left: remove original file
            Files.deleteIfExists(logFile);
            Files.deleteIfExists(tmp);
        }
    }

    /** Very small validation to skip obviously broken lines (not a full JSON parser). */
    private boolean isPlausibleJsonLine(String line) {
        if (line == null) return false;
        if (!line.startsWith("{") || !line.endsWith("}")) return false;
        // require candidateId to exist (simple substring check)
        return line.contains("\"candidateId\"");
    }
}
