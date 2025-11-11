package com.shieldai.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DetectionReportTest {

    @Test
    public void sanitizeAndToJsonLine_containsEscapesAndOfflineTimestamp() {
        DetectionReport r = new DetectionReport();
        r.setCandidateId(null);
        r.setToolName("Test\"Tool");
        r.setToolType("browser");
        r.setTimestamp(null);
        r.setOsInfo("macOS");
        r.setProcessDetails("Example | URL: http://x.com \"quote\"");
        r.setConfidence(0.5);
        r.setDescription("desc");

        String json = r.toJsonLine();
        assertNotNull(json);
        assertTrue(json.contains("\"candidateId\""));
        assertTrue(json.contains("\\\"quote\\\"") || json.contains("\\\"Tool")); // escaped quotes present
        assertTrue(json.contains("\"offlineLoggedAt\""));
    }

    @Test
    public void fingerprint_isStableForSameLogicalFields() {
        DetectionReport a = new DetectionReport();
        a.setCandidateId("cid");
        a.setToolName("t");
        a.setToolType("type");
        a.setProcessDetails("proc");
        a.setTimestamp("ts");

        DetectionReport b = new DetectionReport();
        b.setCandidateId("cid");
        b.setToolName("t");
        b.setToolType("type");
        b.setProcessDetails("proc");
        b.setTimestamp("ts");

        assertEquals(a.fingerprint(), b.fingerprint());
        assertEquals(a, b);
    }
}