package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.domain.Visitante;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.housingcontrol.software.infrastructure.repository.VisitanteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VisitanteService {

    private final VisitanteRepository visitanteRepository;
    private final UsuarioRepository usuarioRepository;

    public VisitanteService(VisitanteRepository visitanteRepository,
                            UsuarioRepository usuarioRepository) {
        this.visitanteRepository = visitanteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // HU-02: Registro previo de visitante por el residente
    public Visitante registrar(RegistrarVisitanteDTO dto) {
        if (esVacio(dto.nombre()) || esVacio(dto.cedula())
                || esVacio(dto.tipoDocumento()) || esVacio(dto.cedulaUsuarios())) {
            throw new IllegalArgumentException(
                    "Nombre, documento, tipo de documento y residente son obligatorios");
        }
        if (!usuarioRepository.existsById(dto.cedulaUsuarios())) {
            throw new IllegalArgumentException("El residente no existe");
        }

        Visitante visitante = new Visitante();
        visitante.setNombre(dto.nombre());
        visitante.setCedula(dto.cedula());
        visitante.setTipoDocumento(dto.tipoDocumento());
        visitante.setFechaPrevista(dto.fechaPrevista());
        visitante.setCedulaUsuarios(dto.cedulaUsuarios());

        return visitanteRepository.save(visitante);
    }

    // HU-03: Consultar visitantes registrados en portería
    public List<Visitante> consultarPorCedula(String cedula) {
        if (esVacio(cedula)) {
            throw new IllegalArgumentException("El documento del visitante es obligatorio para la consulta");
        }
        return visitanteRepository.findByCedula(cedula);
    }

    // HU-03: Registrar el ingreso almacenando fecha y hora
    public Visitante registrarIngreso(UUID id) {
        Visitante visitante = visitanteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El visitante no se encuentra registrado"));

        visitante.setFechaIngreso(LocalDateTime.now());
        return visitanteRepository.save(visitante);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}