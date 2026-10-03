package com.crowdguard.service;

import com.crowdguard.model.Authority;
import com.crowdguard.model.AuthorityType;
import com.crowdguard.model.Quarter;
import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.QuarterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository repo;
    private final QuarterRepository quarterRepo;
    private final PasswordEncoder encoder;

    public Authority register(String fullName, String email, String rawPassword,
                              String badge, AuthorityType type, UUID quarterId) {
        Quarter q = quarterRepo.findById(quarterId).orElseThrow();
        Authority a = Authority.builder()
                .fullName(fullName)
                .email(email)
                .password(encoder.encode(rawPassword))
                .badgeNumber(badge)
                .type(type)
                .quarter(q)
                .build();
        return repo.save(a);
    }
}