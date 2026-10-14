package com.housingcontrol.software.infrastructure.repository;

import com.housingcontrol.software.domain.Pqrs;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PqrsRepository extends JpaRepository<Pqrs, UUID> {
}