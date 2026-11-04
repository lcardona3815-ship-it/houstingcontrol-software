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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        // Simulamos que Spring Security interceptó un token JWT válido para el residente "1010000004"
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("1010000004", null, java.util.List.of())
        );
    }

    @Test
    void debeConsultarDisponibilidadDeZonaComunYRetornarDisponibleTrue() throws Exception {
        UUID zonaId = UUID.randomUUID();
        String fechaFutura = "2027-11-20T15:00:00"; // Aseguramos usar una fecha en el futuro

        mockMvc.perform(get("/api/reservas/disponibilidad")
                .param("zonasComunesId", zonaId.toString())
                .param("fechaReserva", fechaFutura)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void debeCrearReservaCuandoZonaEstaDisponibleYRetornar201() throws Exception {
        UUID zonaId = UUID.randomUUID();
        
        // HU-05: Ya no enviamos la cédula en el JSON (Mitigación BOLA)
        String jsonReserva = String.format("""
            {
                "zonasComunesId": "%s",
                "fechaReserva": "2027-11-20T15:00:00"
            }
            """, zonaId);

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReserva))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                // Verificamos que la cédula se haya tomado del token simulado, no del JSON
                .andExpect(jsonPath("$.cedulaUsuarios").value("1010000004"));
    }

    @Test
    void debeRechazarReservaDuplicadaMismoHorarioYRetornar409Conflict() throws Exception {
        UUID zonaId = UUID.randomUUID();

        String primeraSolicitud = String.format("""
            {
                "zonasComunesId": "%s",
                "fechaReserva": "2027-11-20T18:00:00"
            }
            """, zonaId);

        String segundaSolicitudDuplicada = String.format("""
            {
                "zonasComunesId": "%s",
                "fechaReserva": "2027-11-20T18:00:00"
            }
            """, zonaId);

        // 1. La primera solicitud se confirma correctamente
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(primeraSolicitud))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        // 2. La segunda solicitud para la misma fecha choca contra la barrera de concurrencia
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(segundaSolicitudDuplicada))
                .andExpect(status().isConflict());
    }
}