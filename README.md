# SocialSports WhatsApp Chatbot

A Spring Boot application that facilitates sports event organization through WhatsApp.

## Features

- Create and manage sports events
- Join existing events
- Automatic WhatsApp group creation
- Event reminders
- Participant management
- Premium user features

## Tech Stack

- Java 17
- Spring Boot 3.x
- AWS DynamoDB
- WhatsApp Business API for messaging
- Docker & Docker Compose
- Testcontainers for testing

## Prerequisites

- Java 17 or higher
- Maven
- Docker
- Meta Business account (for WhatsApp Business API)
- AWS account (for production DynamoDB)

## Configuration

Create a `.env` file in the project root with:

```properties
# AWS Configuration
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=your_region

# WhatsApp Business API Configuration
WHATSAPP_BUSINESS_PHONE_NUMBER_ID=your_phone_number_id
WHATSAPP_ACCESS_TOKEN=your_access_token
```

## Development

### Running Locally

1. Start local DynamoDB:
```bash
docker-compose up dynamodb-local
```

2. Run the application:
```bash
mvn spring-boot:run
```

### Running Tests
```bash
mvn test
```

### Running with Docker
```bash
docker-compose up
```

### Webhook Configuration

1. Deploy the application to your server
2. Configure the webhook URL in the WhatsApp Business dashboard:
   ```
   https://your-domain/api/webhook
   ```

## API Commands

The chatbot responds to the following WhatsApp commands:

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
```bash
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
   ```bash
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