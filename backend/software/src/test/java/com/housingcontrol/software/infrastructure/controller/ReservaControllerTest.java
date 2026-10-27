package com.housingcontrol.software.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.housingcontrol.software.infrastructure.repository.ReservaRepository;

@SpringBootTest
public class ReservaControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @BeforeEach
    void configurarEntorno() {
        reservaRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.standaloneSetup(reservaController).build();
    }

    @Test
    void debeConsultarDisponibilidadDeZonaComunYRetornarDisponibleTrue() throws Exception {
        // HU-05: Consultar disponibilidad antes de reservar
        UUID zonaId = UUID.randomUUID();
        String fecha = "2026-11-20T15:00:00";

        mockMvc.perform(get("/api/reservas/disponibilidad")
                .param("zonasComunesId", zonaId.toString())
                .param("fechaReserva", fecha)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void debeCrearReservaCuandoZonaEstaDisponibleYRetornar201() throws Exception {
        // HU-05: Crear reserva en fecha y horario disponible -> HTTP 201 Created
        UUID zonaId = UUID.randomUUID();
        String jsonReserva = String.format("""
            {
                "zonasComunesId": "%s",
                "fechaReserva": "2026-11-20T15:00:00",
                "cedulaUsuarios": "1010000004"
            }
            """, zonaId);

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReserva))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.cedulaUsuarios").value("1010000004"));
    }

    @Test
    void debeRechazarReservaDuplicadaMismoHorarioYRetornar409Conflict() throws Exception {
        // HU-05 y HU-06: Dos solicitudes para la misma zona y mismo horario
        UUID zonaId = UUID.randomUUID();

        String primeraSolicitud = String.format("""
            {
                "zonasComunesId": "%s",
                "fechaReserva": "2026-11-20T18:00:00",
                "cedulaUsuarios": "1010000004"
            }
            """, zonaId);

        String segundaSolicitudDuplicada = String.format("""
            {
                "zonasComunesId": "%s",
                "fechaReserva": "2026-11-20T18:00:00",
                "cedulaUsuarios": "1010000005"
            }
            """, zonaId);

        // 1. La primera solicitud se confirma con HTTP 201 Created
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(primeraSolicitud))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        // 2. La segunda solicitud para el mismo horario es rechazada con HTTP 409 Conflict
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(segundaSolicitudDuplicada))
                .andExpect(status().isConflict());
    }
}