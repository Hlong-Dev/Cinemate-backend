package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.VideoUpdateRequest;
import com.mycompany.mavenproject1.model.Room;
import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.repository.IUserRepository;
import com.mycompany.mavenproject1.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(
        origins = {"http://localhost:3000", "https://hlong-cinemate.vercel.app"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowCredentials = "true"
)
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;
 @Autowired
    private IUserRepository userRepository;
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

    // Lấy avtUrl từ thông tin người dùng
    String username = userDetails.getUsername();
    Optional<User> userOptional = userRepository.findByUsername(username); // Tìm người dùng trong database
    
    if (userOptional.isPresent()) {
        room.setThumbnail(userOptional.get().getAvtUrl()); // Thiết lập thumbnail của phòng là avtUrl của chủ phòng
    } else {
        room.setThumbnail("https://i.imgur.com/Tr9qnkI.jpeg"); // Avatar mặc định nếu không tìm thấy
    }

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

    @PostMapping("/{roomId}/update-video")
    public ResponseEntity<?> updateRoomVideo(
            @PathVariable Long roomId,
            @RequestBody VideoUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Room> roomOptional = roomRepository.findById(roomId);

        if (!roomOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Room room = roomOptional.get();

        // Loại bỏ điều kiện kiểm tra chủ phòng
        // Bây giờ bất kỳ user đã đăng nhập nào cũng có thể update video

        // Cập nhật thông tin video
        room.setCurrentVideoUrl(request.getCurrentVideoUrl());
        room.setCurrentVideoTitle(request.getCurrentVideoTitle());

        // Lưu vào database
        roomRepository.save(room);

        return ResponseEntity.ok().build();
    }

}
