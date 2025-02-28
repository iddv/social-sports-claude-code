package com.socialsports.integration;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import com.socialsports.model.SportType;
import com.socialsports.model.User;
import com.socialsports.service.EventService;
import com.socialsports.service.UserService;
import com.socialsports.service.WhatsAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
public class UserFlowIntegrationTest {

    @Container
    static GenericContainer<?> dynamoDb = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local:latest"))
            .withExposedPorts(8000);

    @DynamicPropertySource
    static void dynamoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.dynamodb.endpoint", () -> 
            String.format("http://%s:%d", dynamoDb.getHost(), dynamoDb.getFirstMappedPort()));
    }

    // Add test configuration to provide the AWS credentials
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DynamoDbClient dynamoDbClient() {
            return DynamoDbClient.builder()
                .endpointOverride(URI.create(String.format("http://%s:%d", dynamoDb.getHost(), dynamoDb.getFirstMappedPort())))
                .region(Region.EU_WEST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("dummy", "dummy")))
                .build();
        }
        
        @Bean
        @Primary
        public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
            return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        }
    }

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @MockBean
    private WhatsAppService whatsAppService;

    @BeforeEach
    void setUp() {
        // Clean up existing tables if they exist
        try {
            dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("User").build());
        } catch (ResourceNotFoundException ignored) {}
        
        try {
            dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("Event").build());
        } catch (ResourceNotFoundException ignored) {}

        // Create tables
        dynamoDbEnhancedClient.table("User", TableSchema.fromBean(User.class))
                .createTable(builder -> builder
                    .provisionedThroughput(b -> b
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build()));
                        
        dynamoDbEnhancedClient.table("Event", TableSchema.fromBean(Event.class))
                .createTable(builder -> builder
                    .provisionedThroughput(b -> b
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build()));
    }

    @Test
    void testCompleteUserFlow() {
        // Mock WhatsApp notifications to avoid actual API calls
        doNothing().when(whatsAppService).sendEventCreationNotification(any());
        doNothing().when(whatsAppService).sendEventJoinConfirmation(any(), any());
        doNothing().when(whatsAppService).sendEventReminder(any());
        doNothing().when(whatsAppService).sendTextMessage(any(), any());
        when(whatsAppService.createWhatsAppGroup(any(), any())).thenReturn("mock-group-id");

        // 1. Create first user (event creator)
        String creatorPhone = "+1234567890";
        User creator = userService.createUser(creatorPhone, "John Creator");
        
        Optional<User> savedCreator = userService.getUserByPhoneNumber(creatorPhone);
        assertTrue(savedCreator.isPresent());
        assertEquals("John Creator", savedCreator.get().getName());
        assertEquals(0, savedCreator.get().getEventsCreated());

        // 2. Create second user (event participant)
        String participantPhone = "+9876543210";
        User participant = userService.createUser(participantPhone, "Jane Participant");
        
        Optional<User> savedParticipant = userService.getUserByPhoneNumber(participantPhone);
        assertTrue(savedParticipant.isPresent());
        assertEquals("Jane Participant", savedParticipant.get().getName());
        assertEquals(0, savedParticipant.get().getEventsJoined());

        // 3. Creator creates a tennis event
        LocalDateTime eventTime = LocalDateTime.now().plusDays(2); // Event in 2 days
        Event event = eventService.createEvent(
            creatorPhone,
            SportType.TENNIS,
            "Central Tennis Club",
            eventTime,
            4, // participant limit
            3, // skill level
            "http://booking.example.com"
        );

        assertNotNull(event);
        assertEquals(SportType.TENNIS, event.getSportType());
        assertEquals(EventStatus.CREATED, event.getStatus());
        assertEquals(1, event.getParticipantPhoneNumbers().size());
        assertTrue(event.getParticipantPhoneNumbers().contains(creatorPhone));

        // Verify creator's events created count increased
        savedCreator = userService.getUserByPhoneNumber(creatorPhone);
        assertEquals(1, savedCreator.get().getEventsCreated());

        // 4. Participant joins the event
        Event updatedEvent = eventService.joinEvent(event.getId(), participantPhone);

        assertNotNull(updatedEvent);
        assertEquals(2, updatedEvent.getParticipantPhoneNumbers().size());
        assertTrue(updatedEvent.getParticipantPhoneNumbers().contains(participantPhone));
        assertEquals(EventStatus.CONFIRMED, updatedEvent.getStatus());
        assertNotNull(updatedEvent.getWhatsappGroupId());

        // Verify participant's events joined count increased
        savedParticipant = userService.getUserByPhoneNumber(participantPhone);
        assertEquals(1, savedParticipant.get().getEventsJoined());

        // 5. Verify upcoming events include our event
        var upcomingEvents = eventService.getUpcomingEvents();
        assertTrue(upcomingEvents.stream()
                .anyMatch(e -> e.getId().equals(event.getId())));

        // 6. Process reminders (simulating time passing)
        eventService.sendEventReminders();

        // Verify the event still exists and wasn't auto-cancelled
        Optional<Event> finalEvent = eventService.getEventById(event.getId());
        assertTrue(finalEvent.isPresent());
        assertEquals(EventStatus.CONFIRMED, finalEvent.get().getStatus());
    }
}