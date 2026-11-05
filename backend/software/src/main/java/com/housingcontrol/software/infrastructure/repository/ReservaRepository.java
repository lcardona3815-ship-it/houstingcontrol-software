package com.housingcontrol.software.infrastructure.repository;

import com.housingcontrol.software.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    // Verifica disponibilidad (HU-05) y actúa como primera barrera lógica (HU-06)
    boolean existsByZonasComunesIdAndFechaReserva(UUID zonasComunesId, LocalDateTime fechaReserva);
}