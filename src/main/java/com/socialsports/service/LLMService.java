package com.socialsports.service;

import com.socialsports.model.Event;
import com.socialsports.model.SportType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMService {
    private final RestTemplate restTemplate;
    private final String openAiApiKey;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public LLMService(@Value("${openai.api.key}") String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> parseNaturalLanguageEventRequest(String message) {
        // Example: "I want to play tennis tomorrow at 3pm at Central Courts with 3 other people"
        Map<String, Object> eventDetails = new HashMap<>();
        
        // Call OpenAI API to extract structured information
        String prompt = String.format("""
            Extract the following information from this sports event request: '%s'
            - Sport type
            - Date and time
            - Location
            - Number of players
            - Any additional details
            Format the response as JSON.
            """, message);

        // TODO: Implement OpenAI API call here
        // For now, using regex-based parsing as fallback
        return parseWithRegex(message);
    }

    private Map<String, Object> parseWithRegex(String message) {
        Map<String, Object> details = new HashMap<>();
        
        // Basic patterns for demonstration
        for (SportType sport : SportType.values()) {
            if (message.toLowerCase().contains(sport.name().toLowerCase())) {
                details.put("sportType", sport);
                break;
            }
        }

        // Time pattern (e.g., "3pm", "15:00")
        Pattern timePattern = Pattern.compile("(\\d{1,2})(?::|\\s)?(\\d{2})?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE);
        Matcher timeMatcher = timePattern.matcher(message);
        if (timeMatcher.find()) {
            details.put("time", parseTimeString(timeMatcher.group()));
        }

        // Number of players pattern
        Pattern playerPattern = Pattern.compile("(\\d+)\\s+(?:other\\s+)?(?:player|people|person)s?");
        Matcher playerMatcher = playerPattern.matcher(message);
        if (playerMatcher.find()) {
            details.put("playerCount", Integer.parseInt(playerMatcher.group(1)));
        }

        // Location pattern (after "at")
        Pattern locationPattern = Pattern.compile("at\\s+([\\w\\s]+?)(?=\\s+(?:with|for|on|at|$))");
        Matcher locationMatcher = locationPattern.matcher(message);
        if (locationMatcher.find()) {
            details.put("location", locationMatcher.group(1).trim());
        }

        return details;
    }

    private LocalDateTime parseTimeString(String timeStr) {
        // Simple time parsing logic - extend as needed
        LocalDateTime now = LocalDateTime.now();
        Pattern pattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?(am|pm)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(timeStr);
        
        if (matcher.find()) {
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            String ampm = matcher.group(3);
            
            if (ampm != null && ampm.equalsIgnoreCase("pm") && hours < 12) {
                hours += 12;
            }
            
            return now.withHour(hours).withMinute(minutes);
        }
        
        return null;
    }
} 