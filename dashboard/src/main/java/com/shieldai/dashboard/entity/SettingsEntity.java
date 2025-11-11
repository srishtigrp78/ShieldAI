package com.shieldai.dashboard.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "settings")
public class SettingsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "confidence_threshold")
    private Double confidenceThreshold = 0.8;
    
    @ElementCollection
    @CollectionTable(name = "monitored_tools", joinColumns = @JoinColumn(name = "settings_id"))
    @Column(name = "tool_name")
    private List<String> monitoredTools;
    
    @Column(name = "dashboard_alerts")
    private Boolean dashboardAlerts = true;
    
    @Column(name = "email_alerts")
    private Boolean emailAlerts = false;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(Double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
    
    public List<String> getMonitoredTools() { return monitoredTools; }
    public void setMonitoredTools(List<String> monitoredTools) { this.monitoredTools = monitoredTools; }
    
    public Boolean getDashboardAlerts() { return dashboardAlerts; }
    public void setDashboardAlerts(Boolean dashboardAlerts) { this.dashboardAlerts = dashboardAlerts; }
    
    public Boolean getEmailAlerts() { return emailAlerts; }
    public void setEmailAlerts(Boolean emailAlerts) { this.emailAlerts = emailAlerts; }
}