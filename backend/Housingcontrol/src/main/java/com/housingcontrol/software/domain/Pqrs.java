package com.housingcontrol.software.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "pqrs")
public class Pqrs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String asunto;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String estado;

    @Column
    private String respuesta;

    @Column(name = "cedula_usuarios", nullable = false)
    private String cedulaUsuarios;

    public Pqrs() {}

    // Getters y Setters
    public UUID getId() { return id; }
    
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    
    public String getCedulaUsuarios() { return cedulaUsuarios; }
    public void setCedulaUsuarios(String cedulaUsuarios) { this.cedulaUsuarios = cedulaUsuarios; }
}