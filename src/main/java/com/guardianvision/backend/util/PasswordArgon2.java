package com.guardianvision.backend.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class PasswordArgon2 {
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int PARALLELISM = 1;
    private static final int MEMORY = 1 << 14;
    private static final int ITERATIONS = 3;

    public boolean matchPasswords(String salt, String rawPassword, String hash) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY, ITERATIONS);
        boolean result;
        rawPassword = salt + rawPassword;
        if (encoder.matches(rawPassword, hash)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public static String encryptPassword(String password, String salt) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY, ITERATIONS);
        String saltedRawPassword = salt + password;
        Instant start = Instant.now();  // start timer
        String hash = encoder.encode(saltedRawPassword);
        System.out.println("Hash: " + hash);
        Instant end = Instant.now();    // end timer
        System.out.println(String.format(
                "Hashing took %s ms",
                ChronoUnit.MILLIS.between(start, end)
        ));
        return hash;
    }

    public static String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        System.out.println("Salt: " + encodedSalt);
        return encodedSalt;
    }
}
