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
        // Usamos una fecha futura para pasar la nueva barrera de tiempo (Ej: Año 2027)
        LocalDateTime fechaFutura = LocalDateTime.of(2027, 11, 15, 14, 0);

        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fechaFutura)).thenReturn(false);

        // 2. Ejecutar
        boolean disponible = reservaService.verificarDisponibilidad(zonaId, fechaFutura);

        // 3. Verificar
        assertTrue(disponible, "La zona debe figurar como disponible si no existen reservas en ese horario");
        verify(reservaRepository, times(1)).existsByZonasComunesIdAndFechaReserva(zonaId, fechaFutura);
    }

    @Test
    void debeCrearReservaConfirmadaSiLaZonaEstaDisponibleYFechaEsFutura() {
        // 1. Preparar (HU-05: Crear reserva segura)
        UUID zonaId = UUID.randomUUID();
        LocalDateTime fechaFutura = LocalDateTime.of(2027, 11, 15, 14, 0);
        
        // El DTO ya no recibe la cédula (Mitigación BOLA)
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fechaFutura);

        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fechaFutura)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        // 2. Ejecutar (Pasamos la cédula directamente como segundo parámetro)
        Reserva resultado = reservaService.crearReserva(dto, "1010000004");

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
        LocalDateTime fechaFutura = LocalDateTime.of(2027, 11, 15, 14, 0);
        
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fechaFutura);

        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fechaFutura)).thenReturn(true);

        // 2. Ejecutar y 3. Verificar
        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(dto, "1010000004");
        });

        verify(reservaRepository, never()).save(any(Reserva.class));
    }
    
    @Test
    void debeLanzarExcepcionSiIntentaReservarEnUnaFechaPasada() {
        // 1. Preparar (Mitigación de la auditoría: Viajes en el tiempo)
        UUID zonaId = UUID.randomUUID();
        // Configuramos una fecha claramente en el pasado
        LocalDateTime fechaPasada = LocalDateTime.of(1990, 1, 1, 10, 0);
        
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fechaPasada);

        // 2. Ejecutar y 3. Verificar que lance IllegalArgumentException antes de llegar a la BD
        assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(dto, "1010000004");
        });

        verify(reservaRepository, never()).existsByZonasComunesIdAndFechaReserva(any(), any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}