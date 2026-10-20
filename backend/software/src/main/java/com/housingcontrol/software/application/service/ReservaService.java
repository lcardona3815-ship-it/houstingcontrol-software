package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.CrearReservaDTO;
import com.housingcontrol.software.domain.Reserva;
import com.housingcontrol.software.infrastructure.repository.ReservaRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public Reserva crearReserva(CrearReservaDTO dto) {
        // BARRERA DE SOFTWARE: Verificación rápida de disponibilidad
        if (reservaRepository.existsByZonasComunesIdAndFechaReserva(dto.zonas_comunes_id(), dto.fecha_reserva())) {
            throw new IllegalStateException("La zona ya se encuentra reservada para esa fecha y hora.");
        }

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setZonasComunesId(dto.zonas_comunes_id());
        nuevaReserva.setFechaReserva(dto.fecha_reserva());
        nuevaReserva.setCedulaUsuarios(dto.cedula_usuarios());
        nuevaReserva.setEstado("CONFIRMADA");

        // Al guardar, se activa la BARRERA DE BASE DE DATOS (El constraint UNIQUE del SQL)
        return reservaRepository.save(nuevaReserva);
    }
}