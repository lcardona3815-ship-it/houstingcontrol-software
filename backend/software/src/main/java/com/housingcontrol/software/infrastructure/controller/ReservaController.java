package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.CrearReservaDTO;
import com.housingcontrol.software.application.service.ReservaService;
import com.housingcontrol.software.domain.Reserva;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<?> generarReserva(@RequestBody CrearReservaDTO dto) {
        try {
            Reserva reservaConfirmada = reservaService.crearReserva(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservaConfirmada);
            
        } catch (IllegalStateException e) {
            // Falla en la primera barrera (Software)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            
        } catch (DataIntegrityViolationException e) {
            // Falla en la segunda barrera (BD - Concurrencia extrema)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Conflicto de concurrencia: La zona fue reservada un milisegundo antes por otra persona.");
        }
    }
}