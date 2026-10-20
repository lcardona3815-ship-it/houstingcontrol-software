package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.application.service.VisitanteService;
import com.housingcontrol.software.domain.Visitante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitanteControllerTest {

    @Mock
    private VisitanteService visitanteService;

    @InjectMocks
    private VisitanteController visitanteController;

    @Test
    void registroValidoResponde201() {
        RegistrarVisitanteDTO dto = new RegistrarVisitanteDTO(
                "Carlos", "987", "CC", null, "123");
        when(visitanteService.registrar(dto)).thenReturn(new Visitante());

        ResponseEntity<?> respuesta = visitanteController.registrar(dto);

        assertEquals(201, respuesta.getStatusCode().value());
    }

    @Test
    void registroInvalidoResponde400() {
        RegistrarVisitanteDTO dto = new RegistrarVisitanteDTO(
                "Carlos", "987", "CC", null, "999");
        when(visitanteService.registrar(dto))
                .thenThrow(new IllegalArgumentException("El residente no existe"));

        ResponseEntity<?> respuesta = visitanteController.registrar(dto);

        assertEquals(400, respuesta.getStatusCode().value());
    }
}