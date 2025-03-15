#!/bin/bash

# Script to check DynamoDB tables locally
# This allows you to verify the content of your DynamoDB tables

# Configuration
ENDPOINT_URL="http://localhost:8000"
REGION="eu-west-1"
AWS_ARGS="--endpoint-url $ENDPOINT_URL --region $REGION"

# Text colors for better readability
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check dependencies
check_dependencies() {
  if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed or not in PATH${NC}"
    echo "Please install AWS CLI. See: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
    exit 1
  fi
  
  if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}Warning: jq is not installed. Some formatting features will be limited.${NC}"
    echo "For better output formatting, install jq: 'sudo apt-get install jq' (Debian/Ubuntu)"
    JQ_INSTALLED=false
  else
    JQ_INSTALLED=true
  fi
}

# Function to check if DynamoDB is running
check_dynamodb() {
  echo -e "${BLUE}Checking if DynamoDB is running...${NC}"
  if ! aws dynamodb list-tables $AWS_ARGS &>/dev/null; then
    echo -e "${RED}Error: DynamoDB is not running at $ENDPOINT_URL${NC}"
    echo "Please start DynamoDB with 'docker-compose up -d dynamodb' and try again."
    exit 1
  fi
  echo -e "${GREEN}DynamoDB is running.${NC}"
}

# Function to list all tables
list_tables() {
  echo -e "${BLUE}Listing DynamoDB tables:${NC}"
  aws dynamodb list-tables $AWS_ARGS --output table
}

# Function to count items in a table
count_items() {
  local table=$1
  echo -e "${BLUE}Counting items in table '$table':${NC}"
  
  # Using scan with Select=COUNT to get the item count
  local count_output=$(aws dynamodb scan $AWS_ARGS \
    --table-name $table \
    --select COUNT \
    --output json)
  
  if [ "$JQ_INSTALLED" = true ]; then
    local count=$(echo "$count_output" | jq '.Count')
  else
    # Extract the count value using grep and sed if jq is not available
    local count=$(echo "$count_output" | grep -o '"Count": [0-9]*' | sed 's/"Count": //')
    if [ -z "$count" ]; then
      count="0 (raw JSON: $count_output)"
    fi
  fi
  
  echo -e "${GREEN}Total items in $table: $count${NC}"
}

# Function to scan a table
scan_table() {
  local table=$1
  local limit=$2
  
  if [ -z "$limit" ]; then
    limit=10
  fi
  
  echo -e "${BLUE}Scanning table '$table' (limit: $limit items):${NC}"
  local output=$(aws dynamodb scan $AWS_ARGS \
    --table-name $table \
    --max-items $limit \
    --output json)
  
  if [ "$JQ_INSTALLED" = true ]; then
    echo "$output" | jq '.'
  else
    echo "$output"
  fi
    
  echo -e "${GREEN}End of items for table $table${NC}"
}

# Function to show detailed info about a table
describe_table() {
  local table=$1
  echo -e "${BLUE}Table details for '$table':${NC}"
  local output=$(aws dynamodb describe-table $AWS_ARGS \
    --table-name $table \
    --output json)
  
  if [ "$JQ_INSTALLED" = true ]; then
    echo "$output" | jq '.'
  else
    echo "$output"
  fi
}

# Function to display help message
show_help() {
  echo -e "${YELLOW}Usage:${NC}"
  echo "  ./scripts/check-dynamodb.sh [options]"
  echo
  echo -e "${YELLOW}Options:${NC}"
  echo "  -h, --help              Show this help message"
  echo "  -l, --list              List all tables"
  echo "  -c, --count [TABLE]     Count items in a specific table or all tables if not specified"
  echo "  -s, --scan TABLE [LIMIT] Scan a table (default limit: 10 items)"
  echo "  -d, --describe TABLE    Show detailed information about a table"
  echo "  -a, --all               Show all information (list tables, count items, scan all tables)"
  echo
  echo -e "${YELLOW}Examples:${NC}"
  echo "  ./scripts/check-dynamodb.sh --list"
  echo "  ./scripts/check-dynamodb.sh --count User"
  echo "  ./scripts/check-dynamodb.sh --scan Event 20"
  echo "  ./scripts/check-dynamodb.sh --describe User"
  echo "  ./scripts/check-dynamodb.sh --all"
}

# Main execution flow
main() {
  # Check dependencies
  check_dependencies
  
  # First check if DynamoDB is running
  check_dynamodb
  
  # If no arguments, show help
  if [ $# -eq 0 ]; then
    show_help
    exit 0
  fi
  
  # Process command line arguments
  while [[ $# -gt 0 ]]; do
    case $1 in
      -h|--help)
        show_help
        exit 0
        ;;
      -l|--list)
        list_tables
        shift
        ;;
      -c|--count)
        if [ -n "$2" ] && [[ $2 != -* ]]; then
          count_items "$2"
          shift 2
        else
          # If no table specified, count all tables
          echo -e "${BLUE}Counting items in all tables:${NC}"
          tables=$(aws dynamodb list-tables $AWS_ARGS --output text --query 'TableNames[*]')
          for table in $tables; do
            count_items "$table"
          done
          shift
        fi
        ;;
      -s|--scan)
        if [ -z "$2" ] || [[ $2 == -* ]]; then
          echo -e "${RED}Error: Table name is required for scan operation${NC}"
          exit 1
        fi
        table=$2
        limit=$3
        if [[ $limit == -* ]] || [ -z "$limit" ]; then
          limit=10
          scan_table "$table" "$limit"
          shift 2
        else
          scan_table "$table" "$limit"
          shift 3
        fi
        ;;
      -d|--describe)
        if [ -z "$2" ] || [[ $2 == -* ]]; then
          echo -e "${RED}Error: Table name is required for describe operation${NC}"
          exit 1
        fi
        describe_table "$2"
        shift 2
        ;;
      -a|--all)
        list_tables
        echo
        
        tables=$(aws dynamodb list-tables $AWS_ARGS --output text --query 'TableNames[*]')
        for table in $tables; do
          count_items "$table"
          scan_table "$table" 5
          echo
        done
        shift
        ;;
      *)
        echo -e "${RED}Unknown option: $1${NC}"
        show_help
        exit 1
        ;;
    esac
  done
}

# Execute main function
main "$@" 