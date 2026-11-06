package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegistroRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.UsuarioYaExisteException;
import com.example.demo.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(usuarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registrar_datosValidos_retorna201Created() throws Exception {
        RegistroRequestDTO request = new RegistroRequestDTO("nuevo@ejemplo.com", "password123");
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "nuevo@ejemplo.com", "USER");

        when(usuarioService.registrar(any(RegistroRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("nuevo@ejemplo.com"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void registrar_emailYaExiste_retorna409Conflict() throws Exception {
        RegistroRequestDTO request = new RegistroRequestDTO("existente@ejemplo.com", "password123");

        when(usuarioService.registrar(any(RegistroRequestDTO.class)))
                .thenThrow(new UsuarioYaExisteException("El email ya se encuentra registrado"));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_credencialesValidas_retorna200OkYToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario@ejemplo.com", "password123");
        LoginResponseDTO response = new LoginResponseDTO("jwt.token.valido");

        when(usuarioService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.valido"));
    }

    @Test
    void login_credencialesInvalidas_retorna401Unauthorized() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario@ejemplo.com", "passwordErroneo");

        when(usuarioService.login(any(LoginRequestDTO.class)))
                .thenThrow(new CredencialesInvalidasException("Email o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
