/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.controller;

/**
 *
 * @author hlongday
 */
import com.mycompany.mavenproject1.model.ChatMessage;
import com.mycompany.mavenproject1.model.MessageType;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        String avtUrl = (String) headerAccessor.getSessionAttributes().get("avtUrl"); // Lấy avtUrl từ session

        if (username != null && roomId != null) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender(username);
            chatMessage.setAvtUrl(avtUrl); // Đảm bảo avatar URL được thiết lập cho tin nhắn
            chatMessage.setType(MessageType.LEAVE);

            // In ra log kiểm tra thông tin
            System.out.println("User " + username + " has disconnected from room " + roomId);

            // Gửi tin nhắn đến tất cả các thành viên trong phòng, sử dụng roomId
            messagingTemplate.convertAndSend("/topic/" + roomId, chatMessage);
        }
    }
}


