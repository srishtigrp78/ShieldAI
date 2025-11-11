package com.shieldai.agent.detections;

import com.shieldai.agent.network.AgentClient;
import com.shieldai.agent.AgentMain;
import com.shieldai.shared.DetectionReport;
import com.shieldai.shared.util.PermissionChecker;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.ProcessSorting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DetectionEngine - integrated:
 * - safe inspection wrapper (PermissionChecker.runSafely) to capture permission/inspection failures
 * - null/empty-safe handling of tab/process titles and args
 * - call report.sanitizeFields() before sending
 * - continue scanning when an inspection is blocked or fails
 */
public class DetectionEngine {
    private static String sessionCandidateId = UUID.randomUUID().toString();

    public static void scanAndDetect() {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();

        // Fetching top 10 processes sorted by CPU usage in descending order
        List<OSProcess> processes = os.getProcesses(null, ProcessSorting.CPU_DESC, 10);

        // 1. Scan processes
        for (OSProcess process : processes) {
            // Use PermissionChecker.runSafely to avoid crashing on platform permission/IO issues.
            Optional<String> searchableOpt = PermissionChecker.runSafely(() -> {
                String processName = safeString(process.getName()).toLowerCase();
                List<String> args = process.getArguments();
                String joinedArgs = (args == null || args.isEmpty())
                        ? ""
                        : args.stream().map(DetectionEngine::safeString).collect(Collectors.joining(" "));
                return (processName + " " + joinedArgs).trim().toLowerCase();
            }, reason -> System.err.println("[ShieldAI] Process inspection blocked: " + reason));

            if (searchableOpt.isEmpty()) {
                // inspection blocked for this process; continue with others
                continue;
            }

            String searchableData = searchableOpt.get();
            String matchedTool = ToolDetector.findMatchedTool(searchableData, "process");
            if (matchedTool != null) {
                sendReport(matchedTool, searchableData, "", "process");
            }
        }

        // 2. Scan browser tabs - wrap the whole detection call to capture permission issues
        Optional<List<BrowserTabScanner.TabDetection>> tabsOpt = PermissionChecker.runSafely(
                BrowserTabScanner::detectTabs,
                reason -> System.err.println("[ShieldAI] Browser tab inspection blocked: " + reason)
        );

        if (tabsOpt.isPresent()) {
            List<BrowserTabScanner.TabDetection> tabDetections = tabsOpt.get();
            for (BrowserTabScanner.TabDetection tab : tabDetections) {
                String title = safeString(tab.title);
                String url = safeString(tab.url);
                
                // Skip empty tabs
                if (title.isEmpty() && url.isEmpty()) continue;
                
                String searchableData = (title + " " + url).trim().toLowerCase();
                
                // Check for AI tools first
                String matchedTool = ToolDetector.findMatchedTool(searchableData, "browser");
                if (matchedTool != null) {
                    sendReport(matchedTool, title, url, "browser");
                    continue;
                }
                
                // Check for common non-AI tools
                String commonTool = ToolDetector.identifyCommonTool(title, url);
                if (commonTool != null) {
                    sendReport(commonTool, title, url, "browser");
                }
            }
        }
    }

    private static void sendReport(String toolName, String details, String url, String type) {
        if (toolName == null) return;

        DetectionReport report = new DetectionReport();
        report.setCandidateId(sessionCandidateId);
        report.setToolName(toolName);
        report.setToolType(type);
        report.setTimestamp(LocalDateTime.now().toString());
        report.setOsInfo(System.getProperty("os.name") + " " + System.getProperty("os.version"));
        
        // Ensure candidateId is not null
        if (report.getCandidateId() == null) {
            report.setCandidateId(sessionCandidateId);
        }

        // Only include relevant details for this specific tool
        String processDetails = safeString(details);
        if (!safeString(url).isEmpty()) {
            processDetails += ", " + url;
        }
        
        // Truncate to prevent database errors
        if (processDetails.length() > 1900) {
            processDetails = processDetails.substring(0, 1900) + "...";
        }
        report.setProcessDetails(processDetails);
        
        // Set description based on tool type
        String description = ToolDetector.getDescription(toolName);
        if (description.isEmpty()) {
            description = toolName + " in browser";
        }
        report.setDescription(description);

        // Confidence: 0.95 for processes, 0.85 for browser AI tools, 0.75 for common tools
        double confidence = "process".equals(type) ? 0.95 : 
                           (ToolDetector.findMatchedTool(toolName.toLowerCase(), "browser") != null ? 0.85 : 0.75);
        report.setConfidence(confidence);

        // sanitize fields to ensure JSON-safe content and normalized empty/null handling
        try {
            report.sanitizeFields();
        } catch (Throwable t) {
            System.err.println("[ShieldAI] Warning: sanitizeFields failed: " + t.getMessage());
        }

        try {
            AgentClient.sendDetection(report);
        } catch (Throwable t) {
            // AgentClient.sendDetection should handle offline persistence; log and continue
            System.err.println("[ShieldAI] Failed to send detection: " + t.getMessage());
        }
        
        // Process through ML analytics
        try {
            if (AgentMain.getAnalyticsManager() != null) {
                AgentMain.getAnalyticsManager().processDetection(report);
            }
        } catch (Throwable t) {
            System.err.println("[ShieldAI] Analytics processing failed: " + t.getMessage());
        }

        System.out.println("[ShieldAI] Detected " + type + ": " + toolName);
    }

    // small helper to avoid NPEs and trim input
    private static String safeString(String s) {
        if (s == null) return "";
        return s.trim();
    }
}