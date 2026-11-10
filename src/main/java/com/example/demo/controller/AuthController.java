package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegistroRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos de registro e inicio de sesión de usuarios (emisión de tokens JWT)")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Recibe email y contraseña, persiste el nuevo usuario con contraseña hasheada (BCrypt) y rol USER por defecto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (e.g. email mal formado o contraseña corta)"),
            @ApiResponse(responseCode = "409", description = "El email ya se encuentra registrado")
    })
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Iniciar sesión y obtener token JWT",
            description = "Valida las credenciales contra la base de datos y devuelve un token JWT firmado que contiene el ID y el rol del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, devuelve token JWT"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }
}
