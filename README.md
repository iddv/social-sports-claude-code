# SocialSports WhatsApp Chatbot

A WhatsApp chatbot that facilitates the organization and coordination of sports events, initially focusing on padel.

## Features

- Create and join sports events through WhatsApp
- Receive notifications and reminders about events
- Automatic WhatsApp group creation for event participants
- Auto-cancellation of events with insufficient participants
- User-friendly commands for event management

## Tech Stack

- Java 17
- Spring Boot 3.x
- AWS DynamoDB for data storage
- Twilio/WhatsApp Business API for messaging
- RESTful webhook API for message processing

## Prerequisites

- JDK 17 or later
- Maven 3.6 or later
- AWS account (for DynamoDB)
- Twilio account or Meta Business account (for WhatsApp Business API)
- ngrok or similar for local webhook testing

## Environment Variables

The following environment variables need to be set:

```
# AWS Credentials
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# Twilio Configuration
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=your_twilio_whatsapp_number

# WhatsApp Business API (if using WhatsApp directly)
WHATSAPP_BUSINESS_PHONE_NUMBER_ID=your_phone_number_id
WHATSAPP_ACCESS_TOKEN=your_access_token
```

## Getting Started

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/social-sports-cc.git
   cd social-sports-cc
   ```

2. Build the application:
   ```
   mvn clean install
   ```

3. Start the local DynamoDB instance for development (optional):
   ```
   # Using Docker
   docker run -p 8000:8000 amazon/dynamodb-local
   ```

4. Run the application:
   ```
   mvn spring-boot:run
   ```

5. Expose the webhook endpoint using ngrok:
   ```
   ngrok http 8080
   ```

6. Configure the webhook URL in the Twilio or WhatsApp Business dashboard:
   ```
   https://your-ngrok-url.ngrok.io/api/webhook
   ```

## WhatsApp Bot Commands

- `CREATE EVENT [sport] AT [location] ON [date] FOR [number] PLAYERS SKILL [1-5] BOOKING [optional-url]`
  - Creates a new sports event
  - Example: `CREATE EVENT PADEL AT City Sports Club ON 2023-04-15 18:30 FOR 4 PLAYERS SKILL 3 BOOKING http://example.com`

- `JOIN [eventId]`
  - Join an existing sports event
  - Example: `JOIN PAD-20230415-A1B2`

- `LEAVE [eventId]`
  - Leave an event you have joined
  - Example: `LEAVE PAD-20230415-A1B2`

- `CANCEL [eventId]`
  - Cancel an event (creator only)
  - Example: `CANCEL PAD-20230415-A1B2`

- `EVENTS`
  - List all upcoming events

- `HELP`
  - Display available commands

## Development

### Running Tests
```
mvn test
```

### Local Development with DynamoDB
For local development, the application is configured to use a local DynamoDB instance at `http://localhost:8000`.

### Creating DynamoDB Tables
Tables will be created automatically on startup. The application uses the following tables:
- User
- Event
- Community

## Deployment

### AWS Deployment
1. Package the application:
   ```
   mvn package
   ```

2. Deploy to AWS Elastic Beanstalk or AWS Lambda with API Gateway.

3. Configure the WhatsApp webhook to point to your deployed application.

## Roadmap

- [ ] Add support for more sports beyond padel
- [ ] Implement LLM integration for natural language processing
- [ ] Add payment integration (e.g., Tikkie)
- [ ] Create venue booking integration
- [ ] Implement user rating system

## License

This project is licensed under the MIT License - see the LICENSE file for details.