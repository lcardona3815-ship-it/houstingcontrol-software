package com.housingcontrol.software.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.housingcontrol.software.application.dto.CrearReservaDTO;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void debeEvitarReservasDuplicadasParaLaMismaZonaYFecha() throws Exception {
        UUID zonaId = UUID.fromString("7bf2a00e-c9cc-4780-bfe7-416fdbd8a5e4"); // Reemplaza por uno válido
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 31, 18, 0);
        
        CrearReservaDTO reserva1 = new CrearReservaDTO(zonaId, fecha, "1010000004");
        CrearReservaDTO reserva2 = new CrearReservaDTO(zonaId, fecha, "1010000005");

        // 1. La primera petición debe ser exitosa (201 Created)
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva1)))
                .andExpect(status().isCreated());

        // 2. La segunda petición simultánea debe ser rechazada (409 Conflict)
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva2)))
                .andExpect(status().isConflict());
    }
}