package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.RegistrarCorrespondenciaDTO;
import com.housingcontrol.software.application.service.CorrespondenciaService;
import com.housingcontrol.software.domain.Correspondencia;
import java.util.Map;
import java.util.UUID;
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

    /** RECIBIDO -> NOTIFICADO. 404 si el paquete no existe, 409 si la transición no es válida. */
    @PatchMapping("/{id}/notificar")
    public ResponseEntity<?> notificar(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(correspondenciaService.notificar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** NOTIFICADO -> ENTREGADO. 404 si el paquete no existe, 409 si la transición no es válida. */
    @PatchMapping("/{id}/entregar")
    public ResponseEntity<?> entregar(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(correspondenciaService.entregar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
