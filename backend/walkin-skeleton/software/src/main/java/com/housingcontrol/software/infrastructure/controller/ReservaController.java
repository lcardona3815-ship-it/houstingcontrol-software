package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.CrearReservaDTO;
import com.housingcontrol.software.domain.Reserva;
import com.housingcontrol.software.infrastructure.repository.ReservaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaRepository reservaRepository;

    public ReservaController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearReserva(@RequestBody CrearReservaDTO dto) {
        
        // 1. PRIMERA BARRERA: Validación rápida antes de instanciar objetos
        boolean ocupado = reservaRepository.existsByZonasComunesIdAndFechaReserva(
                dto.zonas_comunes_id(), dto.fecha_reserva()
        );

        if (ocupado) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La zona ya se encuentra reservada para esa fecha y hora. Por favor, seleccione otro horario.");
        }

        // 2. Preparación de la entidad
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setZonasComunesId(dto.zonas_comunes_id());
        nuevaReserva.setFechaReserva(dto.fecha_reserva());
        nuevaReserva.setCedulaUsuarios(dto.cedula_usuarios());
        
        // Asignación de estado inicial según tu enumerador estado_reserva_enum
        nuevaReserva.setEstado("CONFIRMADA");

        try {
            // 3. SEGUNDA BARRERA: Inserción sujeta a la restricción UNIQUE de la base de datos
            Reserva reservaGuardada = reservaRepository.save(nuevaReserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservaGuardada);
            
        } catch (DataIntegrityViolationException e) {
            // Captura de condición de carrera: Otro hilo insertó el registro un milisegundo antes
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Conflicto de concurrencia: El horario fue reservado por otro usuario un instante antes. Operación rechazada.");
        }
    }
}
