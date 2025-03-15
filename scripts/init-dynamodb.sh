#!/bin/bash

# Create necessary directories
mkdir -p scripts

# Exit on any error
set -e

echo "Creating DynamoDB tables locally..."

# AWS CLI configuration for local DynamoDB
ENDPOINT_URL="http://localhost:8000"
REGION="eu-west-1"
AWS_ARGS="--endpoint-url $ENDPOINT_URL --region $REGION"

# Create User table
echo "Creating User table..."
aws dynamodb create-table $AWS_ARGS \
    --table-name User \
    --attribute-definitions \
        AttributeName=userId,AttributeType=S \
        AttributeName=phoneNumber,AttributeType=S \
        AttributeName=email,AttributeType=S \
    --key-schema \
        AttributeName=userId,KeyType=HASH \
    --global-secondary-indexes \
        "[
            {
                \"IndexName\": \"phoneNumber-index\",
                \"KeySchema\": [{\"AttributeName\":\"phoneNumber\",\"KeyType\":\"HASH\"}],
                \"Projection\": {\"ProjectionType\":\"ALL\"},
                \"ProvisionedThroughput\": {\"ReadCapacityUnits\":5,\"WriteCapacityUnits\":5}
            },
            {
                \"IndexName\": \"email-index\",
                \"KeySchema\": [{\"AttributeName\":\"email\",\"KeyType\":\"HASH\"}],
                \"Projection\": {\"ProjectionType\":\"ALL\"},
                \"ProvisionedThroughput\": {\"ReadCapacityUnits\":5,\"WriteCapacityUnits\":5}
            }
        ]" \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5

# Create Event table
echo "Creating Event table..."
aws dynamodb create-table $AWS_ARGS \
    --table-name Event \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
        AttributeName=eventTime,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
        AttributeName=eventTime,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5

# Create Community table
echo "Creating Community table..."
aws dynamodb create-table $AWS_ARGS \
    --table-name Community \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5

echo "All tables created successfully!"

# List tables to verify
echo "Listing tables:"
aws dynamodb list-tables $AWS_ARGS