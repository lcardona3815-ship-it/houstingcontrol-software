package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.RegistrarCorrespondenciaDTO;
import com.housingcontrol.software.domain.Correspondencia;
import com.housingcontrol.software.domain.EstadoPaquete;
import com.housingcontrol.software.domain.Usuario;
import com.housingcontrol.software.infrastructure.repository.CorrespondenciaRepository;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class CorrespondenciaService {

    private final CorrespondenciaRepository correspondenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public CorrespondenciaService(CorrespondenciaRepository correspondenciaRepository,
                                  UsuarioRepository usuarioRepository) {
        this.correspondenciaRepository = correspondenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Correspondencia registrar(RegistrarCorrespondenciaDTO dto) {
        if (esVacio(dto.descripcion()) || esVacio(dto.cedulaUsuarios())) {
            throw new IllegalArgumentException("Destinatario y descripción son obligatorios");
        }
        Usuario destinatario = usuarioRepository.findById(dto.cedulaUsuarios())
                .orElseThrow(() -> new IllegalArgumentException("El destinatario no existe"));

        Correspondencia correspondencia = new Correspondencia();
        correspondencia.setDescripcion(dto.descripcion());
        correspondencia.setCedulaUsuarios(dto.cedulaUsuarios());
        correspondencia.setNombreDestinatario(destinatario.getNombre());
        correspondencia.setFechaRecepcion(LocalDateTime.now());
        correspondencia.setEstado(EstadoPaquete.RECIBIDO.name());

        return correspondenciaRepository.save(correspondencia);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
