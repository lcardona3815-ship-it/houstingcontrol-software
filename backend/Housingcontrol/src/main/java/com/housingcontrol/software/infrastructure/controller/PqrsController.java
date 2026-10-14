package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.application.service.PqrsService;
import com.housingcontrol.software.domain.Pqrs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pqrs")
public class PqrsController {

    private final PqrsService pqrsService;

    public PqrsController(PqrsService pqrsService) {
        this.pqrsService = pqrsService;
    }

    @PostMapping
    public ResponseEntity<Pqrs> crearPqrs(@RequestBody CrearPqrsDTO dto) {
        Pqrs pqrsGuardada = pqrsService.radicarPqrs(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pqrsGuardada);
    }
}