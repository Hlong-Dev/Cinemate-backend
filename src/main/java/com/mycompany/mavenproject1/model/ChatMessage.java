package com.mycompany.mavenproject1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor  // Constructor không tham số
@AllArgsConstructor // Constructor có tất cả các tham số
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "sender")
    private String sender;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "image")
    private String image;  // Lưu URL của ảnh sau khi upload lên FTP

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MessageType type; // Thêm trường này để lưu loại tin nhắn
}
