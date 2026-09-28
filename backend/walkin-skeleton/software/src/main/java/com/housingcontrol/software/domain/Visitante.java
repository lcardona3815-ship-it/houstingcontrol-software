package com.housingcontrol.software.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visitantes")
public class Visitante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String cedula;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @Column(name = "fecha_prevista")
    private LocalDateTime fechaPrevista;

    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;

    @Column(name = "cedula_usuarios")
    private String cedulaUsuarios;

    public Visitante() {}

    public UUID getId() { return id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public LocalDateTime getFechaPrevista() { return fechaPrevista; }
    public void setFechaPrevista(LocalDateTime fechaPrevista) { this.fechaPrevista = fechaPrevista; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public String getCedulaUsuarios() { return cedulaUsuarios; }
    public void setCedulaUsuarios(String cedulaUsuarios) { this.cedulaUsuarios = cedulaUsuarios; }
}