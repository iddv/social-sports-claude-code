package com.socialsports.service;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import com.socialsports.model.SportType;
import com.socialsports.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final WhatsAppService whatsAppService;

    @Value("${event.minimum.advance.hours}")
    private int minimumAdvanceHours;

    @Value("${event.auto.cancel.hours}")
    private int autoCancelHours;

    public Event createEvent(String creatorPhoneNumber, SportType sportType, String location, 
                            LocalDateTime eventTime, Integer participantLimit, Integer skillLevel, 
                            String bookingLink) {
        
        // Validate event time
        LocalDateTime now = LocalDateTime.now();
        if (eventTime.isBefore(now.plusHours(minimumAdvanceHours))) {
            throw new IllegalArgumentException("Events must be created at least " + 
                                             minimumAdvanceHours + " hours in advance");
        }
        
        // Check if the user can create an event (usage limits)
        if (!userService.canCreateEvent(creatorPhoneNumber)) {
            throw new IllegalArgumentException("You've reached your free event creation limit. " +
                                             "Please upgrade to premium to create more events.");
        }
        
        // Create a new event
        String eventId = UUID.randomUUID().toString();
        List<String> participants = new ArrayList<>();
        participants.add(creatorPhoneNumber); // Creator automatically joins the event
        
        Map<String, Boolean> remindersSent = new HashMap<>();
        remindersSent.put("24h", false);
        remindersSent.put("2h", false);
        
        Event event = Event.builder()
                .id(eventId)
                .sportType(sportType)
                .location(location)
                .eventTime(eventTime)
                .creatorPhoneNumber(creatorPhoneNumber)
                .participantPhoneNumbers(participants)
                .participantLimit(participantLimit)
                .skillLevel(skillLevel)
                .status(EventStatus.CREATED)
                .remindersSent(remindersSent)
                .bookingLink(bookingLink)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        Event savedEvent = eventRepository.save(event);
        
        // Update user's event count
        userService.incrementEventsCreated(creatorPhoneNumber);
        
        // Send notification about the new event
        whatsAppService.sendEventCreationNotification(savedEvent);
        
        return savedEvent;
    }
    
    public Optional<Event> getEventById(String eventId) {
        return eventRepository.findById(eventId);
    }
    
    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now());
    }
    
    public Event joinEvent(String eventId, String participantPhoneNumber) {
        return getEventById(eventId).map(event -> {
            // Check if the event is still accepting participants
            if (!event.getStatus().equals(EventStatus.CREATED)) {
                throw new IllegalStateException("This event is no longer accepting participants");
            }
            
            // Check if the participant limit has been reached
            if (event.getParticipantPhoneNumbers().size() >= event.getParticipantLimit()) {
                throw new IllegalStateException("This event is already full");
            }
            
            // Check if the user is already a participant
            if (event.getParticipantPhoneNumbers().contains(participantPhoneNumber)) {
                throw new IllegalStateException("You are already a participant in this event");
            }
            
            // Add the participant
            List<String> participants = new ArrayList<>(event.getParticipantPhoneNumbers());
            participants.add(participantPhoneNumber);
            event.setParticipantPhoneNumbers(participants);
            event.setUpdatedAt(LocalDateTime.now());
            
            // If this is the first time the event reaches the minimum number of participants, 
            // create a WhatsApp group
            if (participants.size() >= 2 && event.getWhatsappGroupId() == null) {
                String groupName = event.getSportType() + " on " + event.getEventTime();
                String groupId = whatsAppService.createWhatsAppGroup(groupName, participants);
                event.setWhatsappGroupId(groupId);
                event.setStatus(EventStatus.CONFIRMED);
            }
            
            Event updatedEvent = eventRepository.save(event);
            
            // Update user's event count
            userService.incrementEventsJoined(participantPhoneNumber);
            
            // Send join confirmation to the participant
            whatsAppService.sendEventJoinConfirmation(updatedEvent, participantPhoneNumber);
            
            return updatedEvent;
        }).orElseThrow(() -> new NoSuchElementException("Event not found"));
    }
    
    public Event cancelEvent(String eventId, String reason) {
        return getEventById(eventId).map(event -> {
            if (event.getStatus().equals(EventStatus.CANCELED)) {
                throw new IllegalStateException("This event has already been canceled");
            }
            
            event.setStatus(EventStatus.CANCELED);
            event.setUpdatedAt(LocalDateTime.now());
            Event canceledEvent = eventRepository.save(event);
            
            // Send cancellation notification to all participants
            whatsAppService.sendEventCancellationNotification(canceledEvent, reason);
            
            return canceledEvent;
        }).orElseThrow(() -> new NoSuchElementException("Event not found"));
    }
    
    public void leaveEvent(String eventId, String participantPhoneNumber) {
        getEventById(eventId).ifPresent(event -> {
            // Check if the user is a participant
            if (!event.getParticipantPhoneNumbers().contains(participantPhoneNumber)) {
                throw new IllegalStateException("You are not a participant in this event");
            }
            
            // If the user is the creator, they can't leave - they have to cancel the event
            if (event.getCreatorPhoneNumber().equals(participantPhoneNumber)) {
                throw new IllegalStateException("As the creator, you can't leave the event. " +
                                             "You can cancel it instead.");
            }
            
            // Remove the participant
            List<String> participants = new ArrayList<>(event.getParticipantPhoneNumbers());
            participants.remove(participantPhoneNumber);
            event.setParticipantPhoneNumbers(participants);
            event.setUpdatedAt(LocalDateTime.now());
            
            // If there are not enough participants, change status back to CREATED
            if (participants.size() < 2 && event.getStatus().equals(EventStatus.CONFIRMED)) {
                event.setStatus(EventStatus.CREATED);
            }
            
            eventRepository.save(event);
            
            // Send message to participant confirming they've left the event
            whatsAppService.sendTextMessage(participantPhoneNumber, 
                "You have successfully left the " + event.getSportType() + " event on " + 
                event.getEventTime());
        });
    }
    
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayFromNow = now.plusHours(24);
        LocalDateTime twoHoursFromNow = now.plusHours(2);
        
        List<Event> upcomingEvents = getUpcomingEvents();
        
        for (Event event : upcomingEvents) {
            // Send 24h reminder
            if (event.getEventTime().isBefore(oneDayFromNow) && 
                !event.getRemindersSent().get("24h") && 
                event.getStatus().equals(EventStatus.CONFIRMED)) {
                
                whatsAppService.sendEventReminder(event);
                event.getRemindersSent().put("24h", true);
                eventRepository.save(event);
            }
            
            // Send 2h reminder or auto-cancel if not enough participants
            if (event.getEventTime().isBefore(twoHoursFromNow) && 
                !event.getRemindersSent().get("2h")) {
                
                if (event.getStatus().equals(EventStatus.CONFIRMED)) {
                    // Final reminder for confirmed events
                    whatsAppService.sendEventReminder(event);
                    event.getRemindersSent().put("2h", true);
                    eventRepository.save(event);
                } else if (event.getStatus().equals(EventStatus.CREATED)) {
                    // Auto-cancel events that don't have minimum participants
                    cancelEvent(event.getId(), "Not enough participants joined the event");
                }
            }
        }
    }
}