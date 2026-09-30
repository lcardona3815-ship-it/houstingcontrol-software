package com.housingcontrol.software.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrarCorrespondenciaDTO(
        String descripcion,
        @JsonProperty("cedula_usuarios") String cedulaUsuarios) {
}
