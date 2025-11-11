package com.shieldai.agent;

import com.shieldai.agent.detections.DetectionEngine;
import com.shieldai.agent.network.OfflineSyncScheduler;
import com.shieldai.agent.analytics.AnalyticsManager;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    private static AnalyticsManager analyticsManager;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[ShieldAI] Agent started with ML Analytics...");

        // Initialize analytics manager
        analyticsManager = new AnalyticsManager();
        
        // Start offline sync scheduler in background
        OfflineSyncScheduler.start();

        Runnable detectionTask = () -> {
            while (true) {
                try {
                    DetectionEngine.scanAndDetect();
                    Thread.sleep(15000);  // Wait 15 seconds between scans
                } catch (InterruptedException ie) {
                    System.err.println("[ShieldAI] Detection thread interrupted, stopping...");
                    break;  // Exit the loop if interrupted
                } catch (Exception e) {
                    System.err.println("[ShieldAI] Error during detection: " + e.getMessage());
                }
            }
        };

        Thread detectionThread = new Thread(detectionTask, "ShieldAI-DetectionThread");
        detectionThread.setDaemon(true);  // Allows JVM to exit if main app finishes
        detectionThread.start();
    }

    public static void main(String[] args) {
        System.out.println("[ShieldAI] Starting agent in standalone mode with ML Analytics...");
        
        // Initialize analytics manager
        analyticsManager = new AnalyticsManager();
        
        // Start offline sync scheduler
        OfflineSyncScheduler.start();
        
        // Run detection loop
        while (true) {
            try {
                DetectionEngine.scanAndDetect();
                Thread.sleep(15000);  // Wait 15 seconds between scans
            } catch (InterruptedException ie) {
                System.err.println("[ShieldAI] Detection interrupted, stopping...");
                break;
            } catch (Exception e) {
                System.err.println("[ShieldAI] Error during detection: " + e.getMessage());
            }
        }
        
        // Cleanup
        if (analyticsManager != null) {
            analyticsManager.shutdown();
        }
    }
    
    public static AnalyticsManager getAnalyticsManager() {
        return analyticsManager;
    }
}

