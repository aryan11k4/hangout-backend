package com.hangout.backend.group.service;

import java.security.SecureRandom;

/** Generates short, human-typeable invite codes for PRIVATE groups. */
public final class InviteCodeGenerator {

    // No 0/O/1/I - avoids visually-ambiguous codes.
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private InviteCodeGenerator() {}

    public static String generate() {
        return generate(8);
    }

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
