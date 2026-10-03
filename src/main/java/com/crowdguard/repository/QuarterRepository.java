package com.crowdguard.repository;

import com.crowdguard.model.Quarter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuarterRepository extends JpaRepository<Quarter, UUID> {
}