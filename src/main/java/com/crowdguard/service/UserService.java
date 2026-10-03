package com.crowdguard.service;

import com.crowdguard.model.Quarter;
import com.crowdguard.model.User;
import com.crowdguard.repository.FamilyRepository;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final QuarterRepository quarterRepo;
    private final FamilyRepository familyRepo;
    private final PasswordEncoder encoder;
    private final FileStorageService fileStorage;

    public User register(String fullName, String email, String rawPassword,
                         UUID quarterId, MultipartFile id1, MultipartFile id2,
                         String inviteCode) {
        Quarter q = quarterRepo.findById(quarterId).orElseThrow();
        User u = User.builder()
                .fullName(fullName)
                .email(email)
                .password(encoder.encode(rawPassword))
                .quarter(q)
                .idPicturePath1(fileStorage.save(id1))
                .idPicturePath2(fileStorage.save(id2))
                .build();

        if (inviteCode != null && !inviteCode.isBlank()) {
            familyRepo.findByInviteCode(inviteCode).ifPresent(u::setFamily);
        }
        return userRepo.save(u);
    }
}