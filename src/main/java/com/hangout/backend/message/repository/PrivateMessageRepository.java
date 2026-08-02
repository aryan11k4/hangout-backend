package com.hangout.backend.message.repository;

import com.hangout.backend.message.entity.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, UUID> {
}
