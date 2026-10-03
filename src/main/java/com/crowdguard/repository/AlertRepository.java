package com.crowdguard.repository;

import com.crowdguard.model.Alert;
import com.crowdguard.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByStatusOrderByTimestampDesc(AlertStatus status);
}