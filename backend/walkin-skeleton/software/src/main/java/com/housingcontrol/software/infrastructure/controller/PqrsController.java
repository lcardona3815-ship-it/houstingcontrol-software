package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.domain.Pqrs;
import com.housingcontrol.software.infrastructure.repository.PqrsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/pqrs")
public class PqrsController {

    private final PqrsRepository pqrsRepository;

    public PqrsController(PqrsRepository pqrsRepository) {
        this.pqrsRepository = pqrsRepository;
    }

    @PostMapping
    public ResponseEntity<Pqrs> crearPqrs(@RequestBody CrearPqrsDTO dto) {
        // 1. Mapeo del DTO a la Entidad
        Pqrs nuevaPqrs = new Pqrs();
        nuevaPqrs.setAsunto(dto.asunto());
        nuevaPqrs.setDescripcion(dto.descripcion());
        nuevaPqrs.setCedulaUsuarios(dto.cedula_usuarios());
        
        // 2. Regla de negocio: Asigna estado inicial "PENDIENTE"
        nuevaPqrs.setEstado("PENDIENTE");

        // Captura la fecha y hora exacta del sistema en este instante
        nuevaPqrs.setFechaHora(LocalDateTime.now());

        // 3. Transacción a Base de Datos
        Pqrs pqrsGuardada = pqrsRepository.save(nuevaPqrs);

        // 4. Retorno HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(pqrsGuardada);
    }
}