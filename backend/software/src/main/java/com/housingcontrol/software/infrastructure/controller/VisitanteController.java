package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.application.service.VisitanteService;
import com.housingcontrol.software.domain.Visitante;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody RegistrarVisitanteDTO dto) {
        try {
            Visitante visitante = visitanteService.registrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(visitante);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-03: Consultar visitante por documento en portería
    @GetMapping("/buscar/{cedula}")
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

    // HU-03: Registrar fecha y hora de ingreso
    @PutMapping("/{id}/ingreso")
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