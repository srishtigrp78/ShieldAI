#!/bin/bash

echo "Testing ShieldAI Detection System..."

# Test 1: Send a detection
echo "1. Sending test detection..."
curl -X POST http://localhost:8080/api/detections \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "test-chatgpt-001",
    "toolName": "chatgpt",
    "toolType": "browser",
    "timestamp": "2024-01-01T10:00:00",
    "osInfo": "macOS 14.0",
    "processDetails": "ChatGPT - chat.openai.com",
    "confidence": 0.95,
    "description": "ChatGPT in browser"
  }'

echo -e "\n\n2. Retrieving all detections..."
curl http://localhost:8080/api/detections

echo -e "\n\nTest completed!"