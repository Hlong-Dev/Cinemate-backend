package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.VideoMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class Video2Controller {

    @MessageMapping("/chat.videoUpdate/{roomId}")  // Lắng nghe tại đường dẫn "/app/chat.videoUpdate/{roomId}"
    @SendTo("/topic/{roomId}")  // Gửi tin nhắn tới các người dùng trong "/topic/{roomId}"
    public VideoMessage handleVideoUpdate(VideoMessage videoMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Xử lý thông tin về video được gửi từ client (ví dụ: chủ phòng gửi trạng thái)
        return videoMessage;  // Trả lại message để gửi tới tất cả các client
    }
}
