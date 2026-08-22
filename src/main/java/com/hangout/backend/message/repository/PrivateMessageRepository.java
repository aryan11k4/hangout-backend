package com.hangout.backend.message.repository;

import com.hangout.backend.message.entity.PrivateMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, UUID> {

    @Query("""
            SELECT m FROM PrivateMessage m
            WHERE (m.senderId = :userA AND m.receiverId = :userB)
               OR (m.senderId = :userB AND m.receiverId = :userA)
            ORDER BY m.createdAt DESC
            """)
    Page<PrivateMessage> findConversation(@Param("userA") UUID userA,
                                          @Param("userB") UUID userB,
                                          Pageable pageable);
}