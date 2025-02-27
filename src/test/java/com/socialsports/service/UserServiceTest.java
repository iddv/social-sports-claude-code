package com.socialsports.service;

import com.socialsports.model.User;
import com.socialsports.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String PHONE_NUMBER = "+123456789";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .phoneNumber(PHONE_NUMBER)
                .name("Test User")
                .skillLevel(3)
                .eventsCreated(2)
                .eventsJoined(5)
                .isPremium(false)
                .build();
    }

    @Test
    void createUser_ShouldReturnNewUser() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(PHONE_NUMBER, "Test User");

        assertNotNull(result);
        assertEquals(PHONE_NUMBER, result.getPhoneNumber());
        assertEquals("Test User", result.getName());
        assertEquals(1, result.getSkillLevel());
        assertEquals(0, result.getEventsCreated());
        assertEquals(0, result.getEventsJoined());
        assertFalse(result.getIsPremium());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserByPhoneNumber_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByPhoneNumber(PHONE_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    void getUserByPhoneNumber_ShouldReturnEmpty_WhenUserDoesNotExist() {
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByPhoneNumber(PHONE_NUMBER);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    void incrementEventsCreated_ShouldIncrementCounter_WhenUserExists() {
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.incrementEventsCreated(PHONE_NUMBER);

        assertEquals(3, testUser.getEventsCreated());
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void incrementEventsJoined_ShouldIncrementCounter_WhenUserExists() {
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.incrementEventsJoined(PHONE_NUMBER);

        assertEquals(6, testUser.getEventsJoined());
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void canCreateEvent_ShouldReturnTrue_ForPremiumUser() {
        testUser.setIsPremium(true);
        testUser.setEventsCreated(10); // Even with many events, premium users can create more
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(testUser));

        boolean result = userService.canCreateEvent(PHONE_NUMBER);

        assertTrue(result);
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    void canCreateEvent_ShouldReturnTrue_ForFreeUserBelowLimit() {
        testUser.setIsPremium(false);
        testUser.setEventsCreated(4); // Below the limit of 5
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(testUser));

        boolean result = userService.canCreateEvent(PHONE_NUMBER);

        assertTrue(result);
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    void canCreateEvent_ShouldReturnFalse_ForFreeUserAtLimit() {
        testUser.setIsPremium(false);
        testUser.setEventsCreated(5); // At the limit
        when(userRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(testUser));

        boolean result = userService.canCreateEvent(PHONE_NUMBER);

        assertFalse(result);
        verify(userRepository, times(1)).findByPhoneNumber(PHONE_NUMBER);
    }
}