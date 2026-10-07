package com.crowdguard.service;

import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AccountValidation {
    // Practical address format: local part, hostname labels, and a domain suffix.
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9]+(?:[._%+\\-][A-Za-z0-9]+)*@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])?\\.)+"
            + "[A-Za-z]{2,63}$");

    private final UserRepository users;
    private final AuthorityRepository authorities;

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        String normalized = normalizeEmail(email);
        int at = normalized.indexOf('@');
        return normalized.length() <= 254 && at > 0 && at <= 64
                && EMAIL.matcher(normalized).matches();
    }

    public String validateRegistration(String email, String password) {
        String normalized = normalizeEmail(email);
        Map<String, String> errors = new LinkedHashMap<>();
        if (!isValidEmail(normalized)) {
            errors.put("email", "Enter a valid email address, such as name@example.com.");
        }
        if (password == null || password.isBlank() || password.length() < 7) {
            errors.put("password", "Your password must contain at least 7 characters and cannot be only spaces.");
        } else if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            errors.put("password", "Your password is too long. Use no more than 72 UTF-8 bytes.");
        }
        if (errors.isEmpty() && (users.existsByEmailIgnoreCase(normalized)
                || authorities.existsByEmailIgnoreCase(normalized))) {
            errors.put("email", "This email is already registered. Sign in or use another email.");
        }
        if (!errors.isEmpty()) {
            throw new RegistrationValidationException(errors);
        }
        return normalized;
    }
}
