package com.crowdguard.service;

import com.crowdguard.model.User;
import com.crowdguard.repository.AuthorityRepository;
import com.crowdguard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    @Test void invalidEmailIsRejectedBeforeAccountLookup() {
        var users = mock(UserRepository.class);
        var authorities = mock(AuthorityRepository.class);
        var service = new CustomUserDetailsService(users, authorities);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("-1"));
        verifyNoInteractions(users, authorities);
    }

    @Test void loginUsesCaseInsensitiveNormalizedEmail() {
        var users = mock(UserRepository.class);
        var authorities = mock(AuthorityRepository.class);
        var user = User.builder().email("name@example.com").build();
        when(users.findByEmailIgnoreCase("name@example.com")).thenReturn(Optional.of(user));
        var service = new CustomUserDetailsService(users, authorities);
        assertSame(user, service.loadUserByUsername(" NAME@EXAMPLE.COM "));
        verifyNoInteractions(authorities);
    }
}
