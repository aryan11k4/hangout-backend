package com.hangout.backend.group.repository;

import com.hangout.backend.group.entity.JoinRequest;
import com.hangout.backend.common.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, UUID> {

    Optional<JoinRequest> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<JoinRequest> findByGroupIdAndStatus(UUID groupId, JoinRequestStatus status);
}
