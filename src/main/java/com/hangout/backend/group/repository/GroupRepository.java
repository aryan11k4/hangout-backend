package com.hangout.backend.group.repository;

import com.hangout.backend.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findByInviteCode(String inviteCode);
}
