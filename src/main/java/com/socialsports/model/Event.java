package com.socialsports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Event {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private String id;
    private SportType sportType;
    private String location;
    private LocalDateTime eventTime;
    private String eventTimeString; // For DynamoDB sort key
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
    public String getEventTimeString() {
        if (eventTimeString == null && eventTime != null) {
            eventTimeString = eventTime.format(FORMATTER);
        }
        return eventTimeString;
    }
    
    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
        this.eventTimeString = eventTime != null ? eventTime.format(FORMATTER) : null;
    }
    
    public void setEventTimeString(String eventTimeString) {
        this.eventTimeString = eventTimeString;
        this.eventTime = eventTimeString != null ? LocalDateTime.parse(eventTimeString, FORMATTER) : null;
    }
}