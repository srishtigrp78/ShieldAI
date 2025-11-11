package com.shieldai.dashboard.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "detections")
public class DetectionEntity {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String candidateId;
private String toolName;
private String toolType;
private String timestamp;
private String osInfo;
@Column(length = 2000)
private String processDetails;
private String description;

@Column(nullable = false)
private Double confidence;

// Getters & Setters
public Long getId() { return id; }
public void setId(Long id) { this.id = id; }

public String getCandidateId() { return candidateId; }
public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

public String getToolName() { return toolName; }
public void setToolName(String toolName) { this.toolName = toolName; }

public String getToolType() { return toolType; }
public void setToolType(String toolType) { this.toolType = toolType; }

public String getTimestamp() { return timestamp; }
public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

public String getOsInfo() { return osInfo; }
public void setOsInfo(String osInfo) { this.osInfo = osInfo; }

public String getProcessDetails() { return processDetails; }
public void setProcessDetails(String processDetails) 
{ this.processDetails = processDetails;}

public Double getConfidence() { return confidence; }
public void setConfidence(Double confidence) { this.confidence = confidence; }

public String getDescription() { return description; }
public void setDescription(String description) { this.description = description; }

}