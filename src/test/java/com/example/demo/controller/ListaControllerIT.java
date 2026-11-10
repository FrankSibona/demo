package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.infra.FavoritoJpaRepository;
import com.example.demo.infra.ListaEntity;
import com.example.demo.infra.ListaJpaRepository;
import com.example.demo.infra.UsuarioEntity;
import com.example.demo.infra.UsuarioJpaRepository;
import com.example.demo.model.Usuario;
import com.example.demo.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ListaControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private ListaJpaRepository listaJpaRepository;

    @Autowired
    private FavoritoJpaRepository favoritoJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private UsuarioEntity usuario1;
    private UsuarioEntity usuario2;
    private UsuarioEntity usuarioAdmin;

    private ListaEntity listaUsuario1;
    private ListaEntity listaUsuario2;

    private String tokenUsuario1;
    private String tokenUsuario2;
    private String tokenAdmin;

    @BeforeEach
    void setUp() {
        favoritoJpaRepository.deleteAll();
        listaJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        // 1. Crear usuarios de prueba
        usuario1 = new UsuarioEntity();
        usuario1.setEmail("usuario1@ejemplo.com");
        usuario1.setPasswordHash(passwordEncoder.encode("password123"));
        usuario1.setRol("USER");
        usuario1 = usuarioJpaRepository.save(usuario1);

        usuario2 = new UsuarioEntity();
        usuario2.setEmail("usuario2@ejemplo.com");
        usuario2.setPasswordHash(passwordEncoder.encode("password123"));
        usuario2.setRol("USER");
        usuario2 = usuarioJpaRepository.save(usuario2);

        usuarioAdmin = new UsuarioEntity();
        usuarioAdmin.setEmail("admin@ejemplo.com");
        usuarioAdmin.setPasswordHash(passwordEncoder.encode("admin123"));
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin = usuarioJpaRepository.save(usuarioAdmin);

        // 2. Crear listas asociadas a cada usuario
        listaUsuario1 = new ListaEntity();
        listaUsuario1.setNombre("Favoritos de Usuario 1");
        listaUsuario1.setUsuario(usuario1);
        listaUsuario1 = listaJpaRepository.save(listaUsuario1);

        listaUsuario2 = new ListaEntity();
        listaUsuario2.setNombre("Favoritos de Usuario 2");
        listaUsuario2.setUsuario(usuario2);
        listaUsuario2 = listaJpaRepository.save(listaUsuario2);

        // 3. Generar tokens JWT
        tokenUsuario1 = jwtService.generarToken(new Usuario(usuario1.getId(), usuario1.getEmail(), usuario1.getPasswordHash(), usuario1.getRol()));
        tokenUsuario2 = jwtService.generarToken(new Usuario(usuario2.getId(), usuario2.getEmail(), usuario2.getPasswordHash(), usuario2.getRol()));
        tokenAdmin = jwtService.generarToken(new Usuario(usuarioAdmin.getId(), usuarioAdmin.getEmail(), usuarioAdmin.getPasswordHash(), usuarioAdmin.getRol()));
    }

    @Test
    void login_conCredencialesValidas_retorna200ConToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario1@ejemplo.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void login_conCredencialesInvalidas_retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario1@ejemplo.com", "claveIncorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaPropia_retorna200() throws Exception {
        mockMvc.perform(get("/api/listas/" + listaUsuario1.getId())
                        .header("Authorization", "Bearer " + tokenUsuario1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaUsuario1.getId()))
                .andExpect(jsonPath("$.nombre").value("Favoritos de Usuario 1"));
    }

    @Test
    void obtener_listaAjena_retorna403() throws Exception {
        // Usuario 1 intenta acceder a la lista del Usuario 2
        mockMvc.perform(get("/api/listas/" + listaUsuario2.getId())
                        .header("Authorization", "Bearer " + tokenUsuario1))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtener_listasSinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaAjenaComoAdmin_retorna200() throws Exception {
        // Administrador accede a la lista del Usuario 1
        mockMvc.perform(get("/api/listas/" + listaUsuario1.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaUsuario1.getId()))
                .andExpect(jsonPath("$.nombre").value("Favoritos de Usuario 1"));
    }
}
