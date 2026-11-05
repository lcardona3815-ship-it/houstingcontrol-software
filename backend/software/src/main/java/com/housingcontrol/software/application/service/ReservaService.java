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
    public Reserva crearReserva(CrearReservaDTO dto, String cedulaAutenticada) {
        
        // 1. Validación de nulidad
        if (dto.zonasComunesId() == null || dto.fechaReserva() == null) {
            throw new IllegalArgumentException("La zona común y la fecha son obligatorias");
        }

        // 2. Validación de tiempo (Mitigación Caso 2 - Auditoría)
        if (dto.fechaReserva().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden crear reservas en el pasado");
        }

        // 3. Barrera lógica (Mitigación colisiones HU-06)
        if (reservaRepository.existsByZonasComunesIdAndFechaReserva(dto.zonasComunesId(), dto.fechaReserva())) {
            throw new IllegalStateException("La zona común ya se encuentra reservada para esa fecha y horario");
        }

        // 4. Persistencia de la reserva segura
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setZonasComunesId(dto.zonasComunesId());
        nuevaReserva.setFechaReserva(dto.fechaReserva());
        nuevaReserva.setCedulaUsuarios(cedulaAutenticada); // Asignación segura desde el token
        nuevaReserva.setEstado("CONFIRMADA");

        return reservaRepository.save(nuevaReserva);
    }
}