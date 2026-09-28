package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.LoginRequestDTO;
import com.housingcontrol.software.application.dto.LoginResponseDTO;
import com.housingcontrol.software.application.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginValidoResponde200() {
        LoginRequestDTO request = new LoginRequestDTO("123", "clave123");
        when(authService.login(request))
                .thenReturn(Optional.of(new LoginResponseDTO("123", "Ana", "RESIDENTE")));

        ResponseEntity<?> respuesta = authController.login(request);

        assertEquals(200, respuesta.getStatusCode().value());
    }

    @Test
    void loginInvalidoResponde401() {
        LoginRequestDTO request = new LoginRequestDTO("123", "incorrecta");
        when(authService.login(request)).thenReturn(Optional.empty());

        ResponseEntity<?> respuesta = authController.login(request);

        assertEquals(401, respuesta.getStatusCode().value());
    }
}