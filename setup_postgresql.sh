#!/bin/bash

echo "🐘 Setting up PostgreSQL for ShieldAI"
echo "====================================="

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL not found. Installing..."
    
    # macOS installation
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew &> /dev/null; then
            brew install postgresql@15
            brew services start postgresql@15
        else
            echo "Please install Homebrew first: https://brew.sh/"
            exit 1
        fi
    # Linux installation
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt-get update
        sudo apt-get install -y postgresql-15 postgresql-client-15
        sudo systemctl start postgresql
        sudo systemctl enable postgresql
    else
        echo "Unsupported OS. Please install PostgreSQL 15 manually."
        exit 1
    fi
else
    echo "✅ PostgreSQL found"
fi

# Create database and user
echo "🔧 Creating database and user..."

# Create database and user
sudo -u postgres psql << EOF
-- Create database
CREATE DATABASE shieldai_db;

-- Create user
CREATE USER shieldai_user WITH PASSWORD 'strongpassword';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE shieldai_db TO shieldai_user;

-- Grant schema privileges
\c shieldai_db
GRANT ALL ON SCHEMA public TO shieldai_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO shieldai_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO shieldai_user;

-- Show databases
\l

-- Show users
\du

EOF

echo ""
echo "✅ PostgreSQL setup complete!"
echo ""
echo "📋 Connection Details:"
echo "Database: shieldai_db"
echo "User: shieldai_user"
echo "Password: strongpassword"
echo "Host: localhost"
echo "Port: 5432"
echo ""
echo "🔗 Connection URL:"
echo "jdbc:postgresql://localhost:5432/shieldai_db"
echo ""
echo "🧪 Test connection:"
echo "psql -h localhost -U shieldai_user -d shieldai_db"
echo ""
echo "🚀 Start ShieldAI with PostgreSQL:"
echo "cd dashboard && mvn spring-boot:run -Dspring.profiles.active=local"