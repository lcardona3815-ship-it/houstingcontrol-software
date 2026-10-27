package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.CrearReservaDTO;
import com.housingcontrol.software.domain.Reserva;
import com.housingcontrol.software.infrastructure.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    // HU-05: Consultar si una zona común está disponible en una fecha y horario
    public boolean verificarDisponibilidad(UUID zonasComunesId, LocalDateTime fechaReserva) {
        if (zonasComunesId == null || fechaReserva == null) {
            throw new IllegalArgumentException("La zona común y la fecha son obligatorias para consultar disponibilidad");
        }
        return !reservaRepository.existsByZonasComunesIdAndFechaReserva(zonasComunesId, fechaReserva);
    }

    // HU-05 y HU-06: Crear reserva verificando disponibilidad y evitando duplicados
    public Reserva crearReserva(CrearReservaDTO dto) {
        if (dto.zonasComunesId() == null || dto.fechaReserva() == null
                || dto.cedulaUsuarios() == null || dto.cedulaUsuarios().isBlank()) {
            throw new IllegalArgumentException("La zona común, la fecha y el documento del residente son obligatorios");
        }

        // Barrera lógica: verifica disponibilidad antes de insertar
        if (reservaRepository.existsByZonasComunesIdAndFechaReserva(dto.zonasComunesId(), dto.fechaReserva())) {
            throw new IllegalStateException("La zona común ya se encuentra reservada para esa fecha y horario");
        }

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setZonasComunesId(dto.zonasComunesId());
        nuevaReserva.setFechaReserva(dto.fechaReserva());
        nuevaReserva.setCedulaUsuarios(dto.cedulaUsuarios());
        nuevaReserva.setEstado("CONFIRMADA");

        return reservaRepository.save(nuevaReserva);
    }
}