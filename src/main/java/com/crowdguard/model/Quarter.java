package com.crowdguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "quarters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quarter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String municipality;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private SafetyRating safetyRating = SafetyRating.MODERATE;

    @Column(length = 1000)
    private String description;
}