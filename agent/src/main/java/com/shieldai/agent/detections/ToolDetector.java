package com.shieldai.agent.detections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToolDetector {

      private static final List<Map<String, String>> TOOLS = loadTools();

    private static List<Map<String, String>> loadTools() {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream is = ToolDetector.class.getClassLoader()
            .getResourceAsStream("tools.json")) { // classpath resource
        if (is == null) {
            System.err.println("[ShieldAI] tools.json not found in classpath!");
            return new ArrayList<>();
        }

        // Deserialize top-level object first
        Map<String, List<Map<String, String>>> root = mapper.readValue(
                is, new TypeReference<Map<String, List<Map<String, String>>>>() {}
        );

        List<Map<String, String>> tools = root.get("tools");
        if (tools == null) {
            System.err.println("[ShieldAI] No 'tools' array found in tools.json!");
            return new ArrayList<>();
        }
        return tools;

    } catch (Exception e) {
        System.err.println("[ShieldAI] Failed to load tools.json: " + e.getMessage());
        return new ArrayList<>();
    }
}


    // ---------------- MAC ----------------
    public static List<BrowserTabScanner.TabDetection> detectMacBrowserTabs() throws Exception {
        List<BrowserTabScanner.TabDetection> results = new ArrayList<>();

        String[] safariScript = {
                "osascript", "-e",
                "tell application \"Safari\" to repeat with w in windows\n" +
                "repeat with t in tabs of w\n" +
                "set tabInfo to (name of t) & \"|||\" & (URL of t)\n" +
                "log tabInfo\n" +
                "end repeat\n" +
                "end repeat"
        };
        results.addAll(runAppleScript("Safari", safariScript));

        String[] chromeScript = {
                "osascript", "-e",
                "tell application \"Google Chrome\" to repeat with w in windows\n" +
                "repeat with t in tabs of w\n" +
                "set tabInfo to (title of t) & \"|||\" & (URL of t)\n" +
                "log tabInfo\n" +
                "end repeat\n" +
                "end repeat"
        };
        results.addAll(runAppleScript("Google Chrome", chromeScript));

        return results;
    }

    private static List<BrowserTabScanner.TabDetection> runAppleScript(String browser, String[] script) throws Exception {
        List<BrowserTabScanner.TabDetection> results = new ArrayList<>();
        Process proc = new ProcessBuilder(script).start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Split by our custom delimiter
                String[] parts = line.split("\\|\\|\\|");
                if (parts.length >= 2) {
                    String title = parts[0].trim();
                    String url = parts[1].trim();
                    results.add(new BrowserTabScanner.TabDetection(browser, title, url));
                }
            }
        }

        return results;
    }

    // ---------------- WINDOWS ----------------
    public static List<BrowserTabScanner.TabDetection> detectWindowsBrowserTabs() throws Exception {
        List<BrowserTabScanner.TabDetection> results = new ArrayList<>();
        List<Win32WindowUtil.WindowInfo> windows = Win32WindowUtil.getAllWindowInfo();

        for (Win32WindowUtil.WindowInfo win : windows) {
            // Check for AI tools first
            if (containsTool(win.processName, "browser") || containsTool(win.title, "browser") || containsTool(win.url, "browser")) {
                results.add(new BrowserTabScanner.TabDetection(win.processName, win.title, win.url));
            }
            // Also check for common non-AI tools
            else if (identifyCommonTool(win.title, win.url) != null) {
                results.add(new BrowserTabScanner.TabDetection(win.processName, win.title, win.url));
            }
        }
        return results;
    }

    // ---------------- LINUX ----------------
    public static List<BrowserTabScanner.TabDetection> detectLinuxBrowserTabs() throws Exception {
        List<BrowserTabScanner.TabDetection> results = new ArrayList<>();
        Process proc = new ProcessBuilder("wmctrl", "-l").start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Check for AI tools first
                if (containsTool(line, "browser")) {
                    results.add(new BrowserTabScanner.TabDetection("Linux", line, ""));
                }
                // Also check for common non-AI tools
                else if (identifyCommonTool(line, "") != null) {
                    results.add(new BrowserTabScanner.TabDetection("Linux", line, ""));
                }
            }
        }

        return results;
    }

    // ---------------- TOOL MATCHING ----------------
    public static String findMatchedTool(String text, String type) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        for (Map<String, String> tool : TOOLS) {
            String toolName = tool.get("name").toLowerCase();
            String toolType = tool.get("type").toLowerCase();
            if (toolType.equals(type)) {
                // Special handling for browser tools - check URL domains
                if ("browser".equals(type)) {
                    if (matchesBrowserTool(lower, toolName)) {
                        return tool.get("name");
                    }
                } else if (lower.contains(toolName)) {
                    return tool.get("name");
                }
            }
        }

        return null;
    }

    private static boolean matchesBrowserTool(String text, String toolName) {
        switch (toolName) {
            case "chatgpt":
                return text.contains("chatgpt.com") || text.contains("openai.com");
            case "google-gemini":
                return text.contains("gemini.google.com") || text.contains("bard.google.com");
            case "claude":
                return text.contains("claude.ai") || text.contains("anthropic.com");
            case "microsoft-copilot-web":
                return text.contains("copilot.microsoft.com") || text.contains("bing.com/chat");
            case "grok":
                return text.contains("grok.x.com") || text.contains("x.com/grok");
            case "notion-ai":
                return text.contains("notion.so") || text.contains("notion.com");
            case "perplexity-ai":
            case "perplexity ai":
                return text.contains("perplexity.ai");
            case "codeium":
                return text.contains("codeium.com") || text.contains("codeium.en.softonic.com");
            default:
                return text.contains(toolName.replace("-", "").replace(" ", ""));
        }
    }

    public static String getDescription(String toolName) {
        for (Map<String, String> tool : TOOLS) {
            if (tool.get("name").equalsIgnoreCase(toolName)) {
                return tool.getOrDefault("description", "");
            }
        }
        return "";
    }

    private static boolean containsTool(String text, String type) {
        return findMatchedTool(text, type) != null;
    }

    // Helper method to identify common non-AI tools that should be detected separately
    public static String identifyCommonTool(String title, String url) {
        String combined = (title + " " + url).toLowerCase();
        
        if (combined.contains("youtube.com")) return "YouTube";
        if (combined.contains("gmail.com") || combined.contains("mail.google.com")) return "Gmail";
        if (combined.contains("nodejs.org")) return "Node.js Documentation";
        if (combined.contains("github.com")) return "GitHub";
        if (combined.contains("stackoverflow.com")) return "Stack Overflow";
        if (combined.contains("linkedin.com")) return "LinkedIn";
        if (combined.contains("twitter.com") || combined.contains("x.com")) return "Twitter/X";
        if (combined.contains("facebook.com")) return "Facebook";
        if (combined.contains("instagram.com")) return "Instagram";
        if (combined.contains("reddit.com")) return "Reddit";
        if (combined.contains("perplexity.ai")) return "Perplexity AI";
        if (combined.contains("codeium.com") || combined.contains("codeium.en.softonic.com")) return "Codeium";
        if (combined.contains("kilocode.ai")) return "KiloCode AI";
        
        return null;
    }
}
