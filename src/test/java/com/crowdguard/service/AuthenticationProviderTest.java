package com.crowdguard.service;

import com.crowdguard.config.SecurityConfig;
import com.crowdguard.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationProviderTest {
    @Test void correctPasswordAuthenticatesButIncorrectPasswordDoesNot() {
        var details = mock(CustomUserDetailsService.class);
        var user = User.builder().email("name@example.com")
                .password(new BCryptPasswordEncoder().encode("secret7")).build();
        when(details.loadUserByUsername("name@example.com")).thenReturn(user);
        var provider = new SecurityConfig(details).authProvider();
        assertTrue(provider.authenticate(new UsernamePasswordAuthenticationToken("name@example.com", "secret7")).isAuthenticated());
        assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(new UsernamePasswordAuthenticationToken("name@example.com", "wrong-password")));
    }
}
