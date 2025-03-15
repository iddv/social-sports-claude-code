#!/bin/bash

# This script runs the application with the test profile to load test data into the database

# Exit on any error
set -e

echo "Starting the application with test profile to load test data..."

# Check if the DynamoDB container is running
if ! docker ps | grep -q "dynamodb-local"; then
    echo "DynamoDB local container not found. Starting it..."
    docker-compose up -d dynamodb
    
    # Wait for DynamoDB to be ready
    echo "Waiting for DynamoDB to be ready..."
    sleep 5
    
    # Initialize DynamoDB tables if needed
    if [ -f "./scripts/init-dynamodb.sh" ]; then
        echo "Initializing DynamoDB tables..."
        ./scripts/init-dynamodb.sh
    else
        echo "Warning: init-dynamodb.sh script not found. Tables may not be created."
    fi
fi

# Set environment variables for local development
export WHATSAPP_BUSINESS_PHONE_NUMBER_ID=dummy
export WHATSAPP_ACCESS_TOKEN=dummy
export OPENAI_API_KEY=dummy-key

# Run the application with the test profile
echo "Running application with test profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=test

echo "Test data loaded successfully!"
echo "You can now restart the application in the desired profile." 