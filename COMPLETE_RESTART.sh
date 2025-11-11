#!/bin/bash

echo "🔄 COMPLETE SYSTEM RESTART"
echo "=========================="

# Kill everything
echo "🛑 Killing all processes..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:3000 | xargs kill -9 2>/dev/null
sleep 3

# Clean rebuild
echo "🔨 Clean rebuild..."
cd dashboard
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

# Start dashboard
echo "🚀 Starting dashboard..."
mvn spring-boot:run &
DASHBOARD_PID=$!
cd ..

# Wait for startup
echo "⏳ Waiting for dashboard startup..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/settings > /dev/null 2>&1; then
        echo "✅ Dashboard ready!"
        break
    fi
    sleep 1
    if [ $i -eq 30 ]; then
        echo "❌ Dashboard failed to start"
        exit 1
    fi
done

# Start frontend
echo "🚀 Starting frontend..."
cd frontend
npm start &
FRONTEND_PID=$!
cd ..

echo "⏳ Waiting for frontend..."
sleep 5

echo ""
echo "✅ SYSTEM READY!"
echo "🌐 Frontend: http://localhost:3000"
echo "🔧 Settings: http://localhost:3000/settings"
echo "📊 Dashboard: http://localhost:3000/dashboard"
echo ""
echo "🧪 Test signup: http://localhost:3000/signup"
echo "🧪 Test login: http://localhost:3000/login"
echo ""
echo "Press Enter to stop all services..."
read

# Cleanup
kill $DASHBOARD_PID $FRONTEND_PID 2>/dev/null
echo "All services stopped"