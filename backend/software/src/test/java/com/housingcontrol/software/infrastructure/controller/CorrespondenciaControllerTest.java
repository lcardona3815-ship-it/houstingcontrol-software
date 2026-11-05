package com.housingcontrol.software.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.housingcontrol.software.application.dto.RegistrarCorrespondenciaDTO;
import com.housingcontrol.software.application.service.CorrespondenciaService;
import com.housingcontrol.software.domain.Correspondencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CorrespondenciaControllerTest {

    @Mock
    private CorrespondenciaService correspondenciaService;

    @InjectMocks
    private CorrespondenciaController correspondenciaController;

    @Test
    void registroValidoResponde201() {
        RegistrarCorrespondenciaDTO dto = new RegistrarCorrespondenciaDTO("Caja", "123");
        when(correspondenciaService.registrar(dto)).thenReturn(new Correspondencia());

        ResponseEntity<?> respuesta = correspondenciaController.registrar(dto);

        assertEquals(201, respuesta.getStatusCode().value());
    }

    @Test
    void registroInvalidoResponde400() {
        RegistrarCorrespondenciaDTO dto = new RegistrarCorrespondenciaDTO("Caja", "999");
        when(correspondenciaService.registrar(dto))
                .thenThrow(new IllegalArgumentException("El destinatario no existe"));

        ResponseEntity<?> respuesta = correspondenciaController.registrar(dto);

        assertEquals(400, respuesta.getStatusCode().value());
    }
}
