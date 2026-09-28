package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.application.service.VisitanteService;
import com.housingcontrol.software.domain.Visitante;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
}