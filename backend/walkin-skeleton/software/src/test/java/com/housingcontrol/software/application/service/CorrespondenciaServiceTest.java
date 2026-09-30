package com.housingcontrol.software.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.housingcontrol.software.application.dto.RegistrarCorrespondenciaDTO;
import com.housingcontrol.software.domain.Correspondencia;
import com.housingcontrol.software.domain.Usuario;
import com.housingcontrol.software.infrastructure.repository.CorrespondenciaRepository;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CorrespondenciaServiceTest {

    @Mock
    private CorrespondenciaRepository correspondenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CorrespondenciaService correspondenciaService;

    private Usuario residente(String cedula, String nombre) {
        Usuario u = new Usuario();
        u.setCedula(cedula);
        u.setNombre(nombre);
        return u;
    }

    @Test
    void registraPaqueteConEstadoRecibidoYFechaDeRecepcion() {
        RegistrarCorrespondenciaDTO dto = new RegistrarCorrespondenciaDTO("Caja mediana", "123");
        when(usuarioRepository.findById("123")).thenReturn(Optional.of(residente("123", "Ana Gómez")));
        when(correspondenciaRepository.save(any(Correspondencia.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Correspondencia resultado = correspondenciaService.registrar(dto);

        assertEquals("RECIBIDO", resultado.getEstado());
        assertEquals("Ana Gómez", resultado.getNombreDestinatario());
        assertEquals("123", resultado.getCedulaUsuarios());
        assertEquals("Caja mediana", resultado.getDescripcion());
        assertNotNull(resultado.getFechaRecepcion());
    }

    @Test
    void rechazaSiElDestinatarioNoExiste() {
        RegistrarCorrespondenciaDTO dto = new RegistrarCorrespondenciaDTO("Sobre", "999");
        when(usuarioRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> correspondenciaService.registrar(dto));
        verify(correspondenciaRepository, never()).save(any());
    }

    @Test
    void rechazaSiFaltaLaDescripcion() {
        RegistrarCorrespondenciaDTO dto = new RegistrarCorrespondenciaDTO("", "123");

        assertThrows(IllegalArgumentException.class, () -> correspondenciaService.registrar(dto));
        verify(correspondenciaRepository, never()).save(any());
    }
}
