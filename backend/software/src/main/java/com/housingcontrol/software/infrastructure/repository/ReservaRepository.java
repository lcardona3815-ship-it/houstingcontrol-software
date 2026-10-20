package com.housingcontrol.software.infrastructure.repository;

import com.housingcontrol.software.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    // Primera barrera de contención contra duplicados
    boolean existsByZonasComunesIdAndFechaReserva(UUID zonasComunesId, LocalDateTime fechaReserva);
}