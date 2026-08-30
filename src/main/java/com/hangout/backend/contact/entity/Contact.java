package com.hangout.backend.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Directional: "user_id has contact_user_id in their contact list."
 * Mutual contacts are represented as two rows (A->B and B->A), both created
 * together when a ContactRequest is accepted - see ContactService.
 */
@Entity
@Table(
        name = "contacts",
        indexes = {
                @Index(name = "idx_contacts_user", columnList = "user_id"),
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_contacts_pair", columnNames = {"user_id", "contact_user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "contact_user_id", nullable = false)
    private UUID contactUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}