package com.housingcontrol.software.application.service;

import com.housingcontrol.software.application.dto.LoginRequestDTO;
import com.housingcontrol.software.application.dto.LoginResponseDTO;
import com.housingcontrol.software.domain.Rol;
import com.housingcontrol.software.domain.Usuario;
import com.housingcontrol.software.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder);
    }

    private Usuario crearUsuario() {
        Rol rol = new Rol();
        rol.setNombre("RESIDENTE");
        Usuario usuario = new Usuario();
        usuario.setCedula("123");
        usuario.setNombre("Ana");
        usuario.setTipoDocumento("CC");
        usuario.setCredenciales(passwordEncoder.encode("clave123"));
        usuario.setRol(rol);
        return usuario;
    }

    @Test
    void loginConCredencialesValidasDevuelveElRol() {
        when(usuarioRepository.findById("123")).thenReturn(Optional.of(crearUsuario()));

        Optional<LoginResponseDTO> resultado = authService.login(new LoginRequestDTO("123", "clave123"));

        assertTrue(resultado.isPresent());
        assertEquals("RESIDENTE", resultado.get().rol());
    }

    @Test
    void loginConContrasenaIncorrectaNoDejaEntrar() {
        when(usuarioRepository.findById("123")).thenReturn(Optional.of(crearUsuario()));

        Optional<LoginResponseDTO> resultado = authService.login(new LoginRequestDTO("123", "incorrecta"));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void loginConCedulaInexistenteNoDejaEntrar() {
        when(usuarioRepository.findById("999")).thenReturn(Optional.empty());

        Optional<LoginResponseDTO> resultado = authService.login(new LoginRequestDTO("999", "clave123"));

        assertTrue(resultado.isEmpty());
    }
}