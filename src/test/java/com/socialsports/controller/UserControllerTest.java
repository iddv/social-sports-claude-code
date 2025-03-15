package com.socialsports.controller;

import com.socialsports.model.User;
import com.socialsports.service.JwtService;
import com.socialsports.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "+1234567890";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(TEST_USER_ID)
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .phoneNumber(TEST_PHONE)
                .skillLevel(3)
                .eventsCreated(2)
                .eventsJoined(5)
                .whatsappLinked(true)
                .isPremium(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testRegisterUser_Success() {
        // Prepare test data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", TEST_NAME);
        userData.put("email", TEST_EMAIL);
        userData.put("phoneNumber", TEST_PHONE);

        when(userService.createUser(TEST_PHONE, TEST_NAME, TEST_EMAIL)).thenReturn(testUser);

        // Execute the controller method
        ResponseEntity<User> response = userController.registerUser(userData);

        // Verify the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService).createUser(TEST_PHONE, TEST_NAME, TEST_EMAIL);
    }

    @Test
    void testRegisterUser_Exception() {
        // Prepare test data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", TEST_NAME);
        userData.put("email", TEST_EMAIL);
        userData.put("phoneNumber", TEST_PHONE);

        when(userService.createUser(TEST_PHONE, TEST_NAME, TEST_EMAIL))
                .thenThrow(new RuntimeException("Test exception"));

        // Execute the controller method and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.registerUser(userData);
        });

        assertTrue(exception.getMessage().contains("Test exception"));
        verify(userService).createUser(TEST_PHONE, TEST_NAME, TEST_EMAIL);
    }

    @Test
    void testLoginUser_Success_WithEmail() {
        // Prepare test data
        Map<String, String> loginData = new HashMap<>();
        loginData.put("identifier", TEST_EMAIL);
        loginData.put("password", "password");

        when(userService.getUserByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);

        // Execute the controller method
        ResponseEntity<Map<String, String>> response = userController.loginUser(loginData);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_TOKEN, response.getBody().get("token"));
        assertEquals(TEST_USER_ID, response.getBody().get("userId"));
        verify(userService).getUserByEmail(TEST_EMAIL);
        verify(jwtService).generateToken(TEST_USER_ID);
    }

    @Test
    void testLoginUser_Success_WithPhone() {
        // Prepare test data
        Map<String, String> loginData = new HashMap<>();
        loginData.put("identifier", TEST_PHONE);
        loginData.put("password", "password");

        when(userService.getUserByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);

        // Execute the controller method
        ResponseEntity<Map<String, String>> response = userController.loginUser(loginData);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_TOKEN, response.getBody().get("token"));
        assertEquals(TEST_USER_ID, response.getBody().get("userId"));
        verify(userService).getUserByPhoneNumber(TEST_PHONE);
        verify(jwtService).generateToken(TEST_USER_ID);
    }

    @Test
    void testLoginUser_UserNotFound() {
        // Prepare test data
        Map<String, String> loginData = new HashMap<>();
        loginData.put("identifier", TEST_EMAIL);
        loginData.put("password", "password");

        when(userService.getUserByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Execute the controller method
        ResponseEntity<Map<String, String>> response = userController.loginUser(loginData);

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userService).getUserByEmail(TEST_EMAIL);
        verifyNoInteractions(jwtService);
    }

    @Test
    void testGetCurrentUser_Success() {
        // Set up security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(authentication.getName()).thenReturn(TEST_USER_ID);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Execute the controller method
        ResponseEntity<User> response = userController.getCurrentUser();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService).getUserById(TEST_USER_ID);
    }

    @Test
    void testGetCurrentUser_Unauthenticated() {
        // Set up security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(authentication.isAuthenticated()).thenReturn(false);

        // Execute the controller method
        ResponseEntity<User> response = userController.getCurrentUser();

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // Set up security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(authentication.getName()).thenReturn(TEST_USER_ID);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Execute the controller method
        ResponseEntity<User> response = userController.getCurrentUser();

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userService).getUserById(TEST_USER_ID);
    }

    @Test
    void testGetUserById_Success() {
        when(userService.getUserById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Execute the controller method
        ResponseEntity<User> response = userController.getUserById(TEST_USER_ID);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService).getUserById(TEST_USER_ID);
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userService.getUserById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Execute the controller method
        ResponseEntity<User> response = userController.getUserById(TEST_USER_ID);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserById(TEST_USER_ID);
    }
} 