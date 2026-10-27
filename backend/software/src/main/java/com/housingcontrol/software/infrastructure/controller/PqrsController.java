package com.housingcontrol.software.infrastructure.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> listarPqrs(@RequestParam(name = "estado", required = false) String estado) {
        try {
            List<Pqrs> lista = pqrsService.listarPqrs(estado);
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-08: Residente consulta el estado de sus solicitudes
    @GetMapping("/residente/{cedula}")
    public ResponseEntity<?> consultarPorResidente(@PathVariable("cedula") String cedula) {
        try {
            List<Pqrs> lista = pqrsService.consultarPorResidente(cedula);
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // HU-08: Administrador cambia el estado y registra una respuesta
    @PutMapping("/{id}")
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