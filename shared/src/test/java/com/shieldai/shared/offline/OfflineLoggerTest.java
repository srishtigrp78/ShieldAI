package com.shieldai.shared.offline;

import com.shieldai.shared.DetectionReport;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OfflineLoggerTest {

    @Test
    public void appendIfNew_and_safeResend_behaviour() throws Exception {
        Path tmp = Files.createTempFile("offline-test-", ".jsonl");
        try {
            OfflineLogger ol = new OfflineLogger(tmp, 32);

            DetectionReport r = new DetectionReport();
            r.setCandidateId("cid");
            r.setToolName("tool");
            r.setToolType("type");
            r.setTimestamp("t");
            r.setOsInfo("os");
            r.setProcessDetails("proc");
            r.setConfidence(0.9);

            boolean first = ol.appendIfNew(r);
            assertTrue(first, "first append should write");
            boolean second = ol.appendIfNew(r);
            assertFalse(second, "second append should be deduplicated in-memory");

            // safeResend with a sender that returns true (simulates success)
            AtomicInteger called = new AtomicInteger(0);
            ol.safeResend(line -> {
                called.incrementAndGet();
                return true;
            });

            // file should be removed or empty after successful resend
            assertFalse(Files.exists(tmp) && Files.size(tmp) > 0, "offline file should be empty or removed after successful resend");

        } finally {
            try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
        }
    }
}