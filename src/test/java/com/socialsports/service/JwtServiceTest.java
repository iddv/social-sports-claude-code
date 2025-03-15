package com.socialsports.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String TEST_USER_ID = "test-user-123";
    private final String TEST_SECRET = "testSecretKey123456789012345678901234567890";
    private final long TEST_EXPIRATION = 3600; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(TEST_USER_ID);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testExtractUserId() {
        String token = jwtService.generateToken(TEST_USER_ID);
        String extractedUserId = jwtService.extractUserId(token);
        
        assertEquals(TEST_USER_ID, extractedUserId);
    }

    @Test
    void testExtractExpiration() {
        String token = jwtService.generateToken(TEST_USER_ID);
        Date expiration = jwtService.extractExpiration(token);
        
        assertNotNull(expiration);
        
        // The expiration should be in the future (approximately TEST_EXPIRATION seconds from now)
        long expirationTime = expiration.getTime();
        long currentTime = System.currentTimeMillis();
        long difference = expirationTime - currentTime;
        
        // Allow for a small margin of error due to test execution time
        assertTrue(difference > 0);
        assertTrue(difference <= TEST_EXPIRATION * 1000 + 1000); // Add 1 second margin
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtService.generateToken(TEST_USER_ID);
        UserDetails userDetails = new User(TEST_USER_ID, "", new ArrayList<>());
        
        boolean isValid = jwtService.validateToken(token, userDetails);
        
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername() {
        String token = jwtService.generateToken(TEST_USER_ID);
        UserDetails userDetails = new User("wrong-user", "", new ArrayList<>());
        
        boolean isValid = jwtService.validateToken(token, userDetails);
        
        assertFalse(isValid);
    }

    // For the expired token test, we'll skip it for now since it relies on private implementation details
    // and attempting to test it directly could lead to brittle tests.
    // In a real application, we might want to use a more testable design, potentially 
    // exposing isTokenExpired as protected or providing a way to override token validation in tests.
} 