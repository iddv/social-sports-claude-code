package com.socialsports.controller;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import com.socialsports.model.SportType;
import com.socialsports.service.EventService;
import com.socialsports.service.LLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Events", description = "Event management endpoints")
@CrossOrigin(origins = "*") // Enable CORS for frontend integration
public class EventController {

    private final EventService eventService;
    private final LLMService llmService;

    @Operation(summary = "Get all upcoming events with pagination and filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved all upcoming events")
    })
    @GetMapping
    public ResponseEntity<List<Event>> getUpcomingEvents(
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) Integer skillLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<Event> events = eventService.getUpcomingEvents(sportType, skillLevel, page, size);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get event by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(
            @Parameter(description = "ID of the event to fetch") @PathVariable String eventId) {
        
        Optional<Event> event = eventService.getEventById(eventId);
        return event.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Map<String, Object> eventData) {
        try {
            String creatorId = (String) eventData.get("createdBy");
            SportType sportType = SportType.valueOf((String) eventData.get("sport"));
            String location = (String) eventData.get("location");
            LocalDateTime eventTime = LocalDateTime.parse((String) eventData.get("date"));
            Integer maxPlayers = (Integer) eventData.get("maxPlayers");
            Integer skillLevel = (Integer) eventData.get("skillLevel");
            String bookingUrl = (String) eventData.get("bookingUrl");
            
            Event event = eventService.createEvent(
                creatorId, sportType, location, eventTime, 
                maxPlayers, skillLevel, bookingUrl
            );
            
            return new ResponseEntity<>(event, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating event", e);
            throw new IllegalArgumentException("Invalid event data: " + e.getMessage());
        }
    }

    @Operation(summary = "Process natural language event request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully parsed the natural language request"),
        @ApiResponse(responseCode = "400", description = "Failed to parse the request")
    })
    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseEventRequest(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            Map<String, Object> parsedEvent = llmService.parseNaturalLanguageEventRequest(message);
            return ResponseEntity.ok(parsedEvent);
        } catch (Exception e) {
            log.error("Error parsing natural language event request", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Join an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined the event"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "400", description = "Cannot join the event")
    })
    @PostMapping("/{eventId}/join")
    public ResponseEntity<Event> joinEvent(
            @Parameter(description = "ID of the event to join") @PathVariable String eventId,
            @RequestBody Map<String, String> requestBody) {
        
        try {
            String userId = requestBody.get("userId");
            Event joinedEvent = eventService.joinEvent(eventId, userId);
            return ResponseEntity.ok(joinedEvent);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Cancel an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully canceled the event"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel the event")
    })
    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<Event> cancelEvent(
            @Parameter(description = "ID of the event to cancel") @PathVariable String eventId,
            @RequestBody Map<String, String> requestBody) {
        
        try {
            String reason = requestBody.get("reason");
            Event canceledEvent = eventService.cancelEvent(eventId, reason);
            return ResponseEntity.ok(canceledEvent);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Get available sport types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved all sport types")
    })
    @GetMapping("/sport-types")
    public ResponseEntity<SportType[]> getSportTypes() {
        return ResponseEntity.ok(SportType.values());
    }

    /**
     * Get events for the current user (events where the user is a participant)
     * 
     * @param userId ID of the user to get events for
     * @param sportType Optional sport type filter
     * @param skillLevel Optional skill level filter
     * @param page Page number for pagination
     * @param size Page size for pagination
     * @return List of events the user is participating in
     */
    @Operation(summary = "Get events for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved user events successfully")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Event>> getUserEvents(
            @Parameter(description = "ID of the user to get events for") @PathVariable String userId,
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) Integer skillLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<Event> events = eventService.getUserEvents(userId, sportType, skillLevel, page, size);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get events for the currently authenticated user
     * 
     * @param sportType Optional sport type filter
     * @param skillLevel Optional skill level filter
     * @param page Page number for pagination
     * @param size Page size for pagination
     * @return List of events the current user is participating in
     */
    @Operation(summary = "Get events for the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved current user events successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized, user not authenticated")
    })
    @GetMapping("/my-events")
    public ResponseEntity<List<Event>> getCurrentUserEvents(
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) Integer skillLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String userId = authentication.getName();
        List<Event> events = eventService.getUserEvents(userId, sportType, skillLevel, page, size);
        return ResponseEntity.ok(events);
    }
} 