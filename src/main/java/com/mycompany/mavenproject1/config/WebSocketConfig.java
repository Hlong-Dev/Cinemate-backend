package com.mycompany.mavenproject1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemHeartbeatReceiveInterval(20000) // 20 giây
                .setSystemHeartbeatSendInterval(20000);   // 20 giây
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Không dùng SockJS nữa
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "https://hlong-cinemate.vercel.app/",
                        "http://localhost:3000"
                );
    }
}

