package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.VideoUpdateRequest;
import com.mycompany.mavenproject1.model.Room;
import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.repository.IUserRepository;
import com.mycompany.mavenproject1.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoomControllerTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private RoomController roomController;

    private Room testRoom;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setAvtUrl("https://test-avatar.jpg");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setOwnerUsername("testUser");
        testRoom.setThumbnail("https://test-avatar.jpg");
    }

    @Test
    void testGetAllRooms() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        // Act
        List<Room> result = roomController.getAllRooms();

        // Assert
        assertEquals(1, result.size(), "Should return one room");
        assertEquals(testRoom.getName(), result.get(0).getName(), "Room name should match");
        verify(roomRepository).findAll();
    }

    @Test
    void testGetRoomById() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // Act
        Room result = roomController.getRoomById(1L);

        // Assert
        assertNotNull(result, "Room should not be null");
        assertEquals(testRoom.getName(), result.getName(), "Room name should match");
        verify(roomRepository).findById(1L);
    }

    @Test
    void testCreateRoom() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // Act
        Room result = roomController.createRoom(userDetails);

        // Assert
        assertNotNull(result, "Created room should not be null");
        assertEquals("testUser", result.getOwnerUsername(), "Owner username should match");
        assertEquals(testUser.getAvtUrl(), result.getThumbnail(), "Room thumbnail should match user avatar");
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testCreateRoomWithNonExistentUser() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("nonExistentUser");
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room savedRoom = invocation.getArgument(0);
            savedRoom.setId(1L);
            return savedRoom;
        });

        // Act
        Room result = roomController.createRoom(userDetails);

        // Assert
        assertNotNull(result, "Created room should not be null");
        assertEquals("https://i.imgur.com/Tr9qnkI.jpeg", result.getThumbnail(), "Should use default thumbnail");
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testDeleteRoomIfOwner() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(userDetails.getUsername()).thenReturn("testUser");

        // Act
        roomController.deleteRoomIfOwner(1L, userDetails);

        // Assert
        verify(roomRepository).delete(testRoom);
        verify(messagingTemplate).convertAndSend(eq("/topic/1"), eq("{\"type\": \"OWNER_LEFT\"}"));
    }

    @Test
    void testDeleteRoomIfNotOwner() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(userDetails.getUsername()).thenReturn("differentUser");

        // Act
        roomController.deleteRoomIfOwner(1L, userDetails);

        // Assert
        verify(roomRepository, never()).delete(any(Room.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testUpdateRoomVideo() {
        // Arrange
        VideoUpdateRequest request = new VideoUpdateRequest();
        request.setCurrentVideoUrl("https://test-video.mp4");
        request.setCurrentVideoTitle("Test Video");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // Act
        ResponseEntity<?> response = roomController.updateRoomVideo(1L, request, userDetails);

        // Assert
        assertEquals(200, response.getStatusCodeValue(), "Should return 200 OK");
        verify(roomRepository).save(testRoom);
        assertEquals(request.getCurrentVideoUrl(), testRoom.getCurrentVideoUrl(), "Video URL should be updated");
        assertEquals(request.getCurrentVideoTitle(), testRoom.getCurrentVideoTitle(), "Video title should be updated");
    }

    @Test
    void testUpdateRoomVideoNotFound() {
        // Arrange
        VideoUpdateRequest request = new VideoUpdateRequest();
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = roomController.updateRoomVideo(1L, request, userDetails);

        // Assert
        assertEquals(404, response.getStatusCodeValue(), "Should return 404 Not Found");
        verify(roomRepository, never()).save(any(Room.class));
    }
}