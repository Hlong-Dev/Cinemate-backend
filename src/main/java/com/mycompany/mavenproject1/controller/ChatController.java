package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.ChatMessage;
import com.mycompany.mavenproject1.model.MessageType;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        // Xác định loại tin nhắn và gửi đi
        if (chatMessage.getType() == MessageType.IMAGE && chatMessage.getImage() != null && !chatMessage.getImage().isEmpty()) {
            chatMessage.setType(MessageType.IMAGE);
        } else if (chatMessage.getType() == MessageType.LEAVE) {
            chatMessage.setType(MessageType.LEAVE);
        } else {
            chatMessage.setType(MessageType.CHAT);
        }
        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Lưu thông tin người dùng vào session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("avtUrl", chatMessage.getAvtUrl());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        chatMessage.setType(MessageType.JOIN);
        return chatMessage;
    }

    @MessageMapping("/chat.removeUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage removeUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage,
                                  SimpMessageHeaderAccessor headerAccessor) {
        // Lấy thông tin từ session
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String avtUrl = (String) headerAccessor.getSessionAttributes().get("avtUrl");

        // Kiểm tra null cho các giá trị từ session
        if (username != null) {
            chatMessage.setSender(username);
        }
        if (avtUrl != null) {
            chatMessage.setAvtUrl(avtUrl);
        }
        chatMessage.setType(MessageType.LEAVE);
        return chatMessage;
    }

    @MessageMapping("/video.control/{roomId}")
    public void videoControl(@DestinationVariable String roomId, @Payload Map<String, Object> videoControlMessage) {
        // Kiểm tra và gửi thông điệp điều khiển video
        if (videoControlMessage.containsKey("action") && videoControlMessage.containsKey("time")) {
            messagingTemplate.convertAndSend("/topic/video/" + roomId, videoControlMessage);
        } else {
            System.err.println("Invalid video control message received: " + videoControlMessage);
        }
    }
}
