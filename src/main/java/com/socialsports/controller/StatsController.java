package com.socialsports.controller;

import com.socialsports.model.PlatformStats;
import com.socialsports.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for platform statistics endpoints
 */
@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Get platform-wide statistics
     * This endpoint does not require authentication
     * 
     * @return PlatformStats object with calculated metrics
     */
    @GetMapping("/stats")
    public ResponseEntity<PlatformStats> getPlatformStats() {
        PlatformStats stats = statsService.getPlatformStats();
        return ResponseEntity.ok(stats);
    }
} 