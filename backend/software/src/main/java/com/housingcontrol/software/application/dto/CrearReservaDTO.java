package com.housingcontrol.software.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrearReservaDTO(
    UUID zonasComunesId,
    LocalDateTime fechaReserva
) {}