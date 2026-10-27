package com.housingcontrol.software.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.housingcontrol.software.application.dto.CrearReservaDTO;
import com.housingcontrol.software.domain.Reserva;
import com.housingcontrol.software.infrastructure.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void debeRetornarTrueCuandoZonaComunEstaDisponible() {
        // 1. Preparar (HU-05: Consultar disponibilidad)
        UUID zonaId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 15, 14, 0);

        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fecha)).thenReturn(false);

        // 2. Ejecutar
        boolean disponible = reservaService.verificarDisponibilidad(zonaId, fecha);

        // 3. Verificar
        assertTrue(disponible, "La zona debe figurar como disponible si no existen reservas en ese horario");
        verify(reservaRepository, times(1)).existsByZonasComunesIdAndFechaReserva(zonaId, fecha);
    }

    @Test
    void debeCrearReservaConfirmadaSiLaZonaEstaDisponible() {
        // 1. Preparar (HU-05: Crear reserva en horario libre)
        UUID zonaId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 15, 14, 0);
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fecha, "1010000004");

        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fecha)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        // 2. Ejecutar
        Reserva resultado = reservaService.crearReserva(dto);

        // 3. Verificar
        assertNotNull(resultado.getId());
        assertEquals("CONFIRMADA", resultado.getEstado());
        assertEquals(zonaId, resultado.getZonasComunesId());
        assertEquals("1010000004", resultado.getCedulaUsuarios());
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void debeLanzarExcepcionSiLaZonaYaEstaReservadaEnEseHorario() {
        // 1. Preparar (HU-05 y HU-06: Rechazar si ya existe reserva en ese horario)
        UUID zonaId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 15, 14, 0);
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fecha, "1010000004");

        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fecha)).thenReturn(true);

        // 2. Ejecutar y 3. Verificar
        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(dto);
        });

        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}