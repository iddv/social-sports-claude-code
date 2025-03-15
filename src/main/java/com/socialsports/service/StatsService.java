package com.socialsports.service;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import com.socialsports.model.PlatformStats;
import com.socialsports.model.SportType;
import com.socialsports.repository.EventRepository;
import com.socialsports.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    
    // Cache the calculated stats for 6 hours to minimize database load
    private PlatformStats cachedStats;
    private LocalDateTime lastCacheUpdate;
    private static final long CACHE_DURATION_HOURS = 6;

    public StatsService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get platform statistics with caching to reduce database load
     * @return PlatformStats object with calculated metrics
     */
    public PlatformStats getPlatformStats() {
        if (cachedStats == null || 
            lastCacheUpdate == null || 
            ChronoUnit.HOURS.between(lastCacheUpdate, LocalDateTime.now()) >= CACHE_DURATION_HOURS) {
            refreshStats();
        }
        return cachedStats;
    }

    /**
     * Refresh statistics cache
     * This will be called either on-demand or by the scheduled job
     */
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // Refresh cache every 6 hours
    public synchronized void refreshStats() {
        PlatformStats stats = calculateStats();
        this.cachedStats = stats;
        this.lastCacheUpdate = LocalDateTime.now();
    }

    /**
     * Calculate all platform statistics
     * @return PlatformStats with current metrics
     */
    private PlatformStats calculateStats() {
        List<Event> allEvents = eventRepository.findAll();
        
        // Calculate active players (users who joined at least one event in last 90 days)
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        Set<String> activePlayers = allEvents.stream()
            .filter(e -> e.getEventTime().isAfter(ninetyDaysAgo))
            .filter(e -> !e.getStatus().equals(EventStatus.CANCELED))
            .flatMap(e -> e.getParticipantPhoneNumbers().stream())
            .collect(Collectors.toSet());
        
        // Calculate average games per week over last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Event> recentEvents = allEvents.stream()
            .filter(e -> e.getEventTime().isAfter(thirtyDaysAgo) && e.getEventTime().isBefore(LocalDateTime.now()))
            .filter(e -> !e.getStatus().equals(EventStatus.CANCELED))
            .collect(Collectors.toList());
        
        double gamesPerWeek = 0.0;
        if (!recentEvents.isEmpty()) {
            double weeksInPeriod = 30.0 / 7.0;
            gamesPerWeek = recentEvents.size() / weeksInPeriod;
        }
        
        // Count unique venues from past 180 days
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusDays(180);
        Set<String> uniqueVenues = allEvents.stream()
            .filter(e -> e.getEventTime().isAfter(sixMonthsAgo))
            .filter(e -> !e.getStatus().equals(EventStatus.CANCELED))
            .map(Event::getLocation)
            .collect(Collectors.toSet());
        
        // For now, we don't have reviews, so we'll use the fallback value
        double playerRating = 4.8;
        
        return PlatformStats.builder()
                .activePlayers(activePlayers.size())
                .gamesWeekly(Math.round(gamesPerWeek * 10) / 10.0) // Round to 1 decimal place
                .padelVenues(uniqueVenues.size())
                .playerRating(playerRating)
                .build();
    }
} 