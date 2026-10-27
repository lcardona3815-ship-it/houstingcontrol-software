package com.housingcontrol.software.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.housingcontrol.software.domain.Pqrs;

@Repository
public interface PqrsRepository extends JpaRepository<Pqrs, UUID> {
    // HU-08: Permite al administrador filtrar las PQRS por estado
    List<Pqrs> findByEstado(String estado);

    // HU-08: Permite al residente consultar el estado de sus solicitudes
    List<Pqrs> findByCedulaUsuarios(String cedulaUsuarios);
}