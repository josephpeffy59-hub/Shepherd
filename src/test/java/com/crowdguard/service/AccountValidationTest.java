package com.crowdguard.service;

import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountValidationTest {
    private UserRepository users;
    private AuthorityRepository authorities;
    private AccountValidation validation;

    @BeforeEach void setup() {
        users = mock(UserRepository.class);
        authorities = mock(AuthorityRepository.class);
        validation = new AccountValidation(users, authorities);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-1", "name", "name@host", "name@-host.com", "name@host-.com", "name..surname@example.com", "name@example..com", "name@example.com-1", "name @example.com"})
    void invalidEmailCannotRegister(String email) {
        var error = assertThrows(RegistrationValidationException.class,
                () -> validation.validateRegistration(email, "secret7"));
        assertTrue(error.getErrors().containsKey("email"));
        verifyNoInteractions(users, authorities);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123456", "       "})
    void invalidPasswordCannotRegister(String password) {
        var error = assertThrows(RegistrationValidationException.class,
                () -> validation.validateRegistration("name@example.com", password));
        assertTrue(error.getErrors().containsKey("password"));
        verifyNoInteractions(users, authorities);
    }

    @Test void acceptsSevenCharactersAndNormalizesEmail() {
        assertEquals("name@example.com", validation.validateRegistration(" NAME@Example.COM ", "1234567"));
    }

    @Test void acceptsValidHyphenAndPlusAddress() {
        assertEquals("name-1+test@example.com", validation.validateRegistration("name-1+test@example.com", "secret7"));
    }

    @Test void rejectsCitizenDuplicate() {
        when(users.existsByEmailIgnoreCase("name@example.com")).thenReturn(true);
        assertThrows(RegistrationValidationException.class,
                () -> validation.validateRegistration("NAME@example.com", "secret7"));
    }

    @Test void rejectsAuthorityDuplicate() {
        when(authorities.existsByEmailIgnoreCase("name@example.com")).thenReturn(true);
        assertThrows(RegistrationValidationException.class,
                () -> validation.validateRegistration("name@example.com", "secret7"));
    }

    @Test void rejectsPasswordAboveBcryptByteLimit() {
        var error = assertThrows(RegistrationValidationException.class,
                () -> validation.validateRegistration("name@example.com", "é".repeat(37)));
        assertTrue(error.getErrors().containsKey("password"));
    }
}
