package com.crowdguard.service;

import com.crowdguard.model.Family;
import com.crowdguard.model.User;
import com.crowdguard.repository.FamilyRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepo;
    private final UserRepository userRepo;

    @Transactional
    public Family createFamily(User owner, String name) {
        if (owner.getFamily() != null) return owner.getFamily();
        if (name == null || name.isBlank()) {
            throw new RegistrationValidationException(Map.of("family", "Enter a name for your family group."));
        }
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Family f = Family.builder()
                .inviteCode(code)
                .name(name.strip())
                .build();
        familyRepo.save(f);
        owner.setFamily(f);
        userRepo.save(owner);
        return f;
    }
}
