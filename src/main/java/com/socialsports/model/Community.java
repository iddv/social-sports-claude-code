package com.socialsports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Community {
    
    private String id;
    private String name;
    private List<String> adminPhoneNumbers;
    private List<String> memberPhoneNumbers;
    private String whatsappChannelId;
    private List<SportType> supportedSports;
    private String rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}