# Email Setup Instructions

## To Send Real Emails:

### Option 1: Gmail Setup (Recommended for Testing)
1. Go to your Gmail account settings
2. Enable 2-factor authentication
3. Generate an "App Password" for ShieldAI
4. Update `dashboard/src/main/resources/application.properties`:
   ```
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-16-digit-app-password
   ```

### Option 2: Use MailHog (Local Testing)
```bash
# Install MailHog
brew install mailhog  # macOS
# or download from https://github.com/mailhog/MailHog

# Run MailHog
mailhog

# Update application.properties:
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
```

### Option 3: Company SMTP Server
Update application.properties with your company's SMTP settings:
```
spring.mail.host=smtp.company.com
spring.mail.port=587
spring.mail.username=hr@company.com
spring.mail.password=company-password
```

## Testing Steps:
1. Start dashboard: `mvn spring-boot:run`
2. Start frontend: `npm start`
3. Go to Settings → Enter your email
4. Enable email alerts
5. Click "Send Test Email"
6. Check your inbox!

## For HR Use:
- Enter HR manager's email in settings
- Enable email alerts
- When AI tools are detected, alerts will be sent automatically
- Test emails verify the system is working