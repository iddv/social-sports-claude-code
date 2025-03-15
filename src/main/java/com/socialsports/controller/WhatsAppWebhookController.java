package com.socialsports.controller;

import com.socialsports.service.MessageProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "WhatsApp Webhook", description = "WhatsApp webhook endpoints for message handling")
public class WhatsAppWebhookController {

    private final MessageProcessingService messageProcessingService;

    /**
     * Handles WhatsApp API verification
     */
    @Operation(summary = "Verify WhatsApp webhook", description = "Endpoint used by WhatsApp to verify the webhook subscription")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook verified successfully"),
        @ApiResponse(responseCode = "403", description = "Verification failed due to invalid token or mode")
    })
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @Parameter(description = "Hub mode should be 'subscribe'") @RequestParam("hub.mode") String mode,
            @Parameter(description = "Verification token to validate the webhook request") @RequestParam("hub.verify_token") String token,
            @Parameter(description = "Challenge string to be returned if verification succeeds") @RequestParam("hub.challenge") String challenge) {
        
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
    @Operation(summary = "Receive WhatsApp messages", description = "Endpoint that receives incoming messages from WhatsApp")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message received and processed")
    })
    @PostMapping
    public ResponseEntity<String> receiveMessage(
            @Parameter(description = "WhatsApp message payload", required = true) 
            @RequestBody Map<String, Object> payload) {
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