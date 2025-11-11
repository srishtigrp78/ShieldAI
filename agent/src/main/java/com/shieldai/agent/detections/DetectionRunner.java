package com.shieldai.agent.detections;

import com.shieldai.agent.network.AgentClient;
import com.shieldai.shared.offline.OfflineLogger;

import java.nio.file.Paths;
import java.time.Instant;

/** Small runner to run a scan or perform a one-off resend of offline logs.
 * Usage:
 *  - run scan:   java ... com.shieldai.agent.detections.DetectionRunner
 *  - resend:     java ... com.shieldai.agent.detections.DetectionRunner resend
 */
public class DetectionRunner {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && "resend".equalsIgnoreCase(args[0])) {
                String offline = System.getenv().getOrDefault("OFFLINE_LOG_PATH", "offline_detections.jsonl");
                OfflineLogger ol = new OfflineLogger(Paths.get(offline), 1024);
                System.out.println(Instant.now() + " Starting offline resend from " + offline);
                ol.safeResend(line -> AgentClient.sendJsonLine(line));
                System.out.println(Instant.now() + " Offline resend completed");
                return;
            }

            // Default: run detection scan once
            DetectionEngine.scanAndDetect();
        } catch (Throwable t) {
            System.err.println("[ShieldAI] Runner failure: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}