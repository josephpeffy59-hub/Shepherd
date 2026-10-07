package com.crowdguard.service;

import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;
    private final AuthorityRepository authRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = AccountValidation.normalizeEmail(email);
        if (!AccountValidation.isValidEmail(normalized)) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        return userRepo.findByEmailIgnoreCase(normalized)
                .map(u -> (UserDetails) u)
                .orElseGet(() -> authRepo.findByEmailIgnoreCase(normalized)
                        .map(a -> (UserDetails) a)
                        .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials")));
    }
}
