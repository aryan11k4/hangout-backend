package com.hangout.backend.group.repository;

import com.hangout.backend.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findByInviteCode(String inviteCode);

    Page<Group> findByVisibility(
            com.hangout.backend.common.enums.GroupVisibility visibility,
            org.springframework.data.domain.Pageable pageable);

    Page<Group> findByVisibilityAndNameContainingIgnoreCase(
            com.hangout.backend.common.enums.GroupVisibility visibility,
            String nameQuery,
            org.springframework.data.domain.Pageable pageable);
}
