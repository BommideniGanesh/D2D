package com.example.demo.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Get all messages between two users (Sender->Receiver AND Receiver->Sender)
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :user1 AND m.receiverId = :user2) OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);

    // Find all distinct users that a particular user has chatted with
    @Query("SELECT DISTINCT m.senderId FROM ChatMessage m WHERE m.receiverId = :userId " +
           "UNION " +
           "SELECT DISTINCT m.receiverId FROM ChatMessage m WHERE m.senderId = :userId")
    List<String> findDistinctContacts(@Param("userId") String userId);
}
