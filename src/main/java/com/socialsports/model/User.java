package com.socialsports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {
    
    private String phoneNumber;
    private String name;
    private Integer skillLevel;
    private Integer eventsCreated;
    private Integer eventsJoined;
    private Boolean isPremium;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @DynamoDbPartitionKey
    public String getPhoneNumber() {
        return phoneNumber;
    }
}