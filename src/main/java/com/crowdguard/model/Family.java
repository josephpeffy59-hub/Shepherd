package com.crowdguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "families")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String inviteCode;

    private String name;

    @OneToMany(mappedBy = "family")
    private List<User> members = new ArrayList<>();
}