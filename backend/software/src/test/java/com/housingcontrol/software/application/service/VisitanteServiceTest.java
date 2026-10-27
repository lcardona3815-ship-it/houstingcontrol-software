package com.housingcontrol.software.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.domain.Visitante;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.housingcontrol.software.infrastructure.repository.VisitanteRepository;

@ExtendWith(MockitoExtension.class)
public class VisitanteServiceTest {

    @Mock
    private VisitanteRepository visitanteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VisitanteService visitanteService;

    @Test
    void debeRegistrarVisitanteCuandoDatosYResidenteSonValidos() {
        // 1. Preparar (HU-02): Primero va el nombre y segundo la cédula
        RegistrarVisitanteDTO dto = new RegistrarVisitanteDTO(
                "Carlos Pérez",
                "1020304050",
                "CC",
                LocalDateTime.now().plusDays(1),
                "1010000004"
        );

        when(usuarioRepository.existsById("1010000004")).thenReturn(true);
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. Ejecutar
        Visitante resultado = visitanteService.registrar(dto);

        // 3. Verificar
        assertNotNull(resultado);
        assertEquals("Carlos Pérez", resultado.getNombre());
        assertEquals("1020304050", resultado.getCedula());
        assertEquals("1010000004", resultado.getCedulaUsuarios());
        verify(visitanteRepository, times(1)).save(any(Visitante.class));
    }

    @Test
    void debeConsultarVisitanteEnPorteriaYMostrarResidenteAsociado() {
        // 1. Preparar (HU-03)
        Visitante visitante = new Visitante();
        visitante.setCedula("1020304050");
        visitante.setNombre("Carlos Pérez");
        visitante.setTipoDocumento("CC");
        visitante.setCedulaUsuarios("1010000004");

        when(visitanteRepository.findByCedula("1020304050")).thenReturn(List.of(visitante));

        // 2. Ejecutar
        List<Visitante> resultado = visitanteService.consultarPorCedula("1020304050");

        // 3. Verificar: muestra el visitante y el residente asociado
        assertFalse(resultado.isEmpty());
        assertEquals("Carlos Pérez", resultado.get(0).getNombre());
        assertEquals("1010000004", resultado.get(0).getCedulaUsuarios());
        verify(visitanteRepository, times(1)).findByCedula("1020304050");
    }

    @Test
    void debeRegistrarIngresoYAlmacenarFechaYHora() {
        // 1. Preparar (HU-03)
        UUID idVisitante = UUID.randomUUID();
        Visitante visitante = new Visitante();
        visitante.setCedula("1020304050");
        visitante.setNombre("Carlos Pérez");
        visitante.setTipoDocumento("CC");
        visitante.setCedulaUsuarios("1010000004");

        when(visitanteRepository.findById(idVisitante)).thenReturn(Optional.of(visitante));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. Ejecutar
        Visitante resultado = visitanteService.registrarIngreso(idVisitante);

        // 3. Verificar: almacena fecha y hora del ingreso
        assertNotNull(resultado.getFechaIngreso());
        verify(visitanteRepository, times(1)).save(visitante);
    }

    @Test
    void debeLanzarExcepcionSiVisitanteNoExisteAlRegistrarIngreso() {
        UUID idInexistente = UUID.randomUUID();
        when(visitanteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            visitanteService.registrarIngreso(idInexistente);
        });

        verify(visitanteRepository, never()).save(any());
    }
}