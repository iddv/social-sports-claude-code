package com.socialsports.repository;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EventRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Event> eventTable;

    public EventRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.eventTable = dynamoDbEnhancedClient.table("Event", TableSchema.fromBean(Event.class));
    }

    public Event save(Event event) {
        eventTable.putItem(event);
        return event;
    }

    public Optional<Event> findById(String id) {
        return eventTable.scan().items()
                .stream()
                .filter(event -> event.getId().equals(id))
                .findFirst();
    }
    
    public List<Event> findUpcomingEvents(LocalDateTime fromDateTime) {
        return eventTable.scan().items()
                .stream()
                .filter(event -> event.getEventTime().isAfter(fromDateTime) &&
                        !event.getStatus().equals(EventStatus.CANCELED))
                .collect(Collectors.toList());
    }
    
    public List<Event> findEventsByCreator(String creatorPhoneNumber) {
        // Note: In a real implementation, this would require a secondary index on creatorPhoneNumber
        // For demo purposes, this simplified approach scans all items
        return eventTable.scan().items()
                .stream()
                .filter(event -> event.getCreatorPhoneNumber().equals(creatorPhoneNumber))
                .collect(Collectors.toList());
    }

    public void delete(Event event) {
        eventTable.deleteItem(event);
    }
    
    /**
     * Find all events in the database.
     * 
     * @return A list of all events
     */
    public List<Event> findAll() {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        return eventTable.scan(scanRequest)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
    
    /**
     * Find events where the specified user is a participant.
     * 
     * @param userId The user ID to filter by
     * @param fromDateTime Optional date to filter for events after this time
     * @return A list of events where the user is a participant
     */
    public List<Event> findEventsByParticipant(String userId, LocalDateTime fromDateTime) {
        return eventTable.scan().items()
                .stream()
                .filter(event -> event.getParticipantPhoneNumbers().contains(userId))
                .filter(event -> fromDateTime == null || event.getEventTime().isAfter(fromDateTime))
                .filter(event -> !event.getStatus().equals(EventStatus.CANCELED))
                .collect(Collectors.toList());
    }
}