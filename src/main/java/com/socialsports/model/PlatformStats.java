package com.socialsports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents platform-wide statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStats {
    private int activePlayers;     // Unique users who joined at least one event in the last 90 days
    private double gamesWeekly;    // Average number of events per week over the last 30 days
    private int padelVenues;       // Unique venues that hosted events in the last 180 days
    private double playerRating;   // Average rating from all player reviews (with fallback to 4.8)
} 