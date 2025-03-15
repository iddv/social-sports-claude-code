# Test Data for Social Sports App

This document describes the test data loading script for the Social Sports app, which creates realistic Dutch-focused sports events and user profiles for testing purposes.

## Overview

The test data generator creates:
- 15 user profiles with Dutch and international names
- 30 events across various sport types with realistic Dutch locations
- Different event statuses (CONFIRMED, CANCELED, COMPLETED)
- Events distributed across past, present, and future dates

## How to Load Test Data

The test data is loaded using a special test profile. Follow these steps to load the test data:

1. Make sure you have DynamoDB Local running (or a real DynamoDB instance if preferred)
2. Run the test data loading script:

```bash
# Make the script executable (if needed)
chmod +x scripts/load-test-data.sh

# Run the script
./scripts/load-test-data.sh
```

The script will:
- Start DynamoDB Local if it's not already running
- Initialize the DynamoDB tables using the init-dynamodb.sh script
- Run the application with the "test" profile to load the test data
- Exit after the data has been loaded

## Accessing Test Data

Once the test data is loaded, you can access it using these endpoints (when running with the "test" profile):

- GET `/api/test-data/summary` - Get a summary of all test data (counts, distributions)
- GET `/api/test-data/users` - Get all test users
- GET `/api/test-data/events` - Get all test events

## Checking Test Data Using CLI

You can also check the DynamoDB tables directly using the provided command-line tool:

```bash
# Make the script executable (if needed)
chmod +x scripts/check-dynamodb.sh

# Show help and available commands
./scripts/check-dynamodb.sh --help

# List all tables
./scripts/check-dynamodb.sh --list

# Count items in a specific table
./scripts/check-dynamodb.sh --count User

# View items in a table (default limit: 10)
./scripts/check-dynamodb.sh --scan Event

# View items in a table with custom limit
./scripts/check-dynamodb.sh --scan Event 20

# Show table details (schema, indexes, etc.)
./scripts/check-dynamodb.sh --describe User

# Show overview of all tables
./scripts/check-dynamodb.sh --all
```

This CLI tool is useful for:
- Verifying test data was loaded correctly
- Debugging data-related issues
- Checking table schemas and configurations
- Exploring the content of DynamoDB tables

## Test Data Structure

### Users

The generated test users have:
- Dutch and international names common in expat communities
- Dutch-format phone numbers (+31 6 12345678)
- Various skill levels (1-5)
- Different registration dates

### Events

The generated events have:
- Multiple sport types (PADEL, FOOTBALL, TENNIS, etc.)
- Real Dutch venue names and addresses
- Distribution of events:
  - 25% past events (COMPLETED)
  - 10% current events (today/tomorrow, CONFIRMED)
  - 40% near future events (next 2 weeks, CONFIRMED)
  - 25% far future events (next 1-2 months, CONFIRMED)
  - 4 additional CANCELED events

- Event fullness distribution:
  - 20% full events
  - 30% nearly full events (1-2 spots remaining)
  - 30% half full events
  - 20% new/empty events

## Running the Application with Test Data

After loading the test data, you can run the application normally to use it:

```bash
# Normal startup (development)
./mvnw spring-boot:run
```

Or with test profile to continue seeing the test data endpoints:

```bash
# Startup with test profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

## Notes

- The test data is focused on sports and locations popular in the Netherlands, especially for expats
- The data reflects European date/time formats
- The test data includes a mix of WhatsApp group links and booking URLs to test all scenarios
- The test data is loaded fresh each time you run the script; existing data will be overwritten