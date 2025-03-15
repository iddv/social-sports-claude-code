package com.socialsports.service;

import com.socialsports.model.User;
import com.socialsports.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String phoneNumber, String name) {
        return createUser(phoneNumber, name, null);
    }
    
    public User createUser(String phoneNumber, String name, String email) {
        String userId = UUID.randomUUID().toString();
        User user = User.builder()
                .userId(userId)
                .phoneNumber(phoneNumber)
                .name(name)
                .email(email)
                .skillLevel(1)
                .eventsCreated(0)
                .eventsJoined(0)
                .whatsappLinked(phoneNumber != null && !phoneNumber.isEmpty())
                .isPremium(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return userRepository.save(user);
    }

    public Optional<User> getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void incrementEventsCreated(String phoneNumber) {
        getUserByPhoneNumber(phoneNumber).ifPresent(user -> {
            user.setEventsCreated(user.getEventsCreated() + 1);
            updateUser(user);
        });
    }

    public void incrementEventsJoined(String phoneNumber) {
        getUserByPhoneNumber(phoneNumber).ifPresent(user -> {
            user.setEventsJoined(user.getEventsJoined() + 1);
            updateUser(user);
        });
    }
    
    public boolean canCreateEvent(String phoneNumber) {
        return getUserByPhoneNumber(phoneNumber)
                .map(user -> user.getIsPremium() || user.getEventsCreated() < 5)
                .orElse(true);
    }
}