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

@ExtendWith(MockitoExtension.class) // Habilita Mockito en esta clase
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository; // Simulacro de la BD

    @InjectMocks
    private ReservaService reservaService; // El servicio real que vamos a probar

    @Test
    void debeGuardarReservaSiLaZonaEstaDisponible() {
        UUID zonaId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now();
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fecha, "1010000004");
        Reserva reservaSimulada = new Reserva();

        // 1. Entrenamos al Mock: Cuando pregunten si existe, decimos que NO (false)
        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fecha)).thenReturn(false);
        // Entrenamos al Mock: Cuando intenten guardar, devolvemos un objeto vacío
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaSimulada);

        // 2. Ejecutamos el servicio
        Reserva resultado = reservaService.crearReserva(dto);

        // 3. Verificamos que el código fluyó correctamente y se llamó al método save
        assertNotNull(resultado);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void debeLanzarExcepcionSiLaZonaYaEstaReservada() {
        UUID zonaId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now();
        CrearReservaDTO dto = new CrearReservaDTO(zonaId, fecha, "1010000004");

        // 1. Entrenamos al Mock: Cuando pregunten si existe, decimos que SÍ (true)
        when(reservaRepository.existsByZonasComunesIdAndFechaReserva(zonaId, fecha)).thenReturn(true);

        // 2. Ejecutamos y verificamos que la barrera de software estalle con IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(dto);
        });

        // 3. Verificamos que por seguridad NUNCA se intentó guardar en BD
        verify(reservaRepository, never()).save(any());
    }
}