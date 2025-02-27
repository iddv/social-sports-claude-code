package com.socialsports.service;

import com.socialsports.model.User;
import com.socialsports.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String phoneNumber, String name) {
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .name(name)
                .skillLevel(1)
                .eventsCreated(0)
                .eventsJoined(0)
                .isPremium(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return userRepository.save(user);
    }

    public Optional<User> getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
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