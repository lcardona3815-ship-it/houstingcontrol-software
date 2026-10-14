package com.housingcontrol.software.infrastructure.repository;

import com.housingcontrol.software.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    
    // Barrera 1 (Software): Verificación rápida de disponibilidad
    boolean existsByZonasComunesIdAndFechaReserva(UUID zonasComunesId, LocalDateTime fechaReserva);
}
