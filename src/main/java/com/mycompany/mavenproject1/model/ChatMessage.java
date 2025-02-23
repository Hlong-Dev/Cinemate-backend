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
    private MessageType type;

    @Column(name = "avt_url")
    private String avtUrl; // Lưu URL avatar người gửi

    // **Thêm liên kết với tin nhắn được reply**
    @ManyToOne
    @JoinColumn(name = "reply_to_id", referencedColumnName = "id", nullable = true)
    private ChatMessage replyTo;
}
