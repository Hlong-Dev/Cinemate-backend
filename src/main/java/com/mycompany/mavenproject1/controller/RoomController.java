package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.Room;
import com.mycompany.mavenproject1.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:3000") // Cho phép mọi nguồn gốc (origin) kết nối
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // Lấy danh sách tất cả các phòng
    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
 @GetMapping("/{id}")
    public Room getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id).orElse(null);
    }
   @PostMapping
public Room createRoom(@AuthenticationPrincipal UserDetails userDetails) {
    Room room = new Room();
    room.setOwnerUsername(userDetails.getUsername());
    room.setName("Phòng của " + userDetails.getUsername()); // Thiết lập tên phòng mặc định
    return roomRepository.save(room);
}


    // Xóa phòng nếu người dùng thoát là chủ phòng
   @Autowired
private SimpMessagingTemplate messagingTemplate;

@DeleteMapping("/{id}")
public void deleteRoomIfOwner(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    Room room = roomRepository.findById(id).orElse(null);
    if (room != null && room.getOwnerUsername().equals(userDetails.getUsername())) {
        roomRepository.delete(room);
        // Gửi thông báo dưới dạng JSON hợp lệ
        messagingTemplate.convertAndSend("/topic/" + id, "{\"type\": \"OWNER_LEFT\"}");
    }
}


}
