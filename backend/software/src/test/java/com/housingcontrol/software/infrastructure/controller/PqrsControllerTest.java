package com.housingcontrol.software.infrastructure.controller;

import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.domain.Pqrs;
import com.housingcontrol.software.infrastructure.repository.PqrsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PqrsControllerTest {

    @Mock
    private PqrsRepository pqrsRepository;

    @InjectMocks
    private PqrsController pqrsController;

    @Test
    void crearPqrsQuedaEnEstadoPendiente() {
        CrearPqrsDTO dto = new CrearPqrsDTO("Ruido", "Hay ruido en el piso 3", "123");
        when(pqrsRepository.save(any(Pqrs.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ResponseEntity<Pqrs> respuesta = pqrsController.crearPqrs(dto);

        assertEquals(201, respuesta.getStatusCode().value());
        assertEquals("PENDIENTE", respuesta.getBody().getEstado());
        assertEquals("123", respuesta.getBody().getCedulaUsuarios());
    }
}