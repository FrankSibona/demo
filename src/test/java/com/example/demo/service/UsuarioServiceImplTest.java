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
class UsuarioServiceImplTest {

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
    void registrar_conPasswordEnTextoPlano_nuncaGuardaPasswordEnTextoPlano() {
        String passwordPlana = "superPasswordSegura123!";
        String hashEsperado = "$2a$10$e8O6vV9l8pA8l3yPqS9YQ.vE.N31N7pQzH6XwP2wM4P9r8L1m2N3.";
        RegistroRequestDTO request = new RegistroRequestDTO("juan@ejemplo.com", passwordPlana);

        when(usuarioRepository.findByEmail("juan@ejemplo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(passwordPlana)).thenReturn(hashEsperado);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            return new Usuario(1L, u.getEmail(), u.getPasswordHash(), u.getRol());
        });

        UsuarioResponseDTO resultado = usuarioService.registrar(request);

        assertNotNull(resultado);
        assertEquals("juan@ejemplo.com", resultado.email());
        assertEquals("USER", resultado.rol());

        // Verificamos que al repositorio se le pase el hash y NUNCA la contraseña en texto plano
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioPersistido = captor.getValue();

        assertEquals("juan@ejemplo.com", usuarioPersistido.getEmail());
        assertEquals(hashEsperado, usuarioPersistido.getPasswordHash());
        assertNotEquals(passwordPlana, usuarioPersistido.getPasswordHash(), "La contraseña jamás debe ser guardada en texto plano");
        assertFalse(usuarioPersistido.getPasswordHash().contains(passwordPlana), "El hash no debe contener la contraseña plana");
    }

    @Test
    void registrar_conEmailExistente_lanzaUsuarioYaExiste() {
        RegistroRequestDTO request = new RegistroRequestDTO("existente@ejemplo.com", "password123");
        Usuario usuarioExistente = new Usuario(1L, "existente@ejemplo.com", "hash", "USER");

        when(usuarioRepository.findByEmail("existente@ejemplo.com")).thenReturn(Optional.of(usuarioExistente));

        assertThrows(UsuarioYaExisteException.class, () -> usuarioService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_conCredencialesValidas_retornaTokenJwt() {
        LoginRequestDTO request = new LoginRequestDTO("usuario@ejemplo.com", "claveCorrecta");
        Usuario usuario = new Usuario(5L, "usuario@ejemplo.com", "$2a$10$hashValido", "USER");

        when(usuarioRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveCorrecta", "$2a$10$hashValido")).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("token.jwt.mockeado");

        LoginResponseDTO response = usuarioService.login(request);

        assertNotNull(response);
        assertEquals("token.jwt.mockeado", response.token());
    }

    @Test
    void login_conPasswordIncorrecto_lanzaCredencialesInvalidas() {
        LoginRequestDTO request = new LoginRequestDTO("usuario@ejemplo.com", "claveErronea");
        Usuario usuario = new Usuario(5L, "usuario@ejemplo.com", "$2a$10$hashValido", "USER");

        when(usuarioRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveErronea", "$2a$10$hashValido")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.login(request));
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void login_conEmailInexistente_lanzaCredencialesInvalidas() {
        LoginRequestDTO request = new LoginRequestDTO("inexistente@ejemplo.com", "claveCualquiera");

        when(usuarioRepository.findByEmail("inexistente@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.login(request));
        verify(jwtService, never()).generarToken(any());
    }
}
