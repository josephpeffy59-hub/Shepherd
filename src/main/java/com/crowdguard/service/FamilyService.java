package com.crowdguard.service;

import com.crowdguard.model.Family;
import com.crowdguard.model.User;
import com.crowdguard.repository.FamilyRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepo;
    private final UserRepository userRepo;

    public Family createFamily(User owner, String name) {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Family f = Family.builder()
                .inviteCode(code)
                .name(name)
                .build();
        familyRepo.save(f);
        owner.setFamily(f);
        userRepo.save(owner);
        return f;
    }
}