package com.shieldai.dashboard.service;

import com.shieldai.dashboard.entity.SettingsEntity;
import com.shieldai.dashboard.repository.SettingsRepository;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Optional;

@Service
public class SettingsService {
    
    private final SettingsRepository settingsRepository;
    
    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }
    
    public SettingsEntity getSettings() {
        Optional<SettingsEntity> settings = settingsRepository.findById(1L);
        if (settings.isPresent()) {
            return settings.get();
        }
        
        // Create default settings if none exist
        SettingsEntity defaultSettings = new SettingsEntity();
        defaultSettings.setConfidenceThreshold(0.8);
        defaultSettings.setMonitoredTools(Arrays.asList("ChatGPT", "Claude", "Copilot", "Gemini"));
        defaultSettings.setDashboardAlerts(true);
        defaultSettings.setEmailAlerts(false);
        
        return settingsRepository.save(defaultSettings);
    }
    
    public SettingsEntity updateSettings(SettingsEntity settings) {
        settings.setId(1L); // Always update the same settings record
        return settingsRepository.save(settings);
    }
    
    public boolean shouldTriggerAlert(double confidence) {
        SettingsEntity settings = getSettings();
        return confidence >= settings.getConfidenceThreshold();
    }
    
    public boolean isToolMonitored(String toolName) {
        SettingsEntity settings = getSettings();
        return settings.getMonitoredTools().contains(toolName);
    }
}