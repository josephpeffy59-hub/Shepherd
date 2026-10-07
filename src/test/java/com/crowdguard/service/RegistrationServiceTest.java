package com.crowdguard.service;

import com.crowdguard.model.AuthorityType;
import com.crowdguard.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {
    @Test void citizenInvalidDetailsDoNotStoreFilesOrHashPassword() {
        var users = mock(UserRepository.class);
        var authorities = mock(AuthorityRepository.class);
        var quarters = mock(QuarterRepository.class);
        var families = mock(FamilyRepository.class);
        var passwords = mock(PasswordEncoder.class);
        var files = mock(FileStorageService.class);
        var service = new UserService(users, quarters, families, passwords, files,
                new AccountValidation(users, authorities));
        assertThrows(RegistrationValidationException.class,
                () -> service.register("Citizen", "-1", "123456", UUID.randomUUID(), null, null, null));
        verifyNoInteractions(users, authorities, quarters, families, passwords, files);
    }

    @Test void authorityInvalidDetailsDoNotSaveAccount() {
        var users = mock(UserRepository.class);
        var authorities = mock(AuthorityRepository.class);
        var quarters = mock(QuarterRepository.class);
        var passwords = mock(PasswordEncoder.class);
        var service = new AuthorityService(authorities, quarters, passwords,
                new AccountValidation(users, authorities));
        assertThrows(RegistrationValidationException.class,
                () -> service.register("Officer", "name@example.com", "123456", "B123", AuthorityType.POLICE, UUID.randomUUID()));
        verifyNoInteractions(users, authorities, quarters, passwords);
    }
}
