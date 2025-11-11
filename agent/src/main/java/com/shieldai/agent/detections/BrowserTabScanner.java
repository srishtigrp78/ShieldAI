package com.shieldai.agent.detections;

import java.util.ArrayList;
import java.util.List;

public class BrowserTabScanner {

    public static class TabDetection {
        public String browser;
        public String title;
        public String url;

        public TabDetection(String browser, String title, String url) {
            this.browser = browser;
            this.title = title;
            this.url = url;
        }
    }

    public static List<TabDetection> detectTabs() {
        List<TabDetection> tabs = new ArrayList<>();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) tabs.addAll(ToolDetector.detectMacBrowserTabs());
            else if (os.contains("win")) tabs.addAll(ToolDetector.detectWindowsBrowserTabs());
            else tabs.addAll(ToolDetector.detectLinuxBrowserTabs());
        } catch (Exception e) {
            System.err.println("[ShieldAI] Failed to detect browser tabs: " + e.getMessage());
        }
        return tabs;
    }
}
