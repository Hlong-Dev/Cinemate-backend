package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.util.JwtUtil;
import com.mycompany.mavenproject1.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            // Lấy thông tin avatar URL từ UserService sau khi xác thực thành công
            String avtUrl = userService.findByUsername(authentication.getName())
                    .map(user -> user.getAvtUrl())
                    .orElse("");

            // Tạo JWT với avatar URL
            String jwt = jwtUtil.generateJwtToken(authentication.getName(), avtUrl);

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (AuthenticationException e) {
            throw new AuthenticationServiceException("Invalid username or password");
        }
    }
}

@Getter
@Setter
class LoginRequest {
    private String username;
    private String password;
}

@Getter
class JwtResponse {
    private final String token;

    public JwtResponse(String token) {
        this.token = token;
    }
}