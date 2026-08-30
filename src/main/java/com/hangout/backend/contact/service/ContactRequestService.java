package com.hangout.backend.contact.service;

import com.hangout.backend.common.enums.ContactRequestStatus;
import com.hangout.backend.common.exception.DuplicateResourceException;
import com.hangout.backend.common.exception.InvalidRequestException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.contact.dto.ContactRequestResponseDto;
import com.hangout.backend.contact.dto.ContactUserDto;
import com.hangout.backend.contact.entity.Contact;
import com.hangout.backend.contact.entity.ContactRequest;
import com.hangout.backend.contact.repository.ContactRepository;
import com.hangout.backend.contact.repository.ContactRequestRepository;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactRequestService {

    private final ContactRequestRepository contactRequestRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    /**
     * Sends a contact request. Handles two special cases up front:
     * - If the receiver already sent *this* sender a pending request, the
     *   crossing requests auto-accept instead of creating a second pending
     *   request (per product decision - avoids two people staring at
     *   pending requests for each other when they clearly both want to connect).
     * - If already contacts, or a PENDING request already exists in this
     *   exact direction, reject with 409.
     */
    @Transactional
    public ContactRequestResponseDto sendRequest(UUID senderId, UUID receiverId) {
        if (senderId.equals(receiverId)) {
            throw new InvalidRequestException("Cannot send a contact request to yourself");
        }

        userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (contactRepository.existsByUserIdAndContactUserId(senderId, receiverId)) {
            throw new DuplicateResourceException("You are already contacts with this user");
        }

        if (contactRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                senderId, receiverId, ContactRequestStatus.PENDING)) {
            throw new DuplicateResourceException("A pending request already exists");
        }

        // crossing request check: did the receiver already request the sender?
        var reverseRequest = contactRequestRepository.findBySenderIdAndReceiverIdAndStatus(
                receiverId, senderId, ContactRequestStatus.PENDING);

        if (reverseRequest.isPresent()) {
            ContactRequest existing = reverseRequest.get();
            existing.setStatus(ContactRequestStatus.ACCEPTED);
            contactRequestRepository.save(existing);
            createMutualContact(existing.getSenderId(), existing.getReceiverId());

            User sender = userRepository.findById(senderId).orElseThrow();
            return toDto(existing, sender);
        }

        ContactRequest request = ContactRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(ContactRequestStatus.PENDING)
                .build();
        ContactRequest saved = contactRequestRepository.saveAndFlush(request);

        User sender = userRepository.findById(senderId).orElseThrow();
        return toDto(saved, sender);
    }

    public List<ContactRequestResponseDto> getReceivedRequests(UUID receiverId) {
        return contactRequestRepository.findByReceiverIdAndStatus(receiverId, ContactRequestStatus.PENDING)
                .stream()
                .map(req -> {
                    User sender = userRepository.findById(req.getSenderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
                    return toDto(req, sender);
                })
                .toList();
    }

    @Transactional
    public void acceptRequest(UUID currentUserId, UUID requestId) {
        ContactRequest request = getPendingRequestOwnedByReceiver(currentUserId, requestId);
        request.setStatus(ContactRequestStatus.ACCEPTED);
        contactRequestRepository.save(request);
        createMutualContact(request.getSenderId(), request.getReceiverId());
    }

    @Transactional
    public void rejectRequest(UUID currentUserId, UUID requestId) {
        ContactRequest request = getPendingRequestOwnedByReceiver(currentUserId, requestId);
        request.setStatus(ContactRequestStatus.REJECTED);
        contactRequestRepository.save(request);
    }

    private ContactRequest getPendingRequestOwnedByReceiver(UUID currentUserId, UUID requestId) {
        ContactRequest request = contactRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact request not found"));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new InvalidRequestException("You are not the receiver of this request");
        }
        if (request.getStatus() != ContactRequestStatus.PENDING) {
            throw new InvalidRequestException("This request has already been " + request.getStatus());
        }
        return request;
    }

    private void createMutualContact(UUID userA, UUID userB) {
        if (!contactRepository.existsByUserIdAndContactUserId(userA, userB)) {
            contactRepository.save(Contact.builder().userId(userA).contactUserId(userB).build());
        }
        if (!contactRepository.existsByUserIdAndContactUserId(userB, userA)) {
            contactRepository.save(Contact.builder().userId(userB).contactUserId(userA).build());
        }
    }

    private ContactRequestResponseDto toDto(ContactRequest request, User sender) {
        return new ContactRequestResponseDto(
                request.getId(),
                new ContactUserDto(sender.getId(), sender.getUsername(), sender.getContactCode()),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}