package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.domain.Visitante;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.housingcontrol.software.infrastructure.repository.VisitanteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitanteServiceTest {

    @Mock
    private VisitanteRepository visitanteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VisitanteService visitanteService;

    @Test
    void registraVisitanteAsociadoAlResidente() {
        RegistrarVisitanteDTO dto = new RegistrarVisitanteDTO(
                "Carlos", "987", "CC", LocalDateTime.of(2026, 10, 5, 15, 0), "123");
        when(usuarioRepository.existsById("123")).thenReturn(true);
        when(visitanteRepository.save(any(Visitante.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Visitante resultado = visitanteService.registrar(dto);

        assertEquals("Carlos", resultado.getNombre());
        assertEquals("987", resultado.getCedula());
        assertEquals("123", resultado.getCedulaUsuarios());
        assertEquals(LocalDateTime.of(2026, 10, 5, 15, 0), resultado.getFechaPrevista());
        assertNull(resultado.getFechaIngreso());
    }

    @Test
    void rechazaSiElResidenteNoExiste() {
        RegistrarVisitanteDTO dto = new RegistrarVisitanteDTO(
                "Carlos", "987", "CC", null, "999");
        when(usuarioRepository.existsById("999")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrar(dto));
        verify(visitanteRepository, never()).save(any());
    }

    @Test
    void rechazaSiFaltaElNombre() {
        RegistrarVisitanteDTO dto = new RegistrarVisitanteDTO(
                "", "987", "CC", null, "123");

        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrar(dto));
        verify(visitanteRepository, never()).save(any());
    }
}