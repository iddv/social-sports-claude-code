# SocialSports WhatsApp Chatbot

A Spring Boot application that facilitates sports event organization through WhatsApp.

## Features

- Create and manage sports events using natural language
- Support for multiple sports with specific requirements:
  - Padel (2-4 players, requires booking)
  - Tennis (2-4 players, requires booking)
  - Football (6-22 players)
  - Basketball (6-10 players)
  - Volleyball (6-12 players)
  - Squash (2 players, requires booking)
  - Badminton (2-4 players, requires booking)
  - Table Tennis (2-4 players, requires booking)
  - Golf (1-4 players)
  - Climbing (2-8 players)
- Join existing events
- Automatic WhatsApp group creation
- Event reminders
- Participant management
- Premium user features
- Natural language processing for event creation
- Sport-specific requirements and equipment tracking
- JWT-based authentication for API security
- Test data generation for development and testing

## Tech Stack

- Java 17
- Spring Boot 3.x
- AWS DynamoDB
- WhatsApp Business API for messaging
- OpenAI GPT-4 for natural language processing
- Spring Security with JWT authentication
- Docker & Docker Compose
- Testcontainers for testing
- Swagger/OpenAPI for API documentation

## Prerequisites

- Java 17 or higher
- Maven
- Docker
- Meta Business account (for WhatsApp Business API)
- AWS account (for production DynamoDB)
- OpenAI API key (for natural language processing)

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

# OpenAI Configuration
OPENAI_API_KEY=your_openai_api_key

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400
```

## Development

### Running Locally

1. Start local DynamoDB:
```bash
docker-compose up dynamodb-local
```

2. Initialize DynamoDB tables (first time only):
```bash
chmod +x scripts/init-dynamodb.sh
./scripts/init-dynamodb.sh
```

3. Run the application:
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

## Running the Application

### Local Development

For local development, we provide an `application-local.properties` file with hardcoded test values for required environment variables. 

1. Start local DynamoDB first:
```bash
docker-compose up dynamodb-local
```

2. If this is your first time running, initialize the required tables:
```bash
chmod +x scripts/init-dynamodb.sh
./scripts/init-dynamodb.sh
```

3. Run the application with:
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Setting Up LocalTunnel for Frontend Testing

When developing locally, the frontend is typically running on a separate port (e.g., port 3000) and needs to make API calls to the backend. To expose your local backend server to the internet for testing:

1. Install LocalTunnel globally or use npx:
```bash
# Install globally
npm install -g localtunnel

# Or use npx (no installation required)
npx localtunnel
```

2. Start the backend server on port 8080:
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

3. In a separate terminal, create a tunnel to expose your local server:
```bash
# Using globally installed localtunnel
lt --port 8080

# Using npx
npx localtunnel --port 8080
```

4. LocalTunnel will provide a public URL that you can use in your frontend environment variables:
```
your url is: https://xyz-abc-123.loca.lt
```

5. Update your frontend environment variables to use this URL as the API base URL:
```
API_BASE_URL=https://xyz-abc-123.loca.lt
```

Note: The LocalTunnel URL changes each time you restart the tunnel, so you'll need to update your environment variables accordingly.

### Database Initialization and Test Data

For a complete development environment, you'll need to:

1. Start DynamoDB local:
```bash
docker-compose up -d dynamodb-local
```

2. Create required tables:
```bash
chmod +x scripts/init-dynamodb.sh
./scripts/init-dynamodb.sh
```

3. Load test data (optional):
```bash
chmod +x scripts/load-test-data.sh
./scripts/load-test-data.sh
```

4. Verify your data (optional):
```bash
chmod +x scripts/check-dynamodb.sh
./scripts/check-dynamodb.sh --scan User
./scripts/check-dynamodb.sh --scan Event
```

### Running Tests

Tests use `application-test.properties` configuration file:

```bash
./mvnw test -Dspring.profiles.active=test
```

## Running the Application

### Local Development

For local development, we provide an `application-local.properties` file with hardcoded test values for required environment variables. 

1. Start local DynamoDB first:
```bash
docker-compose up dynamodb-local
```

2. If this is your first time running, initialize the required tables:
```bash
chmod +x scripts/init-dynamodb.sh
./scripts/init-dynamodb.sh
```

3. Run the application with:
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Running Tests

Tests use `application-test.properties` configuration file:

```bash
./mvnw test -Dspring.profiles.active=test
```

### Production Deployment

In production, the application uses environment variables defined in the deployment environment (e.g. AWS Amplify):
- `WHATSAPP_BUSINESS_PHONE_NUMBER_ID`
- `WHATSAPP_ACCESS_TOKEN`
- `OPENAI_API_KEY`

## API Commands

The chatbot now supports two ways to create events:

### Natural Language Commands
You can create events using natural language, for example:
- "I want to play tennis tomorrow at 3pm at Central Courts with 3 other people"
- "Looking for 10 people to play football at City Park at 18:30"
- "Need 2 players for squash at Fitness Club this evening at 7pm"

### Structured Commands
The traditional command format is still supported:

- `CREATE EVENT [sport] AT [location] ON [date] FOR [number] PLAYERS SKILL [1-5] BOOKING [optional-url]`
  - Creates a new sports event
  - Example: `CREATE EVENT PADEL AT City Sports Club ON 2023-04-15 18:30 FOR 4 PLAYERS SKILL 3 BOOKING http://example.com`

Other commands:
- `JOIN [eventId]` - Join an existing sports event
- `LEAVE [eventId]` - Leave an event you have joined
- `CANCEL [eventId]` - Cancel an event (creator only)
- `EVENTS` - List all upcoming events
- `HELP` - Display available commands

## Sport-Specific Features

Each sport has specific requirements and constraints:

1. Court Sports (Tennis, Padel, Squash, Badminton):
   - Require venue booking
   - Limited player count
   - Equipment tracking (rackets, balls, etc.)

2. Team Sports (Football, Basketball, Volleyball):
   - Larger player counts
   - Optional venue booking
   - Basic equipment needs

3. Specialty Sports (Golf, Climbing):
   - Specific equipment requirements
   - Venue-dependent
   - Special skill considerations

## Development

### Testing
The application includes comprehensive tests for:
- Sport type validation
- Natural language processing
- Event creation and management
- Time and date parsing
- Player count validation

Run the tests with:
```bash
mvn test
```

## Test Data Generation

For development and testing, we provide a test data generation tool that creates realistic Dutch-focused sports events and user profiles.

### Loading Test Data

```bash
# Make the script executable
chmod +x scripts/load-test-data.sh

# Run the script
./scripts/load-test-data.sh
```

### Checking Test Data

You can check the generated test data using the provided CLI tool:

```bash
# Make the script executable
chmod +x scripts/check-dynamodb.sh

# Show help and available commands
./scripts/check-dynamodb.sh --help

# View items in a table
./scripts/check-dynamodb.sh --scan Event
```

For more details, see the [TEST_DATA_README.md](TEST_DATA_README.md) file.

## Security

The application implements JWT-based authentication for API security:

- User registration and login endpoints are publicly accessible
- All other endpoints require JWT authentication
- JWT tokens are validated for each request
- Tokens have a configurable expiration time

### API Authentication

To use protected endpoints:

1. Register a user: `POST /api/users/register`
2. Login to get a JWT token: `POST /api/users/login`