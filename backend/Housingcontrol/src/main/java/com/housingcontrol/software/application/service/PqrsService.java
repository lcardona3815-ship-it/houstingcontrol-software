package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.CrearPqrsDTO;
import com.housingcontrol.software.domain.Pqrs;
import com.housingcontrol.software.infrastructure.repository.PqrsRepository;
import org.springframework.stereotype.Service;

@Service
public class PqrsService {

    private final PqrsRepository pqrsRepository;

    public PqrsService(PqrsRepository pqrsRepository) {
        this.pqrsRepository = pqrsRepository;
    }

    public Pqrs radicarPqrs(CrearPqrsDTO dto) {
        Pqrs nuevaPqrs = new Pqrs();
        nuevaPqrs.setAsunto(dto.asunto());
        nuevaPqrs.setDescripcion(dto.descripcion());
        nuevaPqrs.setCedulaUsuarios(dto.cedula_usuarios());
        
        // Regla de negocio: Inicialmente queda en estado PENDIENTE[cite: 13]
        nuevaPqrs.setEstado("PENDIENTE");

        return pqrsRepository.save(nuevaPqrs);
    }
}