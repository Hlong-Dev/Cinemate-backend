package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.ChatMessage;
import com.mycompany.mavenproject1.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChatControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatController chatController;

    private Map<String, Object> sessionAttributes;
    private final String ROOM_ID = "room1";

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
    }

    @Test
    void testSendMessage_WithTextMessage() {
        // Arrange
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(MessageType.CHAT);
        chatMessage.setContent("Hello");
        chatMessage.setSender("user1");

        // Act
        ChatMessage result = chatController.sendMessage(ROOM_ID, chatMessage);

        // Assert
        assertEquals(MessageType.CHAT, result.getType(), "Message type should be CHAT");
        assertEquals("Hello", result.getContent(), "Content should match");
        assertEquals("user1", result.getSender(), "Sender should match");
    }

    @Test
    void testSendMessage_WithImageMessage() {
        // Arrange
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(MessageType.IMAGE);
        chatMessage.setImage("base64ImageData");
        chatMessage.setSender("user1");

        // Act
        ChatMessage result = chatController.sendMessage(ROOM_ID, chatMessage);

        // Assert
        assertEquals(MessageType.IMAGE, result.getType(), "Message type should be IMAGE");
        assertEquals("base64ImageData", result.getImage(), "Image data should match");
        assertEquals("user1", result.getSender(), "Sender should match");
    }

    @Test
    void testSendMessage_WithLeaveMessage() {
        // Arrange
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(MessageType.LEAVE);
        chatMessage.setSender("user1");

        // Act
        ChatMessage result = chatController.sendMessage(ROOM_ID, chatMessage);

        // Assert
        assertEquals(MessageType.LEAVE, result.getType(), "Message type should be LEAVE");
        assertEquals("user1", result.getSender(), "Sender should match");
    }

    @Test
    void testAddUser() {
        // Arrange
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender("testUser");
        chatMessage.setAvtUrl("avatarUrl");

        // Act
        ChatMessage result = chatController.addUser(ROOM_ID, chatMessage, headerAccessor);

        // Assert
        assertEquals(MessageType.JOIN, result.getType(), "Message type should be JOIN");
        assertEquals("testUser", sessionAttributes.get("username"), "Username should be stored in session");
        assertEquals("avatarUrl", sessionAttributes.get("avtUrl"), "Avatar URL should be stored in session");
        assertEquals(ROOM_ID, sessionAttributes.get("roomId"), "Room ID should be stored in session");
    }

    @Test
    void testRemoveUser() {
        // Arrange
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        sessionAttributes.put("username", "testUser");
        sessionAttributes.put("avtUrl", "avatarUrl");
        ChatMessage chatMessage = new ChatMessage();

        // Act
        ChatMessage result = chatController.removeUser(ROOM_ID, chatMessage, headerAccessor);

        // Assert
        assertEquals(MessageType.LEAVE, result.getType(), "Message type should be LEAVE");
        assertEquals("testUser", result.getSender(), "Sender should match session username");
        assertEquals("avatarUrl", result.getAvtUrl(), "Avatar URL should match session avtUrl");
    }

    @Test
    void testRemoveUser_WithNullSessionAttributes() {
        // Arrange
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        sessionAttributes.clear();
        ChatMessage chatMessage = new ChatMessage();

        // Act
        ChatMessage result = chatController.removeUser(ROOM_ID, chatMessage, headerAccessor);

        // Assert
        assertEquals(MessageType.LEAVE, result.getType(), "Message type should be LEAVE");
        assertNull(result.getSender(), "Sender should be null");
        assertNull(result.getAvtUrl(), "Avatar URL should be null");
    }

    @Test
    void testVideoControl_ValidMessage() {
        // Arrange
        Map<String, Object> videoControlMessage = new HashMap<>();
        videoControlMessage.put("action", "play");
        videoControlMessage.put("time", 10.5);

        // Act
        chatController.videoControl(ROOM_ID, videoControlMessage);

        // Assert
        verify(messagingTemplate).convertAndSend(
                eq("/topic/video/" + ROOM_ID),
                eq(videoControlMessage)
        );
    }

    @Test
    void testVideoControl_InvalidMessage() {
        // Arrange
        Map<String, Object> videoControlMessage = new HashMap<>();
        videoControlMessage.put("action", "play");
        // Missing time parameter

        // Act
        chatController.videoControl(ROOM_ID, videoControlMessage);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(
                eq("/topic/video/" + ROOM_ID),
                any(Map.class)
        );
    }
}