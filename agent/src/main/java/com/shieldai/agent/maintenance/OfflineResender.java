package com.shieldai.agent.maintenance;

import com.shieldai.agent.network.AgentClient;
import com.shieldai.shared.offline.OfflineLogger;

import java.nio.file.Paths;
import java.time.Instant;

/** Minimal standalone main to trigger safeResend (can be scheduled / cron-invoked). */
public class OfflineResender {
    public static void main(String[] args) {
        String offline = System.getenv().getOrDefault("OFFLINE_LOG_PATH", "offline_detections.jsonl");
        OfflineLogger ol = new OfflineLogger(Paths.get(offline), 1024);
        System.out.println(Instant.now() + " OfflineResender: starting resend for " + offline);
        try {
            ol.safeResend(line -> AgentClient.sendJsonLine(line));
            System.out.println(Instant.now() + " OfflineResender: finished");
        } catch (Throwable t) {
            System.err.println("[ShieldAI] OfflineResender failed: " + t.getMessage());
        }
    }
}