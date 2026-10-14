package com.housingcontrol.software.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.housingcontrol.software.application.dto.CrearPqrsDTO;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Revierte los cambios en la BD después de la prueba para mantenerla limpia
public class PqrsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void debeCrearPqrsExitosamenteConEstadoPendiente() throws Exception {
        // 1. Preparar los datos de prueba
        CrearPqrsDTO dto = new CrearPqrsDTO("Fallo electrico", "Lámpara fundida", "1010000004");
        
        // 2. Ejecutar la petición HTTP simulada 3. Validar los resultados
        mockMvc.perform(post("/api/pqrs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists()) // El sistema genera un identificador[cite: 13]
                .andExpect(jsonPath("$.estado").value("PENDIENTE")) // Inicialmente queda en estado PENDIENTE[cite: 13]
                .andExpect(jsonPath("$.asunto").value("Fallo electrico")) // El residente puede registrar asunto[cite: 13]
                .andExpect(jsonPath("$.cedulaUsuarios").value("1010000004")); // La PQRS queda asociada al residente[cite: 13]
    }
}