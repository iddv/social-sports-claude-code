package com.socialsports.controller;

import com.socialsports.model.Event;
import com.socialsports.model.User;
import com.socialsports.repository.EventRepository;
import com.socialsports.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for test data related endpoints.
 * This controller is only active in the "test" profile.
 */
@RestController
@RequestMapping("/api/test-data")
@Profile("test")
public class TestDataController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public TestDataController(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Get a summary of loaded test data.
     *
     * @return A summary of the test data in the system
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getTestDataSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Get all users
        List<User> users = userRepository.findAll();
        summary.put("totalUsers", users.size());
        
        // Get all events
        List<Event> events = eventRepository.findAll();
        summary.put("totalEvents", events.size());
        
        // Count events by status
        Map<String, Long> eventsByStatus = new HashMap<>();
        events.forEach(event -> {
            String status = event.getStatus().name();
            eventsByStatus.put(status, eventsByStatus.getOrDefault(status, 0L) + 1);
        });
        summary.put("eventsByStatus", eventsByStatus);
        
        // Count events by sport type
        Map<String, Long> eventsBySportType = new HashMap<>();
        events.forEach(event -> {
            String sportType = event.getSportType().name();
            eventsBySportType.put(sportType, eventsBySportType.getOrDefault(sportType, 0L) + 1);
        });
        summary.put("eventsBySportType", eventsBySportType);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Get all test users.
     *
     * @return A list of all test users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllTestUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Get all test events.
     *
     * @return A list of all test events
     */
    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllTestEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }
} 