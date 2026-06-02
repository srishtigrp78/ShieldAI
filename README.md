# ShieldAI
ShieldAI is an AI tool monitoring and detection system designed for organizations to track the usage of AI tools (ChatGPT, GitHub Copilot, Claude, etc.) by candidates or employees during assessments or work sessions.

## Tech Stack

**Java Spring Boot, React (TypeScript), Tailwind CSS, PostgreSQL, AWS RDS, JWT**

## Project Structure

```
ShieldAI/
├── agent/        # Java agent for monitoring AI tool usage
├── dashboard/    # Spring Boot backend with REST APIs
├── shared/       # Shared models and utilities
└── frontend/     # React TypeScript dashboard UI
```

## How It Works

- A lightweight Java agent runs in the background on the candidate's machine
- It continuously scans running processes and open browser tabs every 15 seconds
- Detected AI tool usage is reported to the dashboard in real time
- HR/admins can monitor activity through the web dashboard

## Detected Tools

**Browser:** ChatGPT, Claude, Google Gemini, Microsoft Copilot, Grok, Perplexity AI, Notion AI, Codeium, and more

**Desktop Processes:** GitHub Copilot, AWS CodeWhisperer, Tabnine, Replit, JetBrains AI, Cline, and more

## Features

- Cross-platform agent (Windows, macOS, Linux)
- Real-time detection and reporting
- Offline mode with auto-sync when connection is restored
- JWT-based authentication for dashboard
- Email notifications via Gmail SMTP
- Export detection reports
- ML-based analytics and risk scoring
- AWS RDS PostgreSQL for production database

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL
- Maven

### Run Backend
```bash
cd dashboard
mvn spring-boot:run -Dspring.profiles.active=local
```

### Run Frontend
```bash
cd frontend
npm install
npm start
```

### Run Agent
```bash
cd agent
mvn package
java -javaagent:target/agent-1.0-SNAPSHOT.jar -jar your-app.jar
```

## Deployment

- Backend: AWS Elastic Beanstalk
- Database: AWS RDS PostgreSQL
- Frontend: Static hosting (S3 + CloudFront or Elastic Beanstalk)
