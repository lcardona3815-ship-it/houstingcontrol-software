package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.domain.Visitante;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.housingcontrol.software.infrastructure.repository.VisitanteRepository;
import org.springframework.stereotype.Service;

@Service
public class VisitanteService {

    private final VisitanteRepository visitanteRepository;
    private final UsuarioRepository usuarioRepository;

    public VisitanteService(VisitanteRepository visitanteRepository,
                            UsuarioRepository usuarioRepository) {
        this.visitanteRepository = visitanteRepository;
        this.usuarioRepository = usuarioRepository;
    }

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

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}