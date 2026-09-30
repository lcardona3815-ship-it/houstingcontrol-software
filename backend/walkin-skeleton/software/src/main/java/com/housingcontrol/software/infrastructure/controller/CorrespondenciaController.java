package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.RegistrarCorrespondenciaDTO;
import com.housingcontrol.software.application.service.CorrespondenciaService;
import com.housingcontrol.software.domain.Correspondencia;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/correspondencias")
public class CorrespondenciaController {

    private final CorrespondenciaService correspondenciaService;

    public CorrespondenciaController(CorrespondenciaService correspondenciaService) {
        this.correspondenciaService = correspondenciaService;
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody RegistrarCorrespondenciaDTO dto) {
        try {
            Correspondencia correspondencia = correspondenciaService.registrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(correspondencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
