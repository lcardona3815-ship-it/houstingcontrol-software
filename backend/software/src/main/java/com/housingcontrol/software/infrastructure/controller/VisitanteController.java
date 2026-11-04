package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.application.service.VisitanteService;
import com.housingcontrol.software.domain.Visitante;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/visitantes")
public class VisitanteController {

    private final VisitanteService visitanteService;

    public VisitanteController(VisitanteService visitanteService) {
        this.visitanteService = visitanteService;
    }

    // HU-02: Registro previo de visitante (Protegido contra BOLA / Suplantación)
    @PostMapping
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<?> registrar(@RequestBody RegistrarVisitanteDTO dto) {
        try {
            // Extraemos la cédula directamente del contexto de seguridad de Spring
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String cedulaAutenticada = auth.getName();

            // Pasamos la cédula segura al servicio
            Visitante visitante = visitanteService.registrar(dto, cedulaAutenticada);
            return ResponseEntity.status(HttpStatus.CREATED).body(visitante);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-03: Consultar visitante por documento en portería (Protegido contra Data Scraping)
    @GetMapping("/buscar/{cedula}")
    @PreAuthorize("hasAnyRole('PORTERIA', 'ADMIN')")
    public ResponseEntity<?> consultarPorCedula(@PathVariable("cedula") String cedula) {
        try {
            List<Visitante> visitantes = visitanteService.consultarPorCedula(cedula);
            if (visitantes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Visitante no encontrado"));
            }
            return ResponseEntity.ok(visitantes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-03: Registrar fecha y hora de ingreso (Protegido contra Escalada de Privilegios - MFLAC)
    @PutMapping("/{id}/ingreso")
    @PreAuthorize("hasAnyRole('PORTERIA', 'ADMIN')") // Solo personal autorizado puede marcar el ingreso
    public ResponseEntity<?> registrarIngreso(@PathVariable("id") UUID id) {
        try {
            Visitante visitante = visitanteService.registrarIngreso(id);
            return ResponseEntity.ok(visitante);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}