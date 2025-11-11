#!/bin/bash

echo "🧪 Testing PostgreSQL Setup for ShieldAI"
echo "========================================"

# Test 1: Check PostgreSQL connection
echo "1. Testing PostgreSQL connection..."
if psql -h localhost -U shieldai_user -d shieldai_db -c "SELECT version();" > /dev/null 2>&1; then
    echo "✅ PostgreSQL connection successful"
else
    echo "❌ PostgreSQL connection failed"
    echo "💡 Run ./setup_postgresql.sh first"
    exit 1
fi

# Test 2: Start dashboard with PostgreSQL
echo ""
echo "2. Starting dashboard with PostgreSQL profile..."
cd dashboard

# Kill existing dashboard
lsof -ti:8080 | xargs kill -9 2>/dev/null

# Start with local profile
mvn spring-boot:run -Dspring.profiles.active=local > ../dashboard-postgres.log 2>&1 &
DASHBOARD_PID=$!
cd ..

# Wait for startup
echo "⏳ Waiting for dashboard startup..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo "✅ Dashboard started with PostgreSQL"
        break
    fi
    sleep 1
    if [ $i -eq 30 ]; then
        echo "❌ Dashboard failed to start"
        echo "📋 Check logs:"
        tail -20 dashboard-postgres.log
        kill $DASHBOARD_PID 2>/dev/null
        exit 1
    fi
done

# Test 3: Test detection API
echo ""
echo "3. Testing detection API..."
response=$(curl -s -X POST http://localhost:8080/api/detections \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "test-postgres-001",
    "toolName": "ChatGPT",
    "toolType": "browser",
    "timestamp": "2025-09-21T10:00:00",
    "osInfo": "macOS 14.0",
    "processDetails": "Chrome Browser",
    "confidence": 0.95,
    "description": "PostgreSQL test detection"
  }')

if [[ "$response" == *"successfully"* ]]; then
    echo "✅ Detection API working"
else
    echo "❌ Detection API failed: $response"
fi

# Test 4: Check database tables
echo ""
echo "4. Checking database tables..."
tables=$(psql -h localhost -U shieldai_user -d shieldai_db -t -c "\dt" 2>/dev/null | wc -l)
if [ $tables -gt 0 ]; then
    echo "✅ Database tables created ($tables tables)"
    psql -h localhost -U shieldai_user -d shieldai_db -c "\dt"
else
    echo "❌ No database tables found"
fi

# Test 5: Check offline sync status
echo ""
echo "5. Testing offline sync API..."
offline_status=$(curl -s http://localhost:8080/api/offline/status)
if [[ "$offline_status" == *"pendingCount"* ]]; then
    echo "✅ Offline sync API working"
    echo "📊 Status: $offline_status"
else
    echo "❌ Offline sync API failed"
fi

# Test 6: Check Flyway migrations
echo ""
echo "6. Checking Flyway migrations..."
migrations=$(psql -h localhost -U shieldai_user -d shieldai_db -t -c "SELECT COUNT(*) FROM flyway_schema_history;" 2>/dev/null)
if [ "$migrations" -gt 0 ]; then
    echo "✅ Flyway migrations applied ($migrations migrations)"
else
    echo "❌ No Flyway migrations found"
fi

echo ""
echo "🎉 PostgreSQL setup test completed!"
echo ""
echo "📋 Next steps:"
echo "1. Use pgAdmin or DBeaver to view data"
echo "2. Test offline storage by stopping dashboard"
echo "3. Check logs: tail -f dashboard-postgres.log"
echo ""
echo "Press Enter to stop dashboard..."
read

kill $DASHBOARD_PID 2>/dev/null
echo "Dashboard stopped"