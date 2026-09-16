package com.housingcontrol.software.infrastructure.repository;

import com.housingcontrol.software.domain.Pqrs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PqrsRepository extends JpaRepository<Pqrs, UUID> {
}