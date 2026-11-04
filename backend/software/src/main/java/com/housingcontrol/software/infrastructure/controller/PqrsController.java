package com.housingcontrol.software.infrastructure.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.housingcontrol.software.application.dto.ActualizarPqrsDTO;
import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.application.service.PqrsService;
import com.housingcontrol.software.domain.Pqrs;

@RestController
@RequestMapping("/api/pqrs")
public class PqrsController {

    private final PqrsService pqrsService;

    public PqrsController(PqrsService pqrsService) {
        this.pqrsService = pqrsService;
    }

    // HU-07: Registrar PQRS
    @PostMapping
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<?> registrarPqrs(@RequestBody CrearPqrsDTO dto) {
        try {
            Pqrs creada = pqrsService.crearPqrs(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-08: Visualizar todas las PQRS o filtrar por estado (?estado=PENDIENTE)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el administrador puede ver las PQRS de todos
    public ResponseEntity<?> listarPqrs(@RequestParam(name = "estado", required = false) String estado) {
        try {
            List<Pqrs> lista = pqrsService.listarPqrs(estado);
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-08: Residente consulta el estado de sus solicitudes (Identidad segura desde el Token)
    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('RESIDENTE')") // Validación de rol
    public ResponseEntity<?> consultarMisPqrs() {
        try {
            // Extraemos la cédula directamente del contexto de seguridad de Spring
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String cedulaAutenticada = auth.getName();
            
            List<Pqrs> lista = pqrsService.consultarPorResidente(cedulaAutenticada);
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-08: Administrador cambia el estado y registra una respuesta
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Evita que un residente se auto-resuelva la PQRS
    public ResponseEntity<?> gestionarPqrs(@PathVariable("id") UUID id, @RequestBody ActualizarPqrsDTO dto) {
        try {
            Pqrs actualizada = pqrsService.gestionarPqrs(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }
}