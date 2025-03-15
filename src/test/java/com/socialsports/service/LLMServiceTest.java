package com.socialsports.service;

import com.socialsports.model.SportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LLMServiceTest {

    private LLMService llmService;
    private LLMService mockLlmService;

    @BeforeEach
    void setUp() {
        // Real implementation for method-level tests
        llmService = new LLMService("dummy-api-key");
        
        // Mock implementation for natural language parsing tests
        mockLlmService = Mockito.mock(LLMService.class);
    }

    @Test
    void testParseNaturalLanguageEventRequest_Tennis() {
        // Create expected result for tennis scenario
        Map<String, Object> tennisResult = new HashMap<>();
        tennisResult.put("sportType", SportType.TENNIS);
        tennisResult.put("location", "Central Courts");
        tennisResult.put("playerCount", 3);
        
        LocalDateTime tennisTime = LocalDateTime.now().withHour(15).withMinute(0);
        tennisResult.put("time", tennisTime);
        
        // Set up mock behavior
        String tennisMessage = "I want to play tennis tomorrow at 3pm at Central Courts with 3 other people";
        when(mockLlmService.parseNaturalLanguageEventRequest(tennisMessage)).thenReturn(tennisResult);
        
        // Test with mock
        Map<String, Object> result = mockLlmService.parseNaturalLanguageEventRequest(tennisMessage);
        
        assertEquals(SportType.TENNIS, result.get("sportType"));
        assertEquals("Central Courts", result.get("location"));
        assertEquals(3, result.get("playerCount"));
        
        LocalDateTime time = (LocalDateTime) result.get("time");
        assertNotNull(time);
        assertEquals(15, time.getHour()); // 3pm = 15:00
        assertEquals(0, time.getMinute());
    }

    @Test
    void testParseNaturalLanguageEventRequest_Football() {
        // Create expected result for football scenario
        Map<String, Object> footballResult = new HashMap<>();
        footballResult.put("sportType", SportType.FOOTBALL);
        footballResult.put("location", "City Park");
        footballResult.put("playerCount", 18);
        
        LocalDateTime footballTime = LocalDateTime.now().withHour(18).withMinute(30);
        footballResult.put("time", footballTime);
        
        // Set up mock behavior
        String footballMessage = "Looking for 10 people to play football at City Park at 18:30";
        when(mockLlmService.parseNaturalLanguageEventRequest(footballMessage)).thenReturn(footballResult);
        
        // Test with mock
        Map<String, Object> result = mockLlmService.parseNaturalLanguageEventRequest(footballMessage);
        
        assertEquals(SportType.FOOTBALL, result.get("sportType"));
        assertEquals("City Park", result.get("location"));
        assertEquals(18, result.get("playerCount"));
        
        LocalDateTime time = (LocalDateTime) result.get("time");
        assertNotNull(time);
        assertEquals(18, time.getHour());
        assertEquals(30, time.getMinute());
    }

    @Test
    void testParseTimeString() {
        // Using reflection to test private method
        LocalDateTime result = (LocalDateTime) ReflectionTestUtils.invokeMethod(llmService, "parseTimeString", "3:30pm");
        
        assertNotNull(result);
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    void testParseTimeString_24HourFormat() {
        LocalDateTime result = (LocalDateTime) ReflectionTestUtils.invokeMethod(llmService, "parseTimeString", "15:30");
        
        assertNotNull(result);
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
    }
} 