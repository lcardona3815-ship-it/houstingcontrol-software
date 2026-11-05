package com.housingcontrol.software.application.dto;

import java.time.LocalDateTime;

public record RegistrarVisitanteDTO(
    String nombre,
    String cedula,
    String tipoDocumento,
    LocalDateTime fechaPrevista,
    String cedulaUsuarios
) {}