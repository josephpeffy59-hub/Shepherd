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
        return userRepo.findByEmail(email)
                .map(u -> (UserDetails) u)
                .orElseGet(() -> authRepo.findByEmail(email)
                        .map(a -> (UserDetails) a)
                        .orElseThrow(() -> new UsernameNotFoundException("No account: " + email)));
    }
}