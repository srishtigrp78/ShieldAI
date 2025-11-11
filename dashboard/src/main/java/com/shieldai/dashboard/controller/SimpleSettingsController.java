package com.shieldai.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/settings")
public class SimpleSettingsController {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public SimpleSettingsController() {
        new java.io.File("emails").mkdirs();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("confidenceThreshold", 0.8);
        settings.put("monitoredTools", Arrays.asList("ChatGPT", "Claude", "Copilot", "Gemini"));
        settings.put("dashboardAlerts", true);
        settings.put("emailAlerts", false);
        
        Map<String, String> profile = new HashMap<>();
        profile.put("name", "HR Admin");
        profile.put("email", "hr@company.com");
        
        response.put("settings", settings);
        response.put("profile", profile);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, Object> request) {
        System.out.println("Settings updated: " + request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Settings updated successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean emailSent = false;
        
        // Try to send real Gmail first
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("📧 ShieldAI Test Email");
                message.setText("Dear HR Team,\n\n" +
                    "This is a test email from ShieldAI system.\n" +
                    "Email notifications are working correctly!\n\n" +
                    "If you received this email, the system is ready to send real alerts.\n\n" +
                    "Best regards,\n" +
                    "ShieldAI System");
                message.setFrom("noreply@shieldai.com");
                
                mailSender.send(message);
                emailSent = true;
                System.out.println("📧 Real Gmail sent to: " + email);
            } catch (Exception e) {
                System.err.println("❌ Gmail failed: " + e.getMessage());
            }
        }
        
        // Also save to file as backup
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "emails/test_email_" + timestamp + ".txt";
        
        String emailContent = "📧 SHIELDAI TEST EMAIL\n" +
                "========================\n" +
                "To: " + email + "\n" +
                "From: noreply@shieldai.com\n" +
                "Subject: ShieldAI Test Email\n" +
                "Date: " + LocalDateTime.now() + "\n\n" +
                "Dear HR Team,\n\n" +
                "This is a test email from ShieldAI system.\n" +
                "Email notifications are working correctly!\n\n" +
                "Best regards,\n" +
                "ShieldAI System\n";
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(emailContent);
            System.out.println("💾 Backup saved to: " + filename);
        } catch (Exception e) {
            System.err.println("Failed to write backup: " + e.getMessage());
        }
        
        Map<String, String> response = new HashMap<>();
        if (emailSent) {
            response.put("message", "📧 Real email sent to " + email + " + backup saved to " + filename);
        } else {
            response.put("message", "💾 Email saved to " + filename + " (Gmail config needed for real email)");
        }
        return ResponseEntity.ok(response);
    }
}