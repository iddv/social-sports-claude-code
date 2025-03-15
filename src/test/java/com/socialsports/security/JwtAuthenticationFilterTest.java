package com.socialsports.security;

import com.socialsports.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_USER_ID = "test-user-123";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidToken() throws Exception {
        // Set up request with Authorization header
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        
        // Set up JwtService to return a valid user ID and validate the token
        when(jwtService.extractUserId(TEST_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtService.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        
        // Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);
        
        // Verify that authentication was set in the context
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TEST_USER_ID, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void testDoFilterInternal_WithInvalidToken() throws Exception {
        // Set up request with Authorization header
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        
        // Set up JwtService to return a valid user ID but fail validation
        when(jwtService.extractUserId(TEST_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtService.validateToken(anyString(), any(UserDetails.class))).thenReturn(false);
        
        // Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);
        
        // Verify that no authentication was set in the context
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws Exception {
        // Set up request with no Authorization header
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);
        
        // Verify that no token extraction was attempted
        verifyNoInteractions(jwtService);
        
        // Verify that no authentication was set in the context
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidAuthorizationHeader() throws Exception {
        // Set up request with invalid Authorization header (no "Bearer " prefix)
        when(request.getHeader("Authorization")).thenReturn(TEST_TOKEN);
        
        // Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);
        
        // Verify that no token extraction was attempted
        verifyNoInteractions(jwtService);
        
        // Verify that no authentication was set in the context
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ExceptionDuringTokenProcessing() throws Exception {
        // Set up request with Authorization header
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        
        // Set up JwtService to throw an exception
        when(jwtService.extractUserId(TEST_TOKEN)).thenThrow(new RuntimeException("Token processing error"));
        
        // Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify that the filter chain was called (the exception should be caught)
        verify(filterChain).doFilter(request, response);
        
        // Verify that no authentication was set in the context
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
} 