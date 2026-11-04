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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.HtmlUtils;

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
        
        // Simulamos que Spring Security interceptó un token JWT válido para el residente "1010000004"
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("1010000004", null, java.util.List.of())
        );
    }

    @Test
    void debeRegistrarPqrsEnBaseDeDatosYRetornar201() throws Exception {
        // HU-07: Registro inicial en estado PENDIENTE.
        String asuntoOriginal = "Falla en citófono";
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
                .andExpect(jsonPath("$.asunto").value(HtmlUtils.htmlEscape(asuntoOriginal)))
                .andExpect(jsonPath("$.cedulaUsuarios").value("1010000004"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void debeFiltrarPqrsPorEstadoEnH2() throws Exception {
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
        Pqrs inicial = new Pqrs("Humedad en techo", "Filtración en último piso", "1010000004");
        inicial.setEstado("PENDIENTE");
        Pqrs guardada = pqrsRepository.save(inicial);

        String respuestaOriginal = "Se programó impermeabilización para el jueves a las 9:00 AM";
        
        String jsonActualizacion = String.format("""
            {
                "estado": "EN_PROCESO",
                "respuesta": "%s"
            }
            """, respuestaOriginal);

        mockMvc.perform(put("/api/pqrs/" + guardada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonActualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                // Validación de seguridad XSS en la respuesta JSON
                .andExpect(jsonPath("$.respuesta").value(HtmlUtils.htmlEscape(respuestaOriginal)));
    }

    @Test
    void debePermitirAlResidenteConsultarElEstadoDeSuSolicitud() throws Exception {
        Pqrs pqrs = new Pqrs("Revisión de cámara", "Cámara del lobby desenfocada", "1010000004");
        pqrs.setEstado("RESUELTA");
        
        String respuestaOriginal = "Lente calibrado por soporte técnico";
        pqrs.setRespuesta(HtmlUtils.htmlEscape(respuestaOriginal));
        pqrsRepository.save(pqrs);

        // Cambiamos a la URL segura que no recibe cédula por parámetro
        mockMvc.perform(get("/api/pqrs/mis-solicitudes")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cedulaUsuarios").value("1010000004"))
                .andExpect(jsonPath("$[0].estado").value("RESUELTA"))
                .andExpect(jsonPath("$[0].respuesta").value(HtmlUtils.htmlEscape(respuestaOriginal)));
    }
}