package com.crowdguard.service;

import com.crowdguard.model.Alert;
import com.crowdguard.model.AlertStatus;
import com.crowdguard.model.User;
import com.crowdguard.repository.AlertRepository;
import com.crowdguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate ws;

    @Transactional
    public Alert trigger(User user, double lat, double lng) {
        Alert a = Alert.builder()
                .user(user)
                .latitude(lat)
                .longitude(lng)
                .timestamp(LocalDateTime.now())
                .status(AlertStatus.ACTIVE)
                .build();
        alertRepo.save(a);

        user.setHasActiveAlert(true);
        userRepo.save(user);

        Map<String, Object> payload = new HashMap<>();
        payload.put("alertId", a.getId().toString());
        payload.put("userId", user.getId().toString());
        payload.put("userName", user.getFullName());
        payload.put("userEmail", user.getEmail());
        payload.put("quarter", user.getQuarter() != null ? user.getQuarter().getName() : "Unknown");
        payload.put("quarterId", user.getQuarter() != null ? user.getQuarter().getId().toString() : "");
        payload.put("lat", lat);
        payload.put("lng", lng);
        payload.put("idPicture1", user.getIdPicturePath1());
        payload.put("idPicture2", user.getIdPicturePath2());

        // Notify authorities
        ws.convertAndSend("/topic/alerts", payload);

        // Notify family members
        if (user.getFamily() != null) {
            ws.convertAndSend("/topic/family/" + user.getFamily().getId(), payload);
        }

        return a;
    }

    @Transactional
    public void cancel(User user) {
        user.setHasActiveAlert(false);
        userRepo.save(user);

        alertRepo.findByStatusOrderByTimestampDesc(AlertStatus.ACTIVE).stream()
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .forEach(a -> {
                    a.setStatus(AlertStatus.RESOLVED);
                    alertRepo.save(a);
                });

        if (user.getFamily() != null) {
            Map<String, Object> cancelPayload = new HashMap<>();
            cancelPayload.put("cancel", true);
            cancelPayload.put("userId", user.getId().toString());
            ws.convertAndSend("/topic/family/" + user.getFamily().getId(), cancelPayload);
        }
    }
}