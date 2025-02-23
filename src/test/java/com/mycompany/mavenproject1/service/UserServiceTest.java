package com.mycompany.mavenproject1.service;

import com.mycompany.mavenproject1.model.Role;
import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.repository.IRoleRepository;
import com.mycompany.mavenproject1.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>());
        testUser.setAccountNonLocked(true);

        userRole = Role.builder()
                .id(2L)
                .name("ROLE_USER")
                .description("Normal user role")
                .users(new HashSet<>())
                .build();

    }

    @Test
    void save_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.save(testUser);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertTrue(new BCryptPasswordEncoder().matches("password123", savedUser.getPassword()));
        assertNotEquals("password123", savedUser.getPassword());
    }

    @Test
    void setDefaultRole_WhenUserExists_ShouldAddUserRole() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findRoleById(2L)).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.setDefaultRole("testuser");

        // Assert
        verify(userRepository).save(testUser);
        verify(roleRepository).findRoleById(2L);
        assertTrue(testUser.getRoles().contains(userRole));
    }

    @Test
    void setDefaultRole_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.setDefaultRole("nonexistent"));
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent"));
    }

    @Test
    void findByUsername_ShouldReturnOptionalUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void getUserCount_ShouldReturnTotalCount() {
        // Arrange
        when(userRepository.count()).thenReturn(5L);

        // Act
        long count = userService.getUserCount();

        // Assert
        assertEquals(5L, count);
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
    }

    @Test
    void lockUserAccount_WhenUserExists_ShouldLockAccount() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.lockUserAccount("testuser");

        // Assert
        verify(userRepository).save(testUser);
        assertFalse(testUser.isAccountNonLocked());
    }

    @Test
    void unlockUserAccount_WhenUserExists_ShouldUnlockAccount() {
        // Arrange
        testUser.setAccountNonLocked(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.unlockUserAccount("testuser");

        // Assert
        verify(userRepository).save(testUser);
        assertTrue(testUser.isAccountNonLocked());
    }

    @Test
    void lockAndUnlockUserAccount_WhenUserNotFound_ShouldDoNothing() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        userService.lockUserAccount("nonexistent");
        userService.unlockUserAccount("nonexistent");

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }
}