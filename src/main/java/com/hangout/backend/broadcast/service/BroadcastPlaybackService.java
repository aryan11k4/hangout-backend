package com.hangout.backend.broadcast.service;

import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.repository.BroadcastRepository;
import com.hangout.backend.common.enums.BroadcastStatus;
import com.hangout.backend.common.exception.ForbiddenException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.group.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Owns all mutation of live Broadcast playback state. Every PLAY/PAUSE/SEEK
 * updates currentPosition + isPlaying + lastEventAt on the entity itself, so
 * a late REST GET (or a fresh WS subscriber via requestSync) always reflects
 * the true current state rather than stale data from broadcast creation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BroadcastPlaybackService {

    private final BroadcastRepository broadcastRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public Broadcast applyPlayPause(UUID userId, UUID broadcastId, double position, boolean isPlaying) {
        Broadcast broadcast = getActiveAndRequireHost(userId, broadcastId);

        broadcast.setCurrentPosition(position);
        broadcast.setPlaying(isPlaying);
        broadcast.setLastEventAt(Instant.now());

        return broadcastRepository.save(broadcast);
    }

    @Transactional
    public Broadcast applySeek(UUID userId, UUID broadcastId, double position) {
        Broadcast broadcast = getActiveAndRequireHost(userId, broadcastId);

        broadcast.setCurrentPosition(position);
        broadcast.setLastEventAt(Instant.now());
        // isPlaying deliberately untouched - a seek doesn't imply play or pause

        return broadcastRepository.save(broadcast);
    }

    public Broadcast getForSync(UUID userId, UUID broadcastId) {
        Broadcast broadcast = broadcastRepository.findByIdAndStatus(broadcastId, BroadcastStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active broadcast not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(broadcast.getGroupId(), userId)) {
            throw new ForbiddenException("You are not a member of this group");
        }
        return broadcast;
    }

    public double computeLivePosition(Broadcast broadcast) {
        if (!broadcast.isPlaying() || broadcast.getLastEventAt() == null) {
            return broadcast.getCurrentPosition();
        }
        double elapsedSeconds = (Instant.now().toEpochMilli() - broadcast.getLastEventAt().toEpochMilli()) / 1000.0;
        return broadcast.getCurrentPosition() + elapsedSeconds;
    }

    private Broadcast getActiveAndRequireHost(UUID userId, UUID broadcastId) {
        Broadcast broadcast = broadcastRepository.findByIdAndStatus(broadcastId, BroadcastStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active broadcast not found"));

        if (!broadcast.getHostId().equals(userId)) {
            throw new ForbiddenException("Only the host can control playback");
        }
        return broadcast;
    }
}