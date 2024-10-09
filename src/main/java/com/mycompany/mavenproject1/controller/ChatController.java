package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.ChatMessage;
import com.mycompany.mavenproject1.model.MessageType;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
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
        // Lưu roomId vào session để sử dụng khi người dùng rời đi
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId); // Lưu roomId

        chatMessage.setType(MessageType.JOIN);
        return chatMessage;
    }

    @MessageMapping("/chat.removeUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage removeUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        chatMessage.setSender(username);
        chatMessage.setType(MessageType.LEAVE);
        return chatMessage;
    }
}
