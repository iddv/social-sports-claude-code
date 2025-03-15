package com.socialsports.controller;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import com.socialsports.model.SportType;
import com.socialsports.service.EventService;
import com.socialsports.service.LLMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private LLMService llmService;

    @InjectMocks
    private EventController eventController;

    private Event testEvent;
    private static final String TEST_EVENT_ID = "test-event-123";
    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_LOCATION = "Test Location";
    private static final SportType TEST_SPORT_TYPE = SportType.TENNIS;
    private static final int TEST_SKILL_LEVEL = 3;
    private static final int TEST_MAX_PLAYERS = 4;

    @BeforeEach
    void setUp() {
        LocalDateTime eventTime = LocalDateTime.now().plusDays(1);
        
        testEvent = Event.builder()
                .id(TEST_EVENT_ID)
                .sportType(TEST_SPORT_TYPE)
                .location(TEST_LOCATION)
                .eventTime(eventTime)
                .creatorPhoneNumber("+1234567890")
                .participantPhoneNumbers(Arrays.asList(TEST_USER_ID))
                .participantLimit(TEST_MAX_PLAYERS)
                .skillLevel(TEST_SKILL_LEVEL)
                .status(EventStatus.CREATED)
                .remindersSent(new HashMap<>())
                .bookingLink("http://example.com/booking")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetUpcomingEvents_WithFiltering() {
        // Prepare test data
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getUpcomingEvents(eq(TEST_SPORT_TYPE), eq(TEST_SKILL_LEVEL), anyInt(), anyInt()))
                .thenReturn(events);

        // Execute the controller method
        ResponseEntity<List<Event>> response = eventController.getUpcomingEvents(
                TEST_SPORT_TYPE, TEST_SKILL_LEVEL, 0, 10);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getUpcomingEvents(TEST_SPORT_TYPE, TEST_SKILL_LEVEL, 0, 10);
    }

    @Test
    void testGetUpcomingEvents_NoFilters() {
        // Prepare test data
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getUpcomingEvents(null, null, 0, 10))
                .thenReturn(events);

        // Execute the controller method
        ResponseEntity<List<Event>> response = eventController.getUpcomingEvents(
                null, null, 0, 10);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getUpcomingEvents(null, null, 0, 10);
    }

    @Test
    void testGetEventById_Success() {
        when(eventService.getEventById(TEST_EVENT_ID)).thenReturn(Optional.of(testEvent));

        // Execute the controller method
        ResponseEntity<Event> response = eventController.getEventById(TEST_EVENT_ID);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEvent, response.getBody());
        verify(eventService).getEventById(TEST_EVENT_ID);
    }

    @Test
    void testGetEventById_NotFound() {
        when(eventService.getEventById(TEST_EVENT_ID)).thenReturn(Optional.empty());

        // Execute the controller method
        ResponseEntity<Event> response = eventController.getEventById(TEST_EVENT_ID);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(eventService).getEventById(TEST_EVENT_ID);
    }

    @Test
    void testCreateEvent_Success() {
        // Prepare test data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("createdBy", TEST_USER_ID);
        eventData.put("sport", TEST_SPORT_TYPE.toString());
        eventData.put("location", TEST_LOCATION);
        eventData.put("date", LocalDateTime.now().plusDays(1).toString());
        eventData.put("maxPlayers", TEST_MAX_PLAYERS);
        eventData.put("skillLevel", TEST_SKILL_LEVEL);
        eventData.put("bookingUrl", "http://example.com/booking");

        when(eventService.createEvent(
                eq(TEST_USER_ID),
                eq(TEST_SPORT_TYPE),
                eq(TEST_LOCATION),
                any(LocalDateTime.class),
                eq(TEST_MAX_PLAYERS),
                eq(TEST_SKILL_LEVEL),
                anyString()
        )).thenReturn(testEvent);

        // Execute the controller method
        ResponseEntity<Event> response = eventController.createEvent(eventData);

        // Verify the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testEvent, response.getBody());
        verify(eventService).createEvent(
                eq(TEST_USER_ID),
                eq(TEST_SPORT_TYPE),
                eq(TEST_LOCATION),
                any(LocalDateTime.class),
                eq(TEST_MAX_PLAYERS),
                eq(TEST_SKILL_LEVEL),
                anyString()
        );
    }

    @Test
    void testCreateEvent_Exception() {
        // Prepare test data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("createdBy", TEST_USER_ID);
        eventData.put("sport", TEST_SPORT_TYPE.toString());
        eventData.put("location", TEST_LOCATION);
        eventData.put("date", LocalDateTime.now().plusDays(1).toString());
        eventData.put("maxPlayers", TEST_MAX_PLAYERS);
        eventData.put("skillLevel", TEST_SKILL_LEVEL);
        eventData.put("bookingUrl", "http://example.com/booking");

        when(eventService.createEvent(
                any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("Test exception"));

        // Execute the controller method and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventController.createEvent(eventData);
        });

        assertTrue(exception.getMessage().contains("Test exception"));
    }

    @Test
    void testJoinEvent_Success() {
        // Prepare test data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", TEST_USER_ID);

        when(eventService.joinEvent(TEST_EVENT_ID, TEST_USER_ID)).thenReturn(testEvent);

        // Execute the controller method
        ResponseEntity<Event> response = eventController.joinEvent(TEST_EVENT_ID, requestBody);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEvent, response.getBody());
        verify(eventService).joinEvent(TEST_EVENT_ID, TEST_USER_ID);
    }

    @Test
    void testJoinEvent_NotFound() {
        // Prepare test data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", TEST_USER_ID);

        when(eventService.joinEvent(TEST_EVENT_ID, TEST_USER_ID))
                .thenThrow(new NoSuchElementException("Event not found"));

        // Execute the controller method
        ResponseEntity<Event> response = eventController.joinEvent(TEST_EVENT_ID, requestBody);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(eventService).joinEvent(TEST_EVENT_ID, TEST_USER_ID);
    }

    @Test
    void testJoinEvent_BadRequest() {
        // Prepare test data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", TEST_USER_ID);

        when(eventService.joinEvent(TEST_EVENT_ID, TEST_USER_ID))
                .thenThrow(new IllegalStateException("Event is full"));

        // Execute the controller method
        ResponseEntity<Event> response = eventController.joinEvent(TEST_EVENT_ID, requestBody);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(eventService).joinEvent(TEST_EVENT_ID, TEST_USER_ID);
    }

    @Test
    void testCancelEvent_Success() {
        // Prepare test data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", "Test reason");

        when(eventService.cancelEvent(TEST_EVENT_ID, "Test reason")).thenReturn(testEvent);

        // Execute the controller method
        ResponseEntity<Event> response = eventController.cancelEvent(TEST_EVENT_ID, requestBody);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEvent, response.getBody());
        verify(eventService).cancelEvent(TEST_EVENT_ID, "Test reason");
    }

    @Test
    void testCancelEvent_NotFound() {
        // Prepare test data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", "Test reason");

        when(eventService.cancelEvent(TEST_EVENT_ID, "Test reason"))
                .thenThrow(new NoSuchElementException("Event not found"));

        // Execute the controller method
        ResponseEntity<Event> response = eventController.cancelEvent(TEST_EVENT_ID, requestBody);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(eventService).cancelEvent(TEST_EVENT_ID, "Test reason");
    }

    @Test
    void testCancelEvent_BadRequest() {
        // Prepare test data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", "Test reason");

        when(eventService.cancelEvent(TEST_EVENT_ID, "Test reason"))
                .thenThrow(new IllegalStateException("Event already canceled"));

        // Execute the controller method
        ResponseEntity<Event> response = eventController.cancelEvent(TEST_EVENT_ID, requestBody);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(eventService).cancelEvent(TEST_EVENT_ID, "Test reason");
    }

    @Test
    void testParseEventRequest_Success() {
        // Prepare test data
        Map<String, String> request = new HashMap<>();
        request.put("message", "I want to play tennis tomorrow at 3pm");

        Map<String, Object> parsedEvent = new HashMap<>();
        parsedEvent.put("sportType", TEST_SPORT_TYPE);
        parsedEvent.put("time", LocalDateTime.now().plusDays(1).withHour(15));

        when(llmService.parseNaturalLanguageEventRequest("I want to play tennis tomorrow at 3pm"))
                .thenReturn(parsedEvent);

        // Execute the controller method
        ResponseEntity<Map<String, Object>> response = eventController.parseEventRequest(request);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(parsedEvent, response.getBody());
        verify(llmService).parseNaturalLanguageEventRequest("I want to play tennis tomorrow at 3pm");
    }

    @Test
    void testParseEventRequest_Exception() {
        // Prepare test data
        Map<String, String> request = new HashMap<>();
        request.put("message", "I want to play tennis tomorrow at 3pm");

        when(llmService.parseNaturalLanguageEventRequest("I want to play tennis tomorrow at 3pm"))
                .thenThrow(new RuntimeException("Failed to parse"));

        // Execute the controller method
        ResponseEntity<Map<String, Object>> response = eventController.parseEventRequest(request);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(llmService).parseNaturalLanguageEventRequest("I want to play tennis tomorrow at 3pm");
    }

    @Test
    void testGetSportTypes() {
        // Execute the controller method
        ResponseEntity<SportType[]> response = eventController.getSportTypes();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SportType.values().length, response.getBody().length);
        assertTrue(Arrays.asList(response.getBody()).contains(SportType.TENNIS));
    }
} 