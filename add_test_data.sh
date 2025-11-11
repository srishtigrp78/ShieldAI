#!/bin/bash

echo "Adding test detection data..."

# Add ChatGPT browser detection
curl -X POST http://localhost:8080/api/detections \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "test-001",
    "toolName": "chatgpt",
    "toolType": "browser",
    "timestamp": "2024-01-01T10:00:00",
    "osInfo": "macOS 14.0",
    "processDetails": "ChatGPT - chat.openai.com",
    "confidence": 0.95,
    "description": "ChatGPT in browser"
  }'

# Add GitHub Copilot process detection
curl -X POST http://localhost:8080/api/detections \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "test-002", 
    "toolName": "copilot",
    "toolType": "process",
    "timestamp": "2024-01-01T10:05:00",
    "osInfo": "macOS 14.0",
    "processDetails": "GitHub Copilot Extension",
    "confidence": 0.90,
    "description": "GitHub Copilot process"
  }'

echo -e "\nTest data added! Now refresh your browser at http://localhost:3000"