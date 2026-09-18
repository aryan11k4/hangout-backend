package com.hangout.backend.broadcast.entity;

import com.hangout.backend.common.enums.BroadcastStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * currentPosition/isPlaying are kept up to date on every PLAY/PAUSE/SEEK
 * WS event (see BroadcastPlaybackWebSocketController), not just at
 * creation - so a REST GET always reflects live state for a late joiner,
 * not just the state at broadcast start.
 */
@Entity
@Table(
        name = "broadcasts",
        indexes = {
                @Index(name = "idx_broadcasts_group", columnList = "group_id"),
                @Index(name = "idx_broadcasts_host", columnList = "host_id"),
                @Index(name = "idx_broadcasts_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Broadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "host_id", nullable = false)
    private UUID hostId;

    @Column(name = "youtube_video_id", nullable = false, length = 50)
    private String youtubeVideoId;

    /** Denormalized from the YouTube search result at creation time - avoids
     *  re-hitting the YouTube API just to render a broadcast list/card. */
    @Column(name = "video_title", length = 500)
    private String videoTitle;

    @Column(name = "video_thumbnail_url", length = 500)
    private String videoThumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BroadcastStatus status = BroadcastStatus.ACTIVE;

    @Column(name = "current_position", nullable = false)
    @Builder.Default
    private double currentPosition = 0.0;

    @Column(name = "is_playing", nullable = false)
    @Builder.Default
    private boolean playing = false;

    /** Server-side timestamp of the last PLAY/PAUSE/SEEK event - used for the
     *  "position + elapsed time since last event" live-position calculation. */
    @Column(name = "last_event_at")
    private Instant lastEventAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}