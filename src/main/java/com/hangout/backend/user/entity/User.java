package com.hangout.backend.user.entity;

import com.hangout.backend.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "password")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)  //made it  nullable
    private String password;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;

    @Column(length = 500)
    private String bio;

    @Column(name = "contact_code", length = 3)
    private String contactCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private String authProvider = "LOCAL"; // "LOCAL" or "GOOGLE" - purely informational,
    // doesn't gate anything by itself since a user
    // can have both a password AND a googleId
}
