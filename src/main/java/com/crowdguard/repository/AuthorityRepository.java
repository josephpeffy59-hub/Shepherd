package com.crowdguard.repository;

import com.crowdguard.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
    Optional<Authority> findByEmail(String email);
    Optional<Authority> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<Authority> findByQuarterId(UUID quarterId);
}
