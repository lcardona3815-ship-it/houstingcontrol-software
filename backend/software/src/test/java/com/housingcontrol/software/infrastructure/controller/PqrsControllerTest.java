package com.housingcontrol.software.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.housingcontrol.software.domain.Pqrs;
import com.housingcontrol.software.infrastructure.repository.PqrsRepository;

@SpringBootTest
public class PqrsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private PqrsController pqrsController;

    @Autowired
    private PqrsRepository pqrsRepository;

    @BeforeEach
    void configurarEntorno() {
        pqrsRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.standaloneSetup(pqrsController).build();
    }

    @Test
    void debeRegistrarPqrsEnBaseDeDatosYRetornar201() throws Exception {
        // HU-07: Registro inicial en estado PENDIENTE
        String jsonValido = """
            {
                "asunto": "Falla en citófono",
                "descripcion": "El citófono del apartamento 302 no da tono con portería",
                "cedulaUsuarios": "1010000004"
            }
            """;

        mockMvc.perform(post("/api/pqrs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.asunto").value("Falla en citófono"))
                .andExpect(jsonPath("$.cedulaUsuarios").value("1010000004"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void debeFiltrarPqrsPorEstadoEnH2() throws Exception {
        // HU-08: Administrador filtra PQRS por estado
        Pqrs pendiente = new Pqrs("Bombillo fundido", "Pasillo torre 1 sin luz", "1010000004");
        pendiente.setEstado("PENDIENTE");
        pqrsRepository.save(pendiente);

        Pqrs resuelta = new Pqrs("Puerta peatonal", "Ajuste de brazo hidráulico", "1010000005");
        resuelta.setEstado("RESUELTA");
        pqrsRepository.save(resuelta);

        mockMvc.perform(get("/api/pqrs")
                .param("estado", "RESUELTA")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].asunto").value("Puerta peatonal"))
                .andExpect(jsonPath("$[0].estado").value("RESUELTA"));
    }

    @Test
    void debeActualizarEstadoYRegistrarRespuestaEnH2() throws Exception {
        // HU-08: Administrador actualiza estado y registra respuesta
        Pqrs inicial = new Pqrs("Humedad en techo", "Filtración en último piso", "1010000004");
        inicial.setEstado("PENDIENTE");
        Pqrs guardada = pqrsRepository.save(inicial);

        String jsonActualizacion = """
            {
                "estado": "EN_PROCESO",
                "respuesta": "Se programó impermeabilización para el jueves a las 9:00 AM"
            }
            """;

        mockMvc.perform(put("/api/pqrs/" + guardada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonActualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.respuesta").value("Se programó impermeabilización para el jueves a las 9:00 AM"));
    }

    @Test
    void debePermitirAlResidenteConsultarElEstadoDeSuSolicitud() throws Exception {
        // HU-08: Residente consulta sus PQRS y ve el estado actualizado
        Pqrs pqrs = new Pqrs("Revisión de cámara", "Cámara del lobby desenfocada", "1010000004");
        pqrs.setEstado("RESUELTA");
        pqrs.setRespuesta("Lente calibrado por soporte técnico");
        pqrsRepository.save(pqrs);

        mockMvc.perform(get("/api/pqrs/residente/1010000004")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cedulaUsuarios").value("1010000004"))
                .andExpect(jsonPath("$[0].estado").value("RESUELTA"))
                .andExpect(jsonPath("$[0].respuesta").value("Lente calibrado por soporte técnico"));
    }
}