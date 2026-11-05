package com.housingcontrol.software.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Columna fundamental para el UNIQUE constraint
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(nullable = false)
    private String estado;

    // Columna fundamental para el UNIQUE constraint
    @Column(name = "zonas_comunes_id", nullable = false)
    private UUID zonasComunesId;

    @Column(name = "cedula_usuarios", nullable = false)
    private String cedulaUsuarios;

    // Constructores, Getters y Setters
    public Reserva() {}

    public UUID getId() { return id; }
    
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public UUID getZonasComunesId() { return zonasComunesId; }
    public void setZonasComunesId(UUID zonasComunesId) { this.zonasComunesId = zonasComunesId; }
    
    public String getCedulaUsuarios() { return cedulaUsuarios; }
    public void setCedulaUsuarios(String cedulaUsuarios) { this.cedulaUsuarios = cedulaUsuarios; }
}