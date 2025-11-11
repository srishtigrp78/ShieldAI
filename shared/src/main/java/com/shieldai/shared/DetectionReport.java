// package com.shieldai.shared;
// import jakarta.validation.constraints.DecimalMax;
// import jakarta.validation.constraints.DecimalMin;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;

// public class DetectionReport {

// @NotBlank(message = "Candidate ID cannot be blank")
// private String candidateId;

// @NotBlank(message = "Tool name os required ")
// private String toolName;

// @NotBlank(message = "Tool name os required ")
// private String toolType;

// @NotNull(message = "Timestamp is required")
// private String timestamp;

// @NotBlank(message = "OS info name is required ")
// private String osInfo;

// @NotBlank(message = "Process details are required")
// private String processDetails;

// @NotNull(message = "Confidence value is required")
// @DecimalMin(value = "0.0", inclusive = false, message = "Confidence must be greater than 0")
// @DecimalMax(value = "1.0", inclusive = true, message = "Confidence must be less than or equal to 1")
// private Double confidence;

// // Constructors
// public DetectionReport() {}

// public DetectionReport(String candidateId, String toolName, String toolType, String timestamp, String osInfo, String processDetails, Double confidence) {
// this.candidateId = candidateId;
// this.toolName = toolName;
// this.toolType = toolType;
// this.timestamp = timestamp;
// this.osInfo = osInfo;
// this.processDetails = processDetails;
// this.confidence = confidence;
// }

// // Getters and Setters
// public String getCandidateId() { return candidateId; }
// public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

// public String getToolName() { return toolName; }
// public void setToolName(String toolName) { this.toolName = toolName; }

// public String getToolType() { return toolType; }
// public void setToolType(String toolType) { this.toolType = toolType; }

// public String getTimestamp() { return timestamp; }
// public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

// public String getOsInfo() { return osInfo; }
// public void setOsInfo(String osInfo) { this.osInfo = osInfo; }

// public String getProcessDetails() { return processDetails; }
// public void setProcessDetails(String processDetails) { this.processDetails = processDetails; }

// public Double getConfidence() { return confidence; }
// public void setConfidence(Double confidence) { this.confidence = confidence; }
// }

/** **********tools.json related code changes************ */
package com.shieldai.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DetectionReport {

    private String candidateId;
    private String toolName;
    private String toolType;
    private String timestamp;
    private String osInfo;
    private String processDetails;
    private Double confidence;

    private String description; // New field for extra context

    // ---------------- CONSTRUCTORS ----------------
    public DetectionReport() {}

    public DetectionReport(String candidateId, String toolName, String toolType, String timestamp, String osInfo,
                           String processDetails, Double confidence, String description) {
        this.candidateId = candidateId;
        this.toolName = toolName;
        this.toolType = toolType;
        this.timestamp = timestamp;
        this.osInfo = osInfo;
        this.processDetails = processDetails;
        this.confidence = confidence;
        this.description = description;
    }

    // ---------------- GETTERS & SETTERS ----------------
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getToolType() { return toolType; }
    public void setToolType(String toolType) { this.toolType = toolType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getOsInfo() { return osInfo; }
    public void setOsInfo(String osInfo) { this.osInfo = osInfo; }

    public String getProcessDetails() { return processDetails; }
    public void setProcessDetails(String processDetails) { this.processDetails = processDetails; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    //new code ---------------------------------------------
    // ---------------- Utility methods ----------------

    /** Trim, null-handle and remove characters that break JSON or backend parsing. */
    public void sanitizeFields() {
        this.candidateId = safeTrimOrUnknown(this.candidateId);
        this.toolName = safeTrimOrUnknown(this.toolName);
        this.toolType = safeTrimOrUnknown(this.toolType);
        this.timestamp = safeTrimOrNow(this.timestamp);
        this.osInfo = safeTrimOrUnknown(this.osInfo);
        this.processDetails = sanitizeProcessOrTabTitle(this.processDetails);
        this.description = safeTrimOrEmpty(this.description);
    }

    private static String safeTrimOrUnknown(String s) {
        if (s == null) return "unknown";
        String t = s.trim();
        return t.isEmpty() ? "unknown" : escapeForJson(t);
    }

    private static String safeTrimOrEmpty(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isEmpty() ? "" : escapeForJson(t);
    }

    private static String safeTrimOrNow(String s) {
        if (s == null) return Instant.now().toString();
        String t = s.trim();
        return t.isEmpty() ? Instant.now().toString() : escapeForJson(t);
    }

    /** Remove/escape problematic characters from process/tab titles */
    private static String sanitizeProcessOrTabTitle(String raw) {
        if (raw == null) return "unknown";
        String t = raw.trim();
        if (t.isEmpty()) return "unknown";
        // Remove pipe characters and common header like "URL:"
        t = t.replace("|", " ").replaceAll("(?i)URL:\\s*", "");
        return escapeForJson(t);
    }

    /** Minimal JSON escaping for double-quotes, backslashes and control chars */
    private static String escapeForJson(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Produce a single-line JSON safe for offline logs (includes offlineLoggedAt). */
    public String toJsonLine() {
        sanitizeFields();
        String offlineAt = Instant.now().toString();
        // manual JSON to avoid extra runtime deps — keep a single-line representation
        return "{" +
                "\"candidateId\":\"" + candidateId + "\"," +
                "\"toolName\":\"" + toolName + "\"," +
                "\"toolType\":\"" + toolType + "\"," +
                "\"timestamp\":\"" + timestamp + "\"," +
                "\"osInfo\":\"" + osInfo + "\"," +
                "\"processDetails\":\"" + processDetails + "\"," +
                "\"confidence\":" + (confidence == null ? "null" : confidence.toString()) + "," +
                "\"description\":\"" + description + "\"," +
                "\"offlineLoggedAt\":\"" + offlineAt + "\"" +
                "}";
    }

    /** Deterministic fingerprint for deduplication (base64 of sha-256). */
    public String fingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String seed = (candidateId + "|" + toolName + "|" + toolType + "|" + processDetails + "|" + timestamp);
            byte[] digest = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            // fallback simple hash
            return Integer.toHexString(Objects.hash(candidateId, toolName, toolType, processDetails, timestamp));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetectionReport that = (DetectionReport) o;
        return Objects.equals(this.fingerprint(), that.fingerprint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fingerprint());
    }
}
