package com.example.demo.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setStatus(ChatMessage.MessageStatus.DELIVERED);
        
        // Save to Database
        ChatMessage savedMsg = chatMessageRepository.save(chatMessage);
        
        // Route to the specific user's queue OR to the support topic
        String receiverId = chatMessage.getReceiverId();
        if (receiverId == null || receiverId.isBlank()) return;

        if ("OPERATION_SUPPORT".equals(receiverId)) {
            messagingTemplate.convertAndSend("/topic/support", savedMsg);
            // Echo back to sender so they receive server-confirmed message (with ID/timestamp)
            messagingTemplate.convertAndSendToUser(
                    savedMsg.getSenderId(),
                    "/queue/messages",
                    savedMsg
            );
        } else {
            messagingTemplate.convertAndSendToUser(
                    receiverId,
                    "/queue/messages",
                    savedMsg
            );
        }
    }

    @GetMapping("/api/chat/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String user1, 
            @PathVariable String user2) {
        return ResponseEntity.ok(chatMessageRepository.findChatHistory(user1, user2));
    }

    @GetMapping("/api/chat/contacts/{userId}")
    public ResponseEntity<List<String>> getDistinctContacts(@PathVariable String userId) {
        return ResponseEntity.ok(chatMessageRepository.findDistinctContacts(userId));
    }
}
