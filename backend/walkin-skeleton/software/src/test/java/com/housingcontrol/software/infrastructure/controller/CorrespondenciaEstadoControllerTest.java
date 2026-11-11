package com.housingcontrol.software.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.housingcontrol.software.application.service.CorrespondenciaService;
import com.housingcontrol.software.domain.Correspondencia;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CorrespondenciaEstadoControllerTest {

    @Mock
    private CorrespondenciaService correspondenciaService;

    @InjectMocks
    private CorrespondenciaController correspondenciaController;

    @Test
    void notificarPaqueteValidoResponde200() {
        UUID id = UUID.randomUUID();
        when(correspondenciaService.notificar(id)).thenReturn(new Correspondencia());

        ResponseEntity<?> respuesta = correspondenciaController.notificar(id);

        assertEquals(200, respuesta.getStatusCode().value());
    }

    @Test
    void notificarPaqueteInexistenteResponde404() {
        UUID id = UUID.randomUUID();
        when(correspondenciaService.notificar(id))
                .thenThrow(new IllegalArgumentException("El paquete no existe"));

        ResponseEntity<?> respuesta = correspondenciaController.notificar(id);

        assertEquals(404, respuesta.getStatusCode().value());
    }

    @Test
    void notificarConTransicionInvalidaResponde409() {
        UUID id = UUID.randomUUID();
        when(correspondenciaService.notificar(id))
                .thenThrow(new IllegalStateException("Transición inválida"));

        ResponseEntity<?> respuesta = correspondenciaController.notificar(id);

        assertEquals(409, respuesta.getStatusCode().value());
    }

    @Test
    void entregarPaqueteValidoResponde200() {
        UUID id = UUID.randomUUID();
        when(correspondenciaService.entregar(id)).thenReturn(new Correspondencia());

        ResponseEntity<?> respuesta = correspondenciaController.entregar(id);

        assertEquals(200, respuesta.getStatusCode().value());
    }

    @Test
    void entregarPaqueteInexistenteResponde404() {
        UUID id = UUID.randomUUID();
        when(correspondenciaService.entregar(id))
                .thenThrow(new IllegalArgumentException("El paquete no existe"));

        ResponseEntity<?> respuesta = correspondenciaController.entregar(id);

        assertEquals(404, respuesta.getStatusCode().value());
    }

    @Test
    void entregarSinHaberNotificadoResponde409() {
        UUID id = UUID.randomUUID();
        when(correspondenciaService.entregar(id))
                .thenThrow(new IllegalStateException("Transición inválida"));

        ResponseEntity<?> respuesta = correspondenciaController.entregar(id);

        assertEquals(409, respuesta.getStatusCode().value());
    }
}
