package com.crowdguard.service;

import com.crowdguard.model.*;
import com.crowdguard.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class AlertNotificationsTest {
    @Test void cancellationNotifiesAuthoritiesAndFamilyAndResolvesOnlyUserAlerts() {
        var repository = mock(AlertRepository.class);
        var users = mock(UserRepository.class);
        var ws = mock(SimpMessagingTemplate.class);
        var quarter = Quarter.builder().id(UUID.randomUUID()).name("Test Quarter").build();
        var family = Family.builder().id(UUID.randomUUID()).name("Household").build();
        var user = User.builder().id(UUID.randomUUID()).quarter(quarter).family(family).hasActiveAlert(true).build();
        var alert = Alert.builder().id(UUID.randomUUID()).user(user).status(AlertStatus.ACTIVE).build();
        when(repository.findByUserIdAndStatusOrderByTimestampDesc(user.getId(), AlertStatus.ACTIVE)).thenReturn(List.of(alert));
        new AlertService(repository, users, ws).cancel(user);
        assertEquals(AlertStatus.RESOLVED, alert.getStatus()); assertFalse(user.isHasActiveAlert());
        verify(ws).convertAndSend(eq("/topic/alerts"), (Object) argThat(payload -> payload instanceof Map<?,?> map && Boolean.TRUE.equals(map.get("cancel")) && user.getId().toString().equals(map.get("userId"))));
        verify(ws).convertAndSend(eq("/topic/family/" + family.getId()), any(Object.class));
    }

    @Test void payloadDoesNotIncludePasswordOrFullAccountObject() {
        var user = User.builder().id(UUID.randomUUID()).fullName("Citizen").password("private-hash").build();
        var alert = Alert.builder().id(UUID.randomUUID()).user(user).latitude(3.8).longitude(11.5).timestamp(LocalDateTime.now()).build();
        var payload = AlertService.alertPayload(alert);
        assertEquals("Citizen", payload.get("userName"));
        assertFalse(payload.containsKey("password")); assertFalse(payload.containsKey("user"));
        assertTrue(payload.containsKey("timestamp"));
    }
}
