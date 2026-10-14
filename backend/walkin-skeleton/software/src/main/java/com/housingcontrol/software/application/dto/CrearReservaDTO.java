package com.housingcontrol.software.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrearReservaDTO(
    UUID zonas_comunes_id,
    LocalDateTime fecha_reserva,
    String cedula_usuarios
) {}
