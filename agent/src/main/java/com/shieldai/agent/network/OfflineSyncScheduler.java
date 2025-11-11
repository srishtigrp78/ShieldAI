package com.shieldai.agent.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shieldai.shared.DetectionReport;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OfflineSyncScheduler {

    private static final File OFFLINE_LOG = new File("offline_detections.jsonl");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                syncOfflineDetections();
            } catch (Exception e) {
                System.err.println("[ShieldAI] Offline sync error: " + e.getMessage());
            }
        }, 30, 60, TimeUnit.SECONDS); // Start after 30s, repeat every 60s
    }

    private static synchronized void syncOfflineDetections() throws IOException {
        if (!OFFLINE_LOG.exists() || OFFLINE_LOG.length() == 0) {
            return; // Nothing to sync
        }

        List<String> lines = Files.readAllLines(OFFLINE_LOG.toPath());
        if (lines.isEmpty()) {
            return;
        }

        Iterator<String> iterator = lines.iterator();
        boolean fileChanged = false;

        while (iterator.hasNext()) {
            String line = iterator.next();
            try {
                // Format: timestamp | json
                int sepIndex = line.indexOf("|");
                if (sepIndex == -1) continue;

                String json = line.substring(sepIndex + 1).trim();
                DetectionReport report = objectMapper.readValue(json, DetectionReport.class);

                AgentClient.sendDetection(report); // Uses retry logic
                iterator.remove();
                fileChanged = true;

                System.out.println("[ShieldAI] Offline detection sent: " + report.getToolName());

            } catch (Exception e) {
                System.err.println("[ShieldAI] Failed to resend detection: " + e.getMessage());
            }
        }

        if (fileChanged) {
            // Overwrite file with remaining unsent detections
            Files.write(OFFLINE_LOG.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
    }
}
