package com.housingcontrol.software.application.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.housingcontrol.software.application.dto.ActualizarPqrsDTO;
import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.domain.Pqrs;
import com.housingcontrol.software.infrastructure.repository.PqrsRepository;

@Service
public class PqrsService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "EN_PROCESO", "RESUELTA");

    private final PqrsRepository pqrsRepository;

    public PqrsService(PqrsRepository pqrsRepository) {
        this.pqrsRepository = pqrsRepository;
    }

    // HU-07: Registrar PQRS con estado inicial PENDIENTE
    public Pqrs crearPqrs(CrearPqrsDTO dto) {
        if (esVacio(dto.asunto()) || esVacio(dto.descripcion()) || esVacio(dto.cedulaUsuarios())) {
            throw new IllegalArgumentException("El asunto, la descripción y la cédula del residente son obligatorios");
        }

        Pqrs nuevaPqrs = new Pqrs(dto.asunto(), dto.descripcion(), dto.cedulaUsuarios());
        nuevaPqrs.setEstado("PENDIENTE");
        return pqrsRepository.save(nuevaPqrs);
    }

    // HU-08: Visualizar todas las PQRS o filtrar por estado (Administrador)
    public List<Pqrs> listarPqrs(String estado) {
        if (esVacio(estado)) {
            return pqrsRepository.findAll();
        }
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("El estado a filtrar no es válido");
        }
        return pqrsRepository.findByEstado(estado);
    }

    // HU-08: Consultar el estado de las solicitudes de un residente
    public List<Pqrs> consultarPorResidente(String cedulaUsuarios) {
        if (esVacio(cedulaUsuarios)) {
            throw new IllegalArgumentException("La cédula del residente es obligatoria para consultar");
        }
        return pqrsRepository.findByCedulaUsuarios(cedulaUsuarios);
    }

    // HU-08: Cambiar el estado y registrar una respuesta (Administrador)
    public Pqrs gestionarPqrs(UUID id, ActualizarPqrsDTO dto) {
        if (esVacio(dto.estado()) || !ESTADOS_VALIDOS.contains(dto.estado())) {
            throw new IllegalArgumentException("El estado debe ser PENDIENTE, EN_PROCESO o RESUELTA");
        }

        Pqrs pqrs = pqrsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("La PQRS solicitada no existe"));

        pqrs.setEstado(dto.estado());
        if (!esVacio(dto.respuesta())) {
            pqrs.setRespuesta(dto.respuesta());
        }

        return pqrsRepository.save(pqrs);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}