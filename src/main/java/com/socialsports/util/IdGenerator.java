package com.socialsports.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private IdGenerator() {
        // Utility class, no instances
    }

    /**
     * Generates a user-friendly ID for an event
     * Format: SPORT-DATE-RANDOM (e.g., PAD-20230415-A1B2)
     */
    public static String generateEventId(String sportPrefix, LocalDateTime eventTime) {
        String datePart = eventTime.format(DATE_FORMAT);
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        return sportPrefix + "-" + datePart + "-" + randomPart;
    }

    /**
     * Generates a simple community ID
     */
    public static String generateCommunityId() {
        return "COM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}