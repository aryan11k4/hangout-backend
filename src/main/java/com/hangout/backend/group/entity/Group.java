package com.hangout.backend.group.entity;

import com.hangout.backend.common.enums.GroupVisibility;
import com.hangout.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "groups", uniqueConstraints = {
        @UniqueConstraint(name = "uk_groups_invite_code", columnNames = "invite_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "owner")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupVisibility visibility = GroupVisibility.PRIVATE;

    /** Only meaningful for invite-based joining; nullable for e.g. fully open public groups. */
    @Column(name = "invite_code", length = 20)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(length = 300)
    private String description;
}
