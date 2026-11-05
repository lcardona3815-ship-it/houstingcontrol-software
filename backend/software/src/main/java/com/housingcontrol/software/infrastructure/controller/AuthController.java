package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.LoginRequestDTO;
import com.housingcontrol.software.application.dto.LoginResponseDTO;
import com.housingcontrol.software.application.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        Optional<LoginResponseDTO> resultado = authService.login(dto);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", "Cédula o contraseña incorrectos"));
    }
}