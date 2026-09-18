package com.hangout.backend.broadcast.repository;

import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.common.enums.BroadcastStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {

    List<Broadcast> findByGroupIdAndStatus(UUID groupId, BroadcastStatus status);

    Optional<Broadcast> findByIdAndStatus(UUID id, BroadcastStatus status);

    List<Broadcast> findByHostIdAndStatus(UUID hostId, BroadcastStatus status);
}