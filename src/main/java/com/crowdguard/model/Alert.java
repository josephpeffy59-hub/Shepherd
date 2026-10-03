package com.crowdguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double latitude;
    private Double longitude;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.ACTIVE;
}