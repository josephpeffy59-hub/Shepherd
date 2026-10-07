package com.crowdguard.controller;

import com.crowdguard.model.AuthorityType;
import com.crowdguard.repository.QuarterRepository;
import com.crowdguard.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test void citizenValidationErrorsReturnFormWithoutPassword() {
        var users = mock(UserService.class);
        var authorities = mock(AuthorityService.class);
        var quarters = mock(QuarterRepository.class);
        var controller = new AuthController(users, authorities, quarters);
        var id = UUID.randomUUID();
        var errors = Map.of("password", "Use at least 7 characters.");
        when(users.register(anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenThrow(new RegistrationValidationException(errors));
        var model = new ExtendedModelMap();
        assertEquals("register", controller.register("Citizen", "name@example.com", "123456", id, null, null, "FAMILY", model));
        assertEquals(errors, model.get("validationErrors"));
        assertEquals("name@example.com", model.get("email"));
        assertEquals(id, model.get("quarterId"));
        assertFalse(model.containsAttribute("password"));
        verify(quarters).findAll();
    }

    @Test void authorityValidationErrorsPreserveServiceAndBadge() {
        var users = mock(UserService.class);
        var authorities = mock(AuthorityService.class);
        var quarters = mock(QuarterRepository.class);
        var controller = new AuthController(users, authorities, quarters);
        var id = UUID.randomUUID();
        when(authorities.register(anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenThrow(new RegistrationValidationException(Map.of("email", "Enter a valid email.")));
        var model = new ExtendedModelMap();
        assertEquals("authority-register", controller.authorityRegister("Officer", "-1", "secret7", "B123", AuthorityType.POLICE, id, model));
        assertEquals("B123", model.get("badgeNumber"));
        assertEquals(AuthorityType.POLICE, model.get("selectedType"));
        assertFalse(model.containsAttribute("password"));
    }
}
