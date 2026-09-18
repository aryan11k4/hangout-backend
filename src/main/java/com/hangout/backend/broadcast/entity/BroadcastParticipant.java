package com.hangout.backend.broadcast.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A row exists for every join, and leftAt is set on leave rather than
 * deleting the row - keeps a simple watch-history trail for free. Current
 * viewer count = count of rows for this broadcast where leftAt IS NULL.
 */
@Entity
@Table(
        name = "broadcast_participants",
        indexes = {
                @Index(name = "idx_broadcast_participants_broadcast", columnList = "broadcast_id"),
                @Index(name = "idx_broadcast_participants_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class BroadcastParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "broadcast_id", nullable = false)
    private UUID broadcastId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;
}