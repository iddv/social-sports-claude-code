package com.socialsports.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SportTypeTest {

    @Test
    void testSportTypeProperties() {
        SportType tennis = SportType.TENNIS;
        
        assertEquals(2, tennis.getMinPlayers());
        assertEquals(4, tennis.getMaxPlayers());
        assertTrue(tennis.isRequiresBooking());
        assertEquals("Court sport played with rackets and ball", tennis.getDescription());
        assertArrayEquals(new String[]{"Racket", "Ball"}, tennis.getRequiredEquipment());
    }

    @Test
    void testFromString() {
        assertEquals(SportType.TENNIS, SportType.fromString("TENNIS"));
        assertEquals(SportType.TENNIS, SportType.fromString("tennis"));
        assertNull(SportType.fromString("invalidSport"));
    }

    @Test
    void testAllSportsHaveValidConstraints() {
        for (SportType sport : SportType.values()) {
            assertTrue(sport.getMinPlayers() > 0, "Minimum players should be positive for " + sport);
            assertTrue(sport.getMaxPlayers() >= sport.getMinPlayers(), 
                "Maximum players should be >= minimum players for " + sport);
            assertNotNull(sport.getDescription(), "Description should not be null for " + sport);
            assertTrue(sport.getRequiredEquipment().length > 0, 
                "Required equipment should not be empty for " + sport);
        }
    }
} 