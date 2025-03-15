package com.socialsports.config;

import com.socialsports.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testPublicEndpoints_NoAuthentication() throws Exception {
        // Test public endpoints that should be accessible without authentication
        mockMvc.perform(get("/api/users/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/sport-types"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpoints_NoAuthentication() throws Exception {
        // Test protected endpoints that should require authentication
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/events/123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testProtectedEndpoints_WithAuthentication() throws Exception {
        // Test protected endpoints with authentication
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/123"))
                .andExpect(status().isOk());
    }
} 