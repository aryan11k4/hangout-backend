package com.hangout.backend.message.repository;

import com.hangout.backend.message.entity.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, UUID> {

    Page<GroupMessage> findByGroupIdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);
}