package com.hangout.backend.broadcast.service;

import com.hangout.backend.broadcast.dto.*;
import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.entity.BroadcastParticipant;
import com.hangout.backend.broadcast.repository.BroadcastParticipantRepository;
import com.hangout.backend.broadcast.repository.BroadcastRepository;
import com.hangout.backend.common.enums.BroadcastStatus;
import com.hangout.backend.common.enums.PlaybackEventType;
import com.hangout.backend.common.exception.ForbiddenException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BroadcastService {

    private final BroadcastRepository broadcastRepository;
    private final BroadcastParticipantRepository participantRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String BROADCAST_TOPIC_PREFIX = "/topic/broadcast.";
    private static final String GROUP_BROADCASTS_TOPIC_PREFIX = "/topic/group.";
    private static final String GROUP_BROADCASTS_TOPIC_SUFFIX = ".broadcasts";

    @Transactional
    public BroadcastResponseDto createBroadcast(UUID hostId, UUID groupId, CreateBroadcastDto request) {
        requireGroupMember(groupId, hostId);

        Broadcast broadcast = Broadcast.builder()
                .groupId(groupId)
                .hostId(hostId)
                .youtubeVideoId(request.videoId())
                .videoTitle(request.title())
                .videoThumbnailUrl(request.thumbnailUrl())
                .status(BroadcastStatus.ACTIVE)
                .currentPosition(0.0)
                .playing(false)
                .lastEventAt(Instant.now())
                .build();
        Broadcast saved = broadcastRepository.saveAndFlush(broadcast);

        participantRepository.save(BroadcastParticipant.builder()
                .broadcastId(saved.getId())
                .userId(hostId)
                .build());

        BroadcastSummaryDto summary = toSummaryDto(saved);
        messagingTemplate.convertAndSend(
                GROUP_BROADCASTS_TOPIC_PREFIX + groupId + GROUP_BROADCASTS_TOPIC_SUFFIX,
                BroadcastStartedEventDto.of(summary)
        );

        return toResponseDto(saved);
    }

    public List<BroadcastSummaryDto> getActiveBroadcasts(UUID requesterId, UUID groupId) {
        requireGroupMember(groupId, requesterId);
        return broadcastRepository.findByGroupIdAndStatus(groupId, BroadcastStatus.ACTIVE).stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public BroadcastResponseDto getBroadcast(UUID requesterId, UUID broadcastId) {
        Broadcast broadcast = getActiveOrThrow(broadcastId);
        requireGroupMember(broadcast.getGroupId(), requesterId);
        return toResponseDto(broadcast);
    }

    @Transactional
    public JoinBroadcastResponseDto joinBroadcast(UUID userId, UUID broadcastId) {
        Broadcast broadcast = getActiveOrThrow(broadcastId);
        requireGroupMember(broadcast.getGroupId(), userId);

        List<BroadcastParticipant> existingOpenParticipants =
                participantRepository.findByBroadcastIdAndUserIdAndLeftAtIsNull(broadcastId, userId);
        boolean alreadyIn = !existingOpenParticipants.isEmpty();

        if (!alreadyIn) {
            try {
                participantRepository.save(BroadcastParticipant.builder()
                        .broadcastId(broadcastId)
                        .userId(userId)
                        .build());

                User user = userRepository.findById(userId).orElseThrow();
                long viewerCount = participantRepository.countByBroadcastIdAndLeftAtIsNull(broadcastId);
                messagingTemplate.convertAndSend(
                        BROADCAST_TOPIC_PREFIX + broadcastId,
                        new ViewerEventDto(PlaybackEventType.VIEWER_JOINED, user.getUsername(), viewerCount)
                );
            } catch (DataIntegrityViolationException e) {
                // Lost the race to a concurrent join() for the same user -
                // the DB-level partial unique index rejected our insert
                // because another request's insert for the same
                // (broadcastId, userId) already landed first. The user is
                // joined either way, just not by this call.
            }
        }

        double livePosition = computeLivePosition(broadcast);
        return new JoinBroadcastResponseDto(broadcastId, livePosition, broadcast.isPlaying(), Instant.now());
    }

    @Transactional
    public void leaveBroadcast(UUID userId, UUID broadcastId) {
        Broadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found"));

        // Returns a List, not an Optional - duplicate open rows are
        // possible (historically, and the DB constraint only blocks NEW
        // duplicates going forward). Close out every open row for this
        // user so leaving reliably clears their "joined" state regardless
        // of how many open rows happen to exist.
        List<BroadcastParticipant> openParticipants =
                participantRepository.findByBroadcastIdAndUserIdAndLeftAtIsNull(broadcastId, userId);

        if (!openParticipants.isEmpty()) {
            Instant now = Instant.now();
            openParticipants.forEach(participant -> participant.setLeftAt(now));
            participantRepository.saveAll(openParticipants);

            User user = userRepository.findById(userId).orElseThrow();
            long viewerCount = participantRepository.countByBroadcastIdAndLeftAtIsNull(broadcastId);
            messagingTemplate.convertAndSend(
                    BROADCAST_TOPIC_PREFIX + broadcastId,
                    new ViewerEventDto(PlaybackEventType.VIEWER_LEFT, user.getUsername(), viewerCount)
            );
        }

        if (broadcast.getHostId().equals(userId) && broadcast.getStatus() == BroadcastStatus.ACTIVE) {
            endBroadcastInternal(broadcast, "HOST_LEFT");
        }
    }

    @Transactional
    public void endBroadcast(UUID actorId, UUID broadcastId) {
        Broadcast broadcast = getActiveOrThrow(broadcastId);
        if (!broadcast.getHostId().equals(actorId)) {
            throw new ForbiddenException("Only the host can end this broadcast");
        }
        endBroadcastInternal(broadcast, "HOST_ENDED");
    }

    @Transactional
    public void endBroadcastInternal(Broadcast broadcast, String reason) {
        broadcast.setStatus(BroadcastStatus.ENDED);
        broadcast.setEndedAt(Instant.now());
        broadcastRepository.save(broadcast);

        messagingTemplate.convertAndSend(
                BROADCAST_TOPIC_PREFIX + broadcast.getId(),
                new BroadcastEndedDto(broadcast.getId(), reason)
        );

        messagingTemplate.convertAndSend(
                GROUP_BROADCASTS_TOPIC_PREFIX + broadcast.getGroupId() + GROUP_BROADCASTS_TOPIC_SUFFIX,
                BroadcastEndedListEventDto.of(broadcast.getId())
        );
    }

    public List<BroadcastParticipantDto> getViewers(UUID requesterId, UUID broadcastId) {
        Broadcast broadcast = getActiveOrThrow(broadcastId);
        requireGroupMember(broadcast.getGroupId(), requesterId);

        return participantRepository.findByBroadcastIdAndLeftAtIsNull(broadcastId).stream()
                .map(p -> {
                    User u = userRepository.findById(p.getUserId()).orElseThrow();
                    return new BroadcastParticipantDto(u.getId(), u.getUsername());
                })
                .toList();
    }

    private double computeLivePosition(Broadcast broadcast) {
        if (!broadcast.isPlaying() || broadcast.getLastEventAt() == null) {
            return broadcast.getCurrentPosition();
        }
        double elapsedSeconds = (Instant.now().toEpochMilli() - broadcast.getLastEventAt().toEpochMilli()) / 1000.0;
        return broadcast.getCurrentPosition() + elapsedSeconds;
    }

    private Broadcast getActiveOrThrow(UUID broadcastId) {
        return broadcastRepository.findByIdAndStatus(broadcastId, BroadcastStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active broadcast not found"));
    }

    private void requireGroupMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("You are not a member of this group");
        }
    }

    private BroadcastResponseDto toResponseDto(Broadcast broadcast) {
        User host = userRepository.findById(broadcast.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));
        long viewerCount = participantRepository.countByBroadcastIdAndLeftAtIsNull(broadcast.getId());

        return new BroadcastResponseDto(
                broadcast.getId(),
                broadcast.getGroupId(),
                broadcast.getHostId(),
                host.getUsername(),
                broadcast.getYoutubeVideoId(),
                broadcast.getVideoTitle(),
                broadcast.getVideoThumbnailUrl(),
                broadcast.getStatus(),
                computeLivePosition(broadcast),
                broadcast.isPlaying(),
                viewerCount,
                Instant.now(),
                broadcast.getCreatedAt()
        );
    }

    private BroadcastSummaryDto toSummaryDto(Broadcast broadcast) {
        User host = userRepository.findById(broadcast.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));
        long viewerCount = participantRepository.countByBroadcastIdAndLeftAtIsNull(broadcast.getId());

        return new BroadcastSummaryDto(
                broadcast.getId(),
                broadcast.getHostId(),
                host.getUsername(),
                broadcast.getYoutubeVideoId(),
                broadcast.getVideoTitle(),
                broadcast.getVideoThumbnailUrl(),
                viewerCount
        );
    }
}