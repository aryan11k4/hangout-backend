package com.hangout.backend.broadcast.repository;

import com.hangout.backend.broadcast.entity.BroadcastParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BroadcastParticipantRepository extends JpaRepository<BroadcastParticipant, Long> {

    /**
     * Was Optional<BroadcastParticipant> - changed to List because nothing
     * (until the DB constraint in Part B lands) prevents more than one open
     * row existing for the same (broadcastId, userId) pair. A single-result
     * query method throws NonUniqueResultException the moment a second open
     * row exists, which is exactly what was producing the 500 on every
     * subsequent join() call once a duplicate had been created by a race.
     * Treat "any rows returned" as "already joined", regardless of count.
     */
    List<BroadcastParticipant> findByBroadcastIdAndUserIdAndLeftAtIsNull(UUID broadcastId, UUID userId);

    List<BroadcastParticipant> findByBroadcastIdAndLeftAtIsNull(UUID broadcastId);

    long countByBroadcastIdAndLeftAtIsNull(UUID broadcastId);
}