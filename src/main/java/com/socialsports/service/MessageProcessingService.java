package com.socialsports.service;

import com.socialsports.model.SportType;
import com.socialsports.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingService {

    private final UserService userService;
    private final EventService eventService;
    private final WhatsAppService whatsAppService;
    
    private static final Pattern JOIN_PATTERN = Pattern.compile("JOIN\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CANCEL_PATTERN = Pattern.compile("CANCEL\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_EVENT_PATTERN = Pattern.compile(
            "CREATE\\s+EVENT\\s+" +         // Command
            "(PADEL|TENNIS|FOOTBALL|BASKETBALL|VOLLEYBALL)\\s+" +  // Sport type
            "AT\\s+(.+?)\\s+" +             // Location
            "ON\\s+(.+?)\\s+" +             // Date/time
            "FOR\\s+(\\d+)\\s+PLAYERS\\s+" +   // Participant limit
            "SKILL\\s+(\\d)\\s*" +          // Skill level 
            "(?:BOOKING\\s+(.+))?",        // Optional booking link
            Pattern.CASE_INSENSITIVE);
    
    public void processMessage(String senderPhoneNumber, String messageContent) {
        log.info("Processing message from {}: {}", senderPhoneNumber, messageContent);
        
        // Register user if not already registered
        userService.getUserByPhoneNumber(senderPhoneNumber)
                .orElseGet(() -> userService.createUser(senderPhoneNumber, "User" + senderPhoneNumber.substring(senderPhoneNumber.length() - 4)));
        
        // Process based on message intent
        if (messageContent.toUpperCase().startsWith("JOIN")) {
            processJoinRequest(senderPhoneNumber, messageContent);
        } else if (messageContent.toUpperCase().startsWith("CANCEL")) {
            processCancelRequest(senderPhoneNumber, messageContent);
        } else if (messageContent.toUpperCase().startsWith("CREATE EVENT")) {
            processCreateEventRequest(senderPhoneNumber, messageContent);
        } else if (messageContent.toUpperCase().startsWith("LEAVE")) {
            processLeaveRequest(senderPhoneNumber, messageContent);
        } else if (messageContent.toUpperCase().startsWith("HELP")) {
            sendHelpMessage(senderPhoneNumber);
        } else if (messageContent.toUpperCase().startsWith("EVENTS")) {
            sendUpcomingEvents(senderPhoneNumber);
        } else {
            sendUnknownCommandMessage(senderPhoneNumber);
        }
    }
    
    private void processJoinRequest(String phoneNumber, String message) {
        Matcher matcher = JOIN_PATTERN.matcher(message);
        if (matcher.find()) {
            String eventId = matcher.group(1);
            try {
                eventService.joinEvent(eventId, phoneNumber);
            } catch (NoSuchElementException e) {
                whatsAppService.sendTextMessage(phoneNumber, "Event not found. Please check the event ID and try again.");
            } catch (IllegalStateException e) {
                whatsAppService.sendTextMessage(phoneNumber, e.getMessage());
            }
        } else {
            whatsAppService.sendTextMessage(phoneNumber, "Invalid JOIN command. Please use the format: JOIN [eventId]");
        }
    }
    
    private void processCancelRequest(String phoneNumber, String message) {
        Matcher matcher = CANCEL_PATTERN.matcher(message);
        if (matcher.find()) {
            String eventId = matcher.group(1);
            try {
                eventService.getEventById(eventId).ifPresent(event -> {
                    if (event.getCreatorPhoneNumber().equals(phoneNumber)) {
                        eventService.cancelEvent(eventId, "Canceled by event creator");
                        whatsAppService.sendTextMessage(phoneNumber, "Event canceled successfully");
                    } else {
                        whatsAppService.sendTextMessage(phoneNumber, "Only the event creator can cancel an event. To leave an event, use LEAVE [eventId]");
                    }
                });
            } catch (NoSuchElementException e) {
                whatsAppService.sendTextMessage(phoneNumber, "Event not found. Please check the event ID and try again.");
            }
        } else {
            whatsAppService.sendTextMessage(phoneNumber, "Invalid CANCEL command. Please use the format: CANCEL [eventId]");
        }
    }
    
    private void processLeaveRequest(String phoneNumber, String message) {
        Pattern leavePattern = Pattern.compile("LEAVE\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = leavePattern.matcher(message);
        if (matcher.find()) {
            String eventId = matcher.group(1);
            try {
                eventService.leaveEvent(eventId, phoneNumber);
            } catch (IllegalStateException e) {
                whatsAppService.sendTextMessage(phoneNumber, e.getMessage());
            }
        } else {
            whatsAppService.sendTextMessage(phoneNumber, "Invalid LEAVE command. Please use the format: LEAVE [eventId]");
        }
    }
    
    private void processCreateEventRequest(String phoneNumber, String message) {
        Matcher matcher = CREATE_EVENT_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                SportType sportType = SportType.valueOf(matcher.group(1).toUpperCase());
                String location = matcher.group(2);
                String dateTimeStr = matcher.group(3);
                int participantLimit = Integer.parseInt(matcher.group(4));
                int skillLevel = Integer.parseInt(matcher.group(5));
                String bookingLink = matcher.group(6);
                
                LocalDateTime eventTime;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    eventTime = LocalDateTime.parse(dateTimeStr, formatter);
                } catch (DateTimeParseException e) {
                    whatsAppService.sendTextMessage(phoneNumber, 
                        "Invalid date/time format. Please use format: YYYY-MM-DD HH:MM (e.g., 2023-04-15 18:30)");
                    return;
                }
                
                if (skillLevel < 1 || skillLevel > 5) {
                    whatsAppService.sendTextMessage(phoneNumber, "Skill level must be between 1 and 5");
                    return;
                }
                
                if (participantLimit < 2) {
                    whatsAppService.sendTextMessage(phoneNumber, "Participant limit must be at least 2");
                    return;
                }
                
                try {
                    eventService.createEvent(phoneNumber, sportType, location, eventTime, 
                                          participantLimit, skillLevel, bookingLink);
                    whatsAppService.sendTextMessage(phoneNumber, "Event created successfully! " +
                                                  "We'll notify you when people join.");
                } catch (IllegalArgumentException e) {
                    whatsAppService.sendTextMessage(phoneNumber, e.getMessage());
                }
                
            } catch (Exception e) {
                log.error("Error processing create event: {}", e.getMessage());
                whatsAppService.sendTextMessage(phoneNumber, "Error creating event. " + e.getMessage());
            }
        } else {
            whatsAppService.sendTextMessage(phoneNumber, 
                "Invalid CREATE EVENT command. Example format: " +
                "CREATE EVENT PADEL AT City Sports Club ON 2023-04-15 18:30 FOR 4 PLAYERS SKILL 3 BOOKING http://example.com");
        }
    }
    
    private void sendHelpMessage(String phoneNumber) {
        String helpMessage = 
            "*SocialSports Bot Commands*\n\n" +
            "• *CREATE EVENT [sport] AT [location] ON [date] FOR [number] PLAYERS SKILL [1-5] BOOKING [optional-url]* - Create a new event\n" +
            "• *JOIN [eventId]* - Join an existing event\n" +
            "• *LEAVE [eventId]* - Leave an event you joined\n" +
            "• *CANCEL [eventId]* - Cancel an event (creator only)\n" +
            "• *EVENTS* - Show upcoming events\n" +
            "• *HELP* - Show this help message";
        
        whatsAppService.sendTextMessage(phoneNumber, helpMessage);
    }
    
    private void sendUpcomingEvents(String phoneNumber) {
        var events = eventService.getUpcomingEvents();
        
        if (events.isEmpty()) {
            whatsAppService.sendTextMessage(phoneNumber, "There are no upcoming events. You can create one!");
            return;
        }
        
        StringBuilder message = new StringBuilder("*Upcoming Events*\n\n");
        for (var event : events) {
            message.append("🏆 *").append(event.getSportType()).append("*\n")
                  .append("📅 ").append(event.getEventTime()).append("\n")
                  .append("📍 ").append(event.getLocation()).append("\n")
                  .append("👥 ").append(event.getParticipantPhoneNumbers().size())
                  .append("/").append(event.getParticipantLimit()).append(" participants\n")
                  .append("🔢 ID: ").append(event.getId()).append("\n\n");
        }
        
        whatsAppService.sendTextMessage(phoneNumber, message.toString());
    }
    
    private void sendUnknownCommandMessage(String phoneNumber) {
        whatsAppService.sendTextMessage(phoneNumber, 
            "I don't understand that command. Type HELP to see available commands.");
    }
}