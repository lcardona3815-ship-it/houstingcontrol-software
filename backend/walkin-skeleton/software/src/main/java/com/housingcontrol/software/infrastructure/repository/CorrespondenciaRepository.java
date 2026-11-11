package com.housingcontrol.software.infrastructure.repository;

import com.housingcontrol.software.domain.Correspondencia;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrespondenciaRepository extends JpaRepository<Correspondencia, UUID> {
}
