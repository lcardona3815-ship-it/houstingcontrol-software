package com.housingcontrol.software.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.housingcontrol.software.domain.Visitante;
import com.housingcontrol.software.infrastructure.repository.VisitanteRepository;

@SpringBootTest
public class VisitanteControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private VisitanteController visitanteController;

    @Autowired
    private VisitanteRepository visitanteRepository;

    @BeforeEach
    void configurarEntorno() {
        visitanteRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.standaloneSetup(visitanteController).build();
    }

    @Test
    void debeBuscarVisitantePorCedulaYMostrarResidenteAsociado() throws Exception {
        // 1. Preparar en H2 usando los setters de Visitante
        Visitante visitante = new Visitante();
        visitante.setCedula("1020304050");
        visitante.setNombre("Laura Gómez");
        visitante.setTipoDocumento("CC");
        visitante.setFechaPrevista(LocalDateTime.now().plusHours(2));
        visitante.setCedulaUsuarios("1010000004");
        visitanteRepository.save(visitante);

        // 2. Ejecutar GET y 3. Verificar respuesta JSON
        mockMvc.perform(get("/api/visitantes/buscar/1020304050")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cedula").value("1020304050"))
                .andExpect(jsonPath("$[0].nombre").value("Laura Gómez"))
                .andExpect(jsonPath("$[0].cedulaUsuarios").value("1010000004"));
    }

    @Test
    void debeRegistrarIngresoYGuardarFechaHoraEnH2() throws Exception {
        // 1. Preparar visitante sin fecha de ingreso
        Visitante visitante = new Visitante();
        visitante.setCedula("1020304050");
        visitante.setNombre("Laura Gómez");
        visitante.setTipoDocumento("CC");
        visitante.setFechaPrevista(LocalDateTime.now());
        visitante.setCedulaUsuarios("1010000004");
        Visitante guardado = visitanteRepository.save(visitante);

        // 2. Ejecutar PUT de ingreso y 3. Verificar que fechaIngreso ya no sea nula
        mockMvc.perform(put("/api/visitantes/" + guardado.getId() + "/ingreso")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId().toString()))
                .andExpect(jsonPath("$.fechaIngreso").isNotEmpty());
    }
}