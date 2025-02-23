package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.service.UserService;
import com.mycompany.mavenproject1.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("testPass");

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setAvtUrl("http://example.com/avatar.jpg");
    }

    @Test
    void testAuthenticateUser_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateJwtToken("testUser", testUser.getAvtUrl()))
                .thenReturn("test.jwt.token");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("test.jwt.token", jwtResponse.getToken());
        assertEquals(200, response.getStatusCodeValue());

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByUsername("testUser");
        verify(jwtUtil).generateJwtToken("testUser", testUser.getAvtUrl());
    }

    @Test
    void testAuthenticateUser_WithoutAvatar() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(Optional.empty());
        when(jwtUtil.generateJwtToken("testUser", ""))
                .thenReturn("test.jwt.token");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("test.jwt.token", jwtResponse.getToken());
        assertEquals(200, response.getStatusCodeValue());

        // Verify empty avatar URL is used
        verify(jwtUtil).generateJwtToken("testUser", "");
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationServiceException("Invalid credentials"));

        // Act & Assert
        assertThrows(AuthenticationServiceException.class, () -> {
            authController.authenticateUser(loginRequest);
        }, "Should throw AuthenticationServiceException for invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).findByUsername(anyString());
        verify(jwtUtil, never()).generateJwtToken(anyString(), anyString());
    }

    @Test
    void testAuthenticateUser_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            authController.authenticateUser(null);
        }, "Should throw NullPointerException for null request");

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLoginRequest_Getters_Setters() {
        // Arrange
        LoginRequest request = new LoginRequest();

        // Act
        request.setUsername("testUser");
        request.setPassword("testPass");

        // Assert
        assertEquals("testUser", request.getUsername());
        assertEquals("testPass", request.getPassword());
    }

    @Test
    void testJwtResponse_Constructor_And_Getter() {
        // Arrange & Act
        JwtResponse response = new JwtResponse("test.token");

        // Assert
        assertEquals("test.token", response.getToken());
    }
}