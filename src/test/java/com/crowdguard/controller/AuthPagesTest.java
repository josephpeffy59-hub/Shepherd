package com.crowdguard.controller;

import com.crowdguard.config.SecurityConfig;
import com.crowdguard.model.Quarter;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, properties = "crowdguard.upload-dir=./uploads")
@Import(SecurityConfig.class)
class AuthPagesTest {
    @Autowired private MockMvc mvc;
    @MockBean private UserService users;
    @MockBean private AuthorityService authorities;
    @MockBean private QuarterRepository quarters;
    @MockBean private CustomUserDetailsService details;

    @Test void registrationPagesRenderPasswordRules() throws Exception {
        when(quarters.findAll()).thenReturn(List.of());
        mvc.perform(get("/register")).andExpect(status().isOk())
                .andExpect(content().string(containsString("minlength=\"7\"")));
        mvc.perform(get("/authority-register")).andExpect(status().isOk())
                .andExpect(content().string(containsString("minlength=\"7\"")));
    }

    @Test void failedCitizenRegistrationRendersErrorsAndKeepsNonSecretDetails() throws Exception {
        var id = UUID.randomUUID();
        when(quarters.findAll()).thenReturn(List.of(Quarter.builder().id(id).name("Test quarter").municipality("Test municipality").build()));
        when(users.register(anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenThrow(new RegistrationValidationException(Map.of("email", "Enter a valid email address.")));
        mvc.perform(multipart("/register")
                .file(new MockMultipartFile("idPicture1", "id1.png", "image/png", new byte[]{1}))
                .file(new MockMultipartFile("idPicture2", "id2.png", "image/png", new byte[]{1}))
                .param("fullName", "Citizen").param("email", "-1").param("password", "123456")
                .param("quarterId", id.toString()).param("inviteCode", "CODE"))
                .andExpect(status().isOk()).andExpect(view().name("register"))
                .andExpect(content().string(containsString("Enter a valid email address.")))
                .andExpect(content().string(containsString("value=\"Citizen\"")))
                .andExpect(content().string(not(containsString("value=\"123456\""))));
    }

    @Test void failedAuthorityRegistrationRendersErrors() throws Exception {
        when(quarters.findAll()).thenReturn(List.of());
        when(authorities.register(anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenThrow(new RegistrationValidationException(Map.of("password", "Use at least 7 characters.")));
        mvc.perform(post("/authority-register").param("fullName", "Officer").param("email", "name@example.com")
                .param("password", "123456").param("badgeNumber", "B123").param("type", "POLICE")
                .param("quarterId", UUID.randomUUID().toString()))
                .andExpect(status().isOk()).andExpect(view().name("authority-register"))
                .andExpect(content().string(containsString("Use at least 7 characters.")))
                .andExpect(content().string(containsString("value=\"B123\"")));
    }
}
