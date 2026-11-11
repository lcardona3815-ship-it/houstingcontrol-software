package com.housingcontrol.software.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.housingcontrol.software.domain.Correspondencia;
import com.housingcontrol.software.domain.Usuario;
import com.housingcontrol.software.infrastructure.repository.CorrespondenciaRepository;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba de integración de HU-04: petición HTTP -> controlador -> servicio -> JPA -> base H2.
 * Usa el contexto real de Spring y la configuración de src/test/resources/application.properties.
 * Al ser @Transactional, cada prueba se revierte y no deja datos.
 */
@SpringBootTest
@Transactional
class CorrespondenciaIntegrationTest {

    private static final String CEDULA_RESIDENTE = "9000000001";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CorrespondenciaRepository correspondenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();

        Usuario residente = new Usuario();
        residente.setCedula(CEDULA_RESIDENTE);
        residente.setNombre("Ana Gomez");
        residente.setTipoDocumento("CC");
        residente.setCredenciales("clave-de-prueba");
        usuarioRepository.save(residente);
    }

    private ResultActions registrar(String descripcion, String cedula) throws Exception {
        String json = """
                {"descripcion":"%s","cedula_usuarios":"%s"}
                """.formatted(descripcion, cedula);
        return mvc.perform(post("/api/correspondencias")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json));
    }

    @Test
    void registroValidoResponde201YQuedaGuardadoComoRecibido() throws Exception {
        long antes = correspondenciaRepository.count();

        MvcResult resultado = registrar("Caja mediana de Amazon", CEDULA_RESIDENTE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.nombreDestinatario").value("Ana Gomez"))
                .andReturn();

        // Fuerza el INSERT real en la base antes de verificar
        correspondenciaRepository.flush();

        String id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        Correspondencia guardada = correspondenciaRepository.findById(UUID.fromString(id)).orElseThrow();

        assertEquals(antes + 1, correspondenciaRepository.count());
        assertEquals("RECIBIDO", guardada.getEstado());
        assertEquals("Caja mediana de Amazon", guardada.getDescripcion());
        assertEquals(CEDULA_RESIDENTE, guardada.getCedulaUsuarios());
        assertEquals("Ana Gomez", guardada.getNombreDestinatario());
        assertNotNull(guardada.getFechaRecepcion());
    }

    @Test
    void sinDescripcionResponde400YNoGuardaNada() throws Exception {
        long antes = correspondenciaRepository.count();

        registrar("", CEDULA_RESIDENTE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());

        assertEquals(antes, correspondenciaRepository.count());
    }

    @Test
    void destinatarioInexistenteResponde400YNoGuardaNada() throws Exception {
        long antes = correspondenciaRepository.count();

        registrar("Sobre", "0000000000")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El destinatario no existe"));

        assertEquals(antes, correspondenciaRepository.count());
    }
}
