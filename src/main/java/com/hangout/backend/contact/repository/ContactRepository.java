package com.hangout.backend.contact.repository;

import com.hangout.backend.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByUserId(UUID userId);

    boolean existsByUserIdAndContactUserId(UUID userId, UUID contactUserId);
}