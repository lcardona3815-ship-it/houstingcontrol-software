package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.RegistrarVisitanteDTO;
import com.housingcontrol.software.domain.Visitante;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import com.housingcontrol.software.infrastructure.repository.VisitanteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class VisitanteService {

    // Sanitización para el Caso 3: Evitar inyección de tipos de documento inválidos
    private static final Set<String> TIPOS_VALIDOS = Set.of("CC", "CE", "NIT", "PASAPORTE");

    private final VisitanteRepository visitanteRepository;
    private final UsuarioRepository usuarioRepository;

    public VisitanteService(VisitanteRepository visitanteRepository,
                            UsuarioRepository usuarioRepository) {
        this.visitanteRepository = visitanteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // HU-02: Registro previo de visitante por el residente (Seguro contra BOLA)
    public Visitante registrar(RegistrarVisitanteDTO dto, String cedulaAutenticada) {
        // Ya no validamos dto.cedulaUsuarios(), confiamos 100% en la cédula del Token
        if (esVacio(dto.nombre()) || esVacio(dto.cedula()) || esVacio(dto.tipoDocumento())) {
            throw new IllegalArgumentException(
                    "Nombre, documento y tipo de documento son obligatorios");
        }

        // Validación estricta del Enum (Mitigación Caso 3)
        if (!TIPOS_VALIDOS.contains(dto.tipoDocumento().toUpperCase())) {
            throw new IllegalArgumentException("El tipo de documento debe ser CC, CE, NIT o PASAPORTE");
        }

        // Verificamos que el residente autenticado realmente exista en la BD
        if (!usuarioRepository.existsById(cedulaAutenticada)) {
            throw new IllegalArgumentException("El residente autenticado no existe en el sistema");
        }

        Visitante visitante = new Visitante();
        visitante.setNombre(dto.nombre());
        visitante.setCedula(dto.cedula());
        visitante.setTipoDocumento(dto.tipoDocumento().toUpperCase()); // Normalizamos a mayúsculas
        visitante.setFechaPrevista(dto.fechaPrevista());
        
        // Asignación segura de identidad (Mitigación Caso 1 - BOLA)
        visitante.setCedulaUsuarios(cedulaAutenticada);

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