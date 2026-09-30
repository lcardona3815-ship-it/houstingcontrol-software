package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.LoginRequestDTO;
import com.housingcontrol.software.application.dto.LoginResponseDTO;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<LoginResponseDTO> login(LoginRequestDTO dto) {
        return usuarioRepository.findById(dto.cedula())
                .filter(u -> passwordEncoder.matches(dto.contrasena(), u.getCredenciales()))
                .map(u -> new LoginResponseDTO(
                        u.getCedula(),
                        u.getNombre(),
                        u.getRol() != null ? u.getRol().getNombre() : null));
    }
}