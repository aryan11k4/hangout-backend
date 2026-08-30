package com.hangout.backend.contact.repository;

import com.hangout.backend.common.enums.ContactRequestStatus;
import com.hangout.backend.contact.entity.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, UUID> {

    List<ContactRequest> findByReceiverIdAndStatus(UUID receiverId, ContactRequestStatus status);

    Optional<ContactRequest> findBySenderIdAndReceiverIdAndStatus(
            UUID senderId, UUID receiverId, ContactRequestStatus status);

    boolean existsBySenderIdAndReceiverIdAndStatus(
            UUID senderId, UUID receiverId, ContactRequestStatus status);
}