package com.socialsports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Event {
    
    private String id;
    private SportType sportType;
    private String location;
    private LocalDateTime eventTime;
    private String creatorPhoneNumber;
    private List<String> participantPhoneNumbers;
    private Integer participantLimit;
    private Integer skillLevel;
    private EventStatus status;
    private String whatsappGroupId;
    private Map<String, Boolean> remindersSent;
    private String bookingLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
    
    @DynamoDbSortKey
    public LocalDateTime getEventTime() {
        return eventTime;
    }
}