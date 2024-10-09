package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

   @PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody User user) {
    if (userService.findByUsername(user.getUsername()).isPresent()) {
        return ResponseEntity.badRequest().body("Username already exists");
    }

    try {
        userService.save(user);
        userService.setDefaultRole(user.getUsername());
        return ResponseEntity.ok("User registered successfully");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request.");
    }
}


    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(authentication.getPrincipal());
        } else {
            return ResponseEntity.ok().build();
        }
    }
    
}
