package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userService.findByUsername("testUser")).thenReturn(Optional.empty());
        doNothing().when(userService).save(any(User.class));
        doNothing().when(userService).setDefaultRole("testUser");

        // Act
        ResponseEntity<?> response = userController.registerUser(testUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        assertEquals("User registered successfully", response.getBody(), "Should return success message");
        verify(userService).save(testUser);
        verify(userService).setDefaultRole("testUser");
    }

    @Test
    void testRegisterUser_UsernameExists() {
        // Arrange
        when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = userController.registerUser(testUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 Bad Request");
        assertEquals("Username already exists", response.getBody(), "Should return error message");
        verify(userService, never()).save(any(User.class));
        verify(userService, never()).setDefaultRole(anyString());
    }

    @Test
    void testRegisterUser_Exception() {
        // Arrange
        when(userService.findByUsername("testUser")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Database error")).when(userService).save(any(User.class));

        // Act
        ResponseEntity<?> response = userController.registerUser(testUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Should return 500 Internal Server Error");
        assertEquals("An error occurred while processing your request.", response.getBody(), "Should return error message");
        verify(userService, never()).setDefaultRole(anyString());
    }

    @Test
    void testGetCurrentUser_Authenticated() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        assertEquals(testUser, response.getBody(), "Should return user details");
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        assertNull(response.getBody(), "Should return empty response");
    }

    @Test
    void testGetCurrentUser_NullAuthentication() {
        // Act
        ResponseEntity<?> response = userController.getCurrentUser(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        assertNull(response.getBody(), "Should return empty response");
    }
}