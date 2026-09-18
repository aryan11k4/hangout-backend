package com.hangout.backend.broadcast.repository;

import com.hangout.backend.broadcast.entity.BroadcastMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, UUID> {

    Page<BroadcastMessage> findByBroadcastIdOrderByCreatedAtDesc(UUID broadcastId, Pageable pageable);
}