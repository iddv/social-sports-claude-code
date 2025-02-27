package com.socialsports.service;

import com.socialsports.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.business.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.api.version}")
    private String apiVersion;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private static final String GRAPH_API_URL = "https://graph.facebook.com";

    public void sendTextMessage(String recipientPhoneNumber, String message) {
        try {
            String url = GRAPH_API_URL + "/" + apiVersion + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> recipient = new HashMap<>();
            recipient.put("phone_number_id", recipientPhoneNumber);
            
            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("body", message);

            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("recipient_type", "individual");
            requestBody.put("to", recipientPhoneNumber);
            requestBody.put("type", "text");
            requestBody.put("text", messageBody);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(url, entity, String.class);
            
            log.info("Message sent to {}: {}", recipientPhoneNumber, message);
        } catch (Exception e) {
            log.error("Error sending WhatsApp message: {}", e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }
    
    public String createWhatsAppGroup(String groupName, List<String> participantPhoneNumbers) {
        // Note: This is a placeholder for WhatsApp Business API integration
        // In a real implementation, you would make API calls to create a WhatsApp group
        log.info("Creating WhatsApp group {} with {} participants", groupName, participantPhoneNumbers.size());
        return "group-" + System.currentTimeMillis(); // Mock group ID
    }
    
    public void sendEventCreationNotification(Event event) {
        String message = String.format(
                "🎾 New %s event created!\n" +
                "📅 Date: %s\n" +
                "📍 Location: %s\n" +
                "👥 Skill level: %d/5\n" +
                "👤 Participants: %d/%d\n\n" +
                "Reply with 'JOIN %s' to participate!",
                event.getSportType(),
                event.getEventTime().toString(),
                event.getLocation(),
                event.getSkillLevel(),
                event.getParticipantPhoneNumbers().size(),
                event.getParticipantLimit(),
                event.getId()
        );
        
        // In a real implementation, you would send this to a community channel
        // For demo purposes, we'll log it
        log.info("Event creation notification: {}", message);
    }
    
    public void sendEventJoinConfirmation(Event event, String participantPhoneNumber) {
        String message = String.format(
                "You've successfully joined the %s event on %s at %s. " +
                "You'll receive a reminder 24h before the event. " +
                "Reply with 'CANCEL %s' if you can't make it.",
                event.getSportType(),
                event.getEventTime().toString(),
                event.getLocation(),
                event.getId()
        );
        
        sendTextMessage(participantPhoneNumber, message);
    }
    
    public void sendEventReminder(Event event) {
        String message = String.format(
                "⏰ Reminder: Your %s event is tomorrow at %s!\n" +
                "📍 Location: %s\n" +
                "👥 Participants: %d/%d",
                event.getSportType(),
                event.getEventTime().toString(),
                event.getLocation(),
                event.getParticipantPhoneNumbers().size(),
                event.getParticipantLimit()
        );
        
        for (String participantPhoneNumber : event.getParticipantPhoneNumbers()) {
            sendTextMessage(participantPhoneNumber, message);
        }
    }
    
    public void sendEventCancellationNotification(Event event, String reason) {
        String message = String.format(
                "❌ Your %s event on %s has been cancelled.\n" +
                "Reason: %s\n" +
                "We hope to see you at another event soon!",
                event.getSportType(),
                event.getEventTime().toString(),
                reason
        );
        
        for (String participantPhoneNumber : event.getParticipantPhoneNumbers()) {
            sendTextMessage(participantPhoneNumber, message);
        }
    }
}