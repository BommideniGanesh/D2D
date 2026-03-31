package com.example.demo.tracking;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.lang.NonNull;
import com.example.demo.userauthentication.JwtUtil;
import com.example.demo.userauthentication.CustomUserDetailsService;
import java.util.List;
import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    @Lazy
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private CustomUserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(@NonNull org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    boolean authenticated = false;
                    
                    // 1. Try JWT Auth
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String bearerToken = authHeaders.get(0);
                        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                            String token = bearerToken.substring(7);
                            if (jwtUtil.validateToken(token)) {
                                String username = jwtUtil.extractUsername(token);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                accessor.setUser(auth);
                                authenticated = true;
                            }
                        }
                    }
                    
                    // 2. Try Guest Auth fallback
                    if (!authenticated) {
                        List<String> guestHeaders = accessor.getNativeHeader("Guest-User");
                        if (guestHeaders != null && !guestHeaders.isEmpty()) {
                            String guestName = guestHeaders.get(0);
                            if (guestName != null && !guestName.isBlank()) {
                                UsernamePasswordAuthenticationToken guestAuth = new UsernamePasswordAuthenticationToken(
                                        guestName, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST")));
                                accessor.setUser(guestAuth);
                            }
                        }
                    }
                }
                return message;
            }
        });
    }


    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Allows frontend clients to establish websocket connection with SockJS fallback
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
