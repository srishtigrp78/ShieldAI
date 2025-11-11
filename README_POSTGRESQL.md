# 🐘 PostgreSQL Setup for ShieldAI

## Quick Setup

### 1. Install & Configure PostgreSQL
```bash
./setup_postgresql.sh
```

### 2. Start ShieldAI with PostgreSQL
```bash
cd dashboard
mvn spring-boot:run -Dspring.profiles.active=local
```

## Manual Setup (Alternative)

### Install PostgreSQL 15
```bash
# macOS
brew install postgresql@15
brew services start postgresql@15

# Ubuntu/Debian
sudo apt-get install postgresql-15 postgresql-client-15
sudo systemctl start postgresql
```

### Create Database & User
```sql
sudo -u postgres psql

CREATE DATABASE shieldai_db;
CREATE USER shieldai_user WITH PASSWORD 'strongpassword';
GRANT ALL PRIVILEGES ON DATABASE shieldai_db TO shieldai_user;

\c shieldai_db
GRANT ALL ON SCHEMA public TO shieldai_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO shieldai_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO shieldai_user;
```

## Database Tools

### pgAdmin (GUI)
1. Download: https://www.pgadmin.org/download/
2. Connect with:
   - Host: localhost
   - Port: 5432
   - Database: shieldai_db
   - Username: shieldai_user
   - Password: strongpassword

### DBeaver (GUI)
1. Download: https://dbeaver.io/download/
2. New Connection → PostgreSQL
3. Use same connection details above

### Command Line
```bash
# Connect to database
psql -h localhost -U shieldai_user -d shieldai_db

# View tables
\dt

# View detection data
SELECT * FROM detection_entity LIMIT 10;

# View offline sync status
SELECT sync_status, COUNT(*) FROM offline_detection_sync GROUP BY sync_status;
```

## Profiles

### Local Development
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```
- Uses PostgreSQL locally
- DDL auto-update enabled
- SQL logging enabled

### Production
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```
- Uses AWS RDS PostgreSQL
- DDL validation only
- SQL logging disabled

## Offline Storage Features

### Automatic Sync
- Runs every 30 seconds
- Syncs pending detections to main DB
- Retries failed detections (max 3 attempts)

### Manual Operations
```bash
# Check offline status
curl http://localhost:8080/api/offline/status

# Trigger manual sync
curl -X POST http://localhost:8080/api/offline/sync

# Import from file system
curl -X POST http://localhost:8080/api/offline/import
```

### File System Backup
- Location: `~/.shieldai/offline/`
- Format: `detection_candidateId_timestamp.json`
- Auto-imported every 5 minutes

## Troubleshooting

### Connection Issues
```bash
# Check PostgreSQL status
brew services list | grep postgresql  # macOS
sudo systemctl status postgresql      # Linux

# Test connection
psql -h localhost -U shieldai_user -d shieldai_db -c "SELECT version();"
```

### Permission Issues
```sql
-- Grant additional permissions if needed
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO shieldai_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO shieldai_user;
```

### Reset Database
```sql
DROP DATABASE shieldai_db;
CREATE DATABASE shieldai_db;
GRANT ALL PRIVILEGES ON DATABASE shieldai_db TO shieldai_user;
```

## AWS RDS Production Setup

### RDS Configuration
- Instance: db.t3.micro
- Storage: 20GB SSD
- Database: shieldai_prod
- User: shieldai_admin
- Security Group: Allow 5432 from Elastic Beanstalk only

### Environment Variables
```bash
export RDS_HOSTNAME=your-rds-endpoint.amazonaws.com
export RDS_PORT=5432
export RDS_DB_NAME=shieldai_prod
export RDS_USERNAME=shieldai_admin
export RDS_PASSWORD=your-secure-password
```

### AWS Secrets Manager (Optional)
```json
{
  "username": "shieldai_admin",
  "password": "your-secure-password",
  "engine": "postgres",
  "host": "your-rds-endpoint.amazonaws.com",
  "port": 5432,
  "dbname": "shieldai_prod"
}
```