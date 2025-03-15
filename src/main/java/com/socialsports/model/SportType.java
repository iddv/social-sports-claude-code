package com.socialsports.model;

import lombok.Getter;

@Getter
public enum SportType {
    PADEL(2, 4, true, "Indoor/Outdoor court sport similar to tennis", new String[]{"Racket", "Ball"}),
    TENNIS(2, 4, true, "Court sport played with rackets and ball", new String[]{"Racket", "Ball"}),
    FOOTBALL(6, 22, false, "Team sport played with a ball", new String[]{"Ball", "Boots"}),
    BASKETBALL(6, 10, false, "Team sport played on a court", new String[]{"Ball"}),
    VOLLEYBALL(6, 12, false, "Team sport played over a net", new String[]{"Ball"}),
    SQUASH(2, 2, true, "Indoor court sport", new String[]{"Racket", "Ball"}),
    BADMINTON(2, 4, true, "Indoor court sport with shuttlecock", new String[]{"Racket", "Shuttlecock"}),
    TABLE_TENNIS(2, 4, true, "Indoor table sport", new String[]{"Paddle", "Ball"}),
    GOLF(1, 4, false, "Outdoor course sport", new String[]{"Golf Clubs", "Golf Balls"}),
    CLIMBING(2, 8, false, "Indoor/Outdoor climbing sport", new String[]{"Climbing Shoes", "Harness"});

    private final int minPlayers;
    private final int maxPlayers;
    private final boolean requiresBooking;
    private final String description;
    private final String[] requiredEquipment;

    SportType(int minPlayers, int maxPlayers, boolean requiresBooking, String description, String[] requiredEquipment) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.requiresBooking = requiresBooking;
        this.description = description;
        this.requiredEquipment = requiredEquipment;
    }

    public static SportType fromString(String sportName) {
        try {
            return SportType.valueOf(sportName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}