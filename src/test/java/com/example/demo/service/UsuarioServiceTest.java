package com.example.demo.service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegistroRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.exception.UsuarioYaExisteException;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void registrar_usuarioNuevo_guardaConPasswordHasheadaYRolUser() {
        RegistroRequestDTO request = new RegistroRequestDTO("test@ejemplo.com", "miPassword123");

        when(usuarioRepository.findByEmail("test@ejemplo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("miPassword123")).thenReturn("$2a$10$hashSeguroMock");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            return new Usuario(1L, u.getEmail(), u.getPasswordHash(), u.getRol());
        });

        UsuarioResponseDTO resultado = usuarioService.registrar(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("test@ejemplo.com", resultado.email());
        assertEquals("USER", resultado.rol());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioGuardado = captor.getValue();
        assertEquals("test@ejemplo.com", usuarioGuardado.getEmail());
        assertEquals("$2a$10$hashSeguroMock", usuarioGuardado.getPasswordHash());
        assertNotEquals("miPassword123", usuarioGuardado.getPasswordHash());
        assertEquals("USER", usuarioGuardado.getRol());
    }

    @Test
    void registrar_emailExistente_lanzaUsuarioYaExisteException() {
        RegistroRequestDTO request = new RegistroRequestDTO("duplicado@ejemplo.com", "miPassword123");
        Usuario usuarioExistente = new Usuario(1L, "duplicado@ejemplo.com", "hash", "USER");

        when(usuarioRepository.findByEmail("duplicado@ejemplo.com")).thenReturn(Optional.of(usuarioExistente));

        assertThrows(UsuarioYaExisteException.class, () -> usuarioService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_credencialesValidas_retornaTokenJwt() {
        LoginRequestDTO request = new LoginRequestDTO("usuario@ejemplo.com", "miPasswordCorrecto");
        Usuario usuario = new Usuario(10L, "usuario@ejemplo.com", "$2a$10$hashGuardado", "USER");

        when(usuarioRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("miPasswordCorrecto", "$2a$10$hashGuardado")).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("mock.jwt.token");

        LoginResponseDTO response = usuarioService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.token());
        verify(jwtService).generarToken(usuario);
    }

    @Test
    void login_emailInexistente_lanzaCredencialesInvalidasException() {
        LoginRequestDTO request = new LoginRequestDTO("inexistente@ejemplo.com", "miPassword");

        when(usuarioRepository.findByEmail("inexistente@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.login(request));
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesInvalidasException() {
        LoginRequestDTO request = new LoginRequestDTO("usuario@ejemplo.com", "passwordErroneo");
        Usuario usuario = new Usuario(10L, "usuario@ejemplo.com", "$2a$10$hashGuardado", "USER");

        when(usuarioRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("passwordErroneo", "$2a$10$hashGuardado")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.login(request));
        verify(jwtService, never()).generarToken(any());
    }
}
