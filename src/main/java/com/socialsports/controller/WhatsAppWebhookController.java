package com.socialsports.controller;

import com.socialsports.service.MessageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final MessageProcessingService messageProcessingService;

    /**
     * Handles WhatsApp API verification
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        
        // Note: In a real implementation, you would validate the token against your configured token
        if ("subscribe".equals(mode) && "my_verify_token".equals(token)) {
            log.info("Webhook verified with challenge: {}", challenge);
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed. Mode: {}, Token: {}", mode, token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Handles incoming WhatsApp messages
     */
    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> payload) {
        log.info("Received webhook payload: {}", payload);
        
        try {
            // Extract the relevant data from the payload
            // Note: This is a simplified example. In a real implementation, 
            // you would need to handle the actual WhatsApp API payload structure
            
            // For Twilio
            if (payload.containsKey("From") && payload.containsKey("Body")) {
                String from = (String) payload.get("From");
                String body = (String) payload.get("Body");
                
                messageProcessingService.processMessage(from, body);
                return ResponseEntity.ok().build();
            }
            
            // For WhatsApp Business API
            if (payload.containsKey("entry")) {
                // Extract the sender and message from the WhatsApp Business API payload
                // This is a simplified example - actual parsing will depend on the exact structure
                
                @SuppressWarnings("unchecked")
                var entries = (java.util.List<Map<String, Object>>) payload.get("entry");
                
                if (!entries.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    var changes = (java.util.List<Map<String, Object>>) entries.get(0).get("changes");
                    
                    if (!changes.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        var value = (Map<String, Object>) changes.get(0).get("value");
                        
                        @SuppressWarnings("unchecked")
                        var messages = (java.util.List<Map<String, Object>>) value.get("messages");
                        
                        if (messages != null && !messages.isEmpty()) {
                            var message = messages.get(0);
                            var from = (String) message.get("from");
                            
                            @SuppressWarnings("unchecked")
                            var text = (Map<String, Object>) message.get("text");
                            
                            if (text != null) {
                                var body = (String) text.get("body");
                                messageProcessingService.processMessage(from, body);
                            }
                        }
                    }
                }
                
                return ResponseEntity.ok().build();
            }
            
            log.warn("Unsupported payload format");
            return ResponseEntity.ok().build(); // Always return 200 to WhatsApp
            
        } catch (Exception e) {
            log.error("Error processing webhook payload", e);
            return ResponseEntity.ok().build(); // Always return 200 to WhatsApp
        }
    }
}