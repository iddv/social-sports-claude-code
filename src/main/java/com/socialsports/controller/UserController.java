package com.socialsports.controller;

import com.socialsports.model.User;
import com.socialsports.service.JwtService;
import com.socialsports.service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
@CrossOrigin(origins = "*") // Enable CORS for frontend integration
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody Map<String, Object> userData) {
        try {
            String name = (String) userData.get("name");
            String email = (String) userData.get("email");
            String phoneNumber = (String) userData.get("phoneNumber");
            
            User user = userService.createUser(phoneNumber, name, email);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error registering user", e);
            throw new IllegalArgumentException("Invalid user data: " + e.getMessage());
        }
    }

    @Operation(summary = "Login user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String identifier = loginData.get("identifier"); // Can be email or phone number
            String password = loginData.get("password");
            
            // Authentication logic would go here
            // For now, we'll just check if the user exists
            Optional<User> user;
            
            if (identifier.contains("@")) {
                user = userService.getUserByEmail(identifier);
            } else {
                user = userService.getUserByPhoneNumber(identifier);
            }
            
            if (user.isPresent()) {
                // In a real implementation, we would verify the password
                // Generate a JWT token
                String token = jwtService.generateToken(user.get().getUserId());
                
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("userId", user.get().getUserId());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Error during login", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Get current user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved user profile"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        // Get the authenticated user from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();
            Optional<User> user = userService.getUserById(userId);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Get user profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the user"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to fetch") @PathVariable String userId) {
        
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 