#!/bin/bash

echo "🧪 Testing ML Analytics Enhancement"
echo "=================================="

# Build the project
echo "📦 Building ShieldAI with ML Analytics..."
cd /Users/srishtigupta/ShieldAI
mvn clean compile -q

# Start the agent in background to generate analytics data
echo "🚀 Starting ShieldAI Agent with ML Analytics..."
cd agent
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.shieldai.agent.AgentMain &
AGENT_PID=$!

echo "Agent started with PID: $AGENT_PID"
echo "⏳ Generating detection data for 60 seconds..."

# Let it run for 60 seconds to generate some data
sleep 60

# Kill the agent
echo "🛑 Stopping agent..."
kill $AGENT_PID 2>/dev/null

echo ""
echo "✅ Test completed! Expected ML Analytics features:"
echo "  • Risk scores calculated for detected candidates"
echo "  • Pattern detection (burst activity, tool switching)"
echo "  • Risk predictions with trend analysis"
echo "  • Automated alerts for high-risk candidates"
echo ""
echo "📊 Check console output above for:"
echo "  • '[ShieldAI] Agent started with ML Analytics...'"
echo "  • Risk analysis messages"
echo "  • Alert notifications (🚨 ALERT [LEVEL])"
echo "  • Analytics reports"
echo ""
echo "🌐 Frontend Analytics Dashboard available at:"
echo "  http://localhost:3000/analytics"
echo ""
echo "📡 API Endpoints:"
echo "  GET /api/analytics/risk-scores"
echo "  GET /api/analytics/alerts"
echo "  GET /api/analytics/patterns/{candidateId}"