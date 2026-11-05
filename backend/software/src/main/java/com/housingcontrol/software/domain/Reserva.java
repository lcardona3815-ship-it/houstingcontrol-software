package com.housingcontrol.software.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "reservas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_reserva_zona_fecha",
            columnNames = {"zonas_comunes_id", "fecha_reserva"}
        )
    }
)
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(nullable = false)
    private String estado;

    @Column(name = "zonas_comunes_id", nullable = false)
    private UUID zonasComunesId;

    @Column(name = "cedula_usuarios", nullable = false, length = 50)
    private String cedulaUsuarios;

    public Reserva() {}

    public Reserva(LocalDateTime fechaReserva, String estado, UUID zonasComunesId, String cedulaUsuarios) {
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.zonasComunesId = zonasComunesId;
        this.cedulaUsuarios = cedulaUsuarios;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public UUID getZonasComunesId() { return zonasComunesId; }
    public void setZonasComunesId(UUID zonasComunesId) { this.zonasComunesId = zonasComunesId; }

    public String getCedulaUsuarios() { return cedulaUsuarios; }
    public void setCedulaUsuarios(String cedulaUsuarios) { this.cedulaUsuarios = cedulaUsuarios; }
}