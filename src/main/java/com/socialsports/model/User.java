package com.socialsports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {
    
    private String userId;
    private String phoneNumber;
    private String name;
    private String email;
    private Integer skillLevel;
    private Integer eventsCreated;
    private Integer eventsJoined;
    private Boolean isPremium;
    private Boolean whatsappLinked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = {"phoneNumber-index"})
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = {"email-index"})
    public String getEmail() {
        return email;
    }
}