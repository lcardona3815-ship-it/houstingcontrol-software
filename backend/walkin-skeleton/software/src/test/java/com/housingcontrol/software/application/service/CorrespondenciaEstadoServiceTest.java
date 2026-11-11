package com.housingcontrol.software.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.housingcontrol.software.domain.Correspondencia;
import com.housingcontrol.software.domain.EstadoPaquete;
import com.housingcontrol.software.infrastructure.repository.CorrespondenciaRepository;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CorrespondenciaEstadoServiceTest {

    @Mock
    private CorrespondenciaRepository correspondenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CorrespondenciaService service;

    private Correspondencia paqueteEn(EstadoPaquete estado) {
        Correspondencia c = new Correspondencia();
        c.setEstado(estado.name());
        return c;
    }

    @Test
    void notificaPaqueteRecibidoYPasaANotificado() {
        UUID id = UUID.randomUUID();
        when(correspondenciaRepository.findById(id)).thenReturn(Optional.of(paqueteEn(EstadoPaquete.RECIBIDO)));
        when(correspondenciaRepository.save(any(Correspondencia.class))).thenAnswer(i -> i.getArgument(0));

        Correspondencia resultado = service.notificar(id);

        assertEquals("NOTIFICADO", resultado.getEstado());
    }

    @Test
    void entregaPaqueteNotificadoYPasaAEntregado() {
        UUID id = UUID.randomUUID();
        when(correspondenciaRepository.findById(id)).thenReturn(Optional.of(paqueteEn(EstadoPaquete.NOTIFICADO)));
        when(correspondenciaRepository.save(any(Correspondencia.class))).thenAnswer(i -> i.getArgument(0));

        Correspondencia resultado = service.entregar(id);

        assertEquals("ENTREGADO", resultado.getEstado());
    }

    @Test
    void rechazaEntregarSiElPaqueteNoFueNotificado() {
        UUID id = UUID.randomUUID();
        when(correspondenciaRepository.findById(id)).thenReturn(Optional.of(paqueteEn(EstadoPaquete.RECIBIDO)));

        assertThrows(IllegalStateException.class, () -> service.entregar(id));
        verify(correspondenciaRepository, never()).save(any(Correspondencia.class));
    }

    @Test
    void rechazaNotificarSiElPaqueteYaFueNotificado() {
        UUID id = UUID.randomUUID();
        when(correspondenciaRepository.findById(id)).thenReturn(Optional.of(paqueteEn(EstadoPaquete.NOTIFICADO)));

        assertThrows(IllegalStateException.class, () -> service.notificar(id));
        verify(correspondenciaRepository, never()).save(any(Correspondencia.class));
    }

    @Test
    void rechazaCambiarEstadoSiElPaqueteNoExiste() {
        UUID id = UUID.randomUUID();
        when(correspondenciaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.notificar(id));
        verify(correspondenciaRepository, never()).save(any(Correspondencia.class));
    }
}
