package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.CrearReservaDTO;
import com.housingcontrol.software.application.service.ReservaService;
import com.housingcontrol.software.domain.Reserva;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // HU-05: Consultar disponibilidad de zona común en fecha y horario
    @GetMapping("/disponibilidad")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam("zonasComunesId") UUID zonasComunesId,
            @RequestParam("fechaReserva") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaReserva) {
        try {
            boolean disponible = reservaService.verificarDisponibilidad(zonasComunesId, fechaReserva);
            return ResponseEntity.ok(Map.of(
                    "zonasComunesId", zonasComunesId,
                    "fechaReserva", fechaReserva.toString(),
                    "disponible", disponible
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-05 y HU-06: Crear reserva y rechazar colisiones con HTTP 409
    @PostMapping
    public ResponseEntity<?> crearReserva(@RequestBody CrearReservaDTO dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String cedulaAutenticada = auth.getName(); 
            Reserva creada = reservaService.crearReserva(dto, cedulaAutenticada);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Colisión detectada en BD"));
        }
    }
}