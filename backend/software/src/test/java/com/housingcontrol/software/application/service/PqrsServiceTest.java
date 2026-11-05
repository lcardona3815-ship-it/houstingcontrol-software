package com.housingcontrol.software.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.HtmlUtils;

import com.housingcontrol.software.application.dto.ActualizarPqrsDTO;
import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.domain.Pqrs;
import com.housingcontrol.software.infrastructure.repository.PqrsRepository;

@ExtendWith(MockitoExtension.class)
public class PqrsServiceTest {

    @Mock
    private PqrsRepository pqrsRepository;

    @InjectMocks
    private PqrsService pqrsService;

    @Test
    void debeCrearPqrsConEstadoPendienteYCedulaAsociada() {
        // HU-07: Crear PQRS
        CrearPqrsDTO dto = new CrearPqrsDTO(
                "Ruido en zona comun", 
                "Musica a alto volumen", 
                "1010000004"
        );

        when(pqrsRepository.save(any(Pqrs.class))).thenAnswer(invocation -> {
            Pqrs guardada = invocation.getArgument(0);
            guardada.setId(UUID.randomUUID());
            return guardada;
        });

        Pqrs resultado = pqrsService.crearPqrs(dto);

        assertNotNull(resultado.getId());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("1010000004", resultado.getCedulaUsuarios());
        verify(pqrsRepository, times(1)).save(any(Pqrs.class));
    }

    @Test
    void debeFiltrarPqrsPorEstadoParaElAdministrador() {
        // HU-08: Filtrar por estado
        Pqrs pqrsPendiente = new Pqrs("Daño en ascensor", "No cierra puerta piso 2", "1010000004");
        pqrsPendiente.setEstado("PENDIENTE");

        when(pqrsRepository.findByEstado("PENDIENTE")).thenReturn(List.of(pqrsPendiente));

        List<Pqrs> resultado = pqrsService.listarPqrs("PENDIENTE");

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
        verify(pqrsRepository, times(1)).findByEstado("PENDIENTE");
    }

    @Test
    void debeActualizarEstadoYRegistrarRespuestaDelAdministrador() {
        // HU-08: Cambiar estado y registrar respuesta
        UUID pqrsId = UUID.randomUUID();
        Pqrs pqrsExistente = new Pqrs("Fuga de agua", "Goteo en parqueadero", "1010000004");
        pqrsExistente.setId(pqrsId);
        pqrsExistente.setEstado("PENDIENTE");

        String respuestaOriginal = "El equipo de mantenimiento reparó la tubería el día de hoy";
        ActualizarPqrsDTO dto = new ActualizarPqrsDTO("RESUELTA", respuestaOriginal);

        when(pqrsRepository.findById(pqrsId)).thenReturn(Optional.of(pqrsExistente));
        when(pqrsRepository.save(any(Pqrs.class))).thenAnswer(inv -> inv.getArgument(0));

        Pqrs actualizada = pqrsService.gestionarPqrs(pqrsId, dto);

        assertEquals("RESUELTA", actualizada.getEstado());
        
        // Validación de Seguridad: Comprobamos que el servicio escapó correctamente los caracteres (XSS)
        assertEquals(HtmlUtils.htmlEscape(respuestaOriginal), actualizada.getRespuesta());
        
        verify(pqrsRepository, times(1)).save(pqrsExistente);
    }

    @Test
    void debeConsultarPqrsPorResidenteParaVerEstadoYSeguimiento() {
        // HU-08: El residente puede consultar el estado de su solicitud
        Pqrs pqrsResidente = new Pqrs("Fuga de agua", "Goteo en parqueadero", "1010000004");
        pqrsResidente.setEstado("EN_PROCESO");
        pqrsResidente.setRespuesta("Tecnico asignado para revision");

        when(pqrsRepository.findByCedulaUsuarios("1010000004")).thenReturn(List.of(pqrsResidente));

        List<Pqrs> resultado = pqrsService.consultarPorResidente("1010000004");

        assertFalse(resultado.isEmpty());
        assertEquals("EN_PROCESO", resultado.get(0).getEstado());
        assertEquals("Tecnico asignado para revision", resultado.get(0).getRespuesta());
        verify(pqrsRepository, times(1)).findByCedulaUsuarios("1010000004");
    }
}