package com.housingcontrol.software.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.housingcontrol.software.domain.Usuario;
import com.housingcontrol.software.infrastructure.repository.CorrespondenciaRepository;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Prueba de integración del flujo de estados de HU-04:
 * HTTP -> controlador -> servicio -> JPA -> base H2, sin mocks.
 * Después de cada paso se hace flush() y clear() para que la lectura de verificación
 * salga de la base de datos y no de la memoria de JPA.
 */
@SpringBootTest
@Transactional
class CorrespondenciaEstadoIntegrationTest {

    private static final String CEDULA_RESIDENTE = "9000000002";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CorrespondenciaRepository correspondenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();

        Usuario residente = new Usuario();
        residente.setCedula(CEDULA_RESIDENTE);
        residente.setNombre("Luis Perez");
        residente.setTipoDocumento("CC");
        residente.setCredenciales("clave-de-prueba");
        usuarioRepository.save(residente);
    }

    private UUID registrarPaquete() throws Exception {
        String json = """
                {"descripcion":"Paquete de prueba","cedula_usuarios":"%s"}
                """.formatted(CEDULA_RESIDENTE);
        MvcResult resultado = mvc.perform(post("/api/correspondencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        return UUID.fromString(id);
    }

    /** Lee el estado directo de la base: escribe lo pendiente y vacía la memoria de JPA. */
    private String estadoEnBaseDeDatos(UUID id) {
        entityManager.flush();
        entityManager.clear();
        return correspondenciaRepository.findById(id).orElseThrow().getEstado();
    }

    @Test
    void flujoCompletoRecibidoNotificadoEntregadoQuedaGuardadoEnBaseDeDatos() throws Exception {
        UUID id = registrarPaquete();
        assertEquals("RECIBIDO", estadoEnBaseDeDatos(id));

        mvc.perform(patch("/api/correspondencias/{id}/notificar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NOTIFICADO"));
        assertEquals("NOTIFICADO", estadoEnBaseDeDatos(id));

        mvc.perform(patch("/api/correspondencias/{id}/entregar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
        assertEquals("ENTREGADO", estadoEnBaseDeDatos(id));
    }

    @Test
    void entregarSinNotificarResponde409YElEstadoSigueRecibido() throws Exception {
        UUID id = registrarPaquete();

        mvc.perform(patch("/api/correspondencias/{id}/entregar", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").exists());

        assertEquals("RECIBIDO", estadoEnBaseDeDatos(id));
    }

    @Test
    void notificarDosVecesResponde409YElEstadoSigueNotificado() throws Exception {
        UUID id = registrarPaquete();

        mvc.perform(patch("/api/correspondencias/{id}/notificar", id))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/correspondencias/{id}/notificar", id))
                .andExpect(status().isConflict());

        assertEquals("NOTIFICADO", estadoEnBaseDeDatos(id));
    }

    @Test
    void cambiarEstadoDePaqueteInexistenteResponde404() throws Exception {
        UUID inexistente = UUID.randomUUID();

        mvc.perform(patch("/api/correspondencias/{id}/notificar", inexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El paquete no existe"));
    }
}
