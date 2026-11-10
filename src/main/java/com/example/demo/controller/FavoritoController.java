package com.example.demo.controller;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "CRUD de productos favoritos. Requiere autenticación (JWT Bearer Token).")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    @Operation(summary = "Crear un favorito", description = "Requiere autenticación (Bearer Token).")
    public ResponseEntity<FavoritoResponseDTO> crearFavorito(@Valid @RequestBody FavoritoRequestDTO request) {
        FavoritoResponseDTO response = favoritoService.guardarFavorito(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los favoritos", description = "Requiere autenticación (Bearer Token).")
    public ResponseEntity<List<FavoritoResponseDTO>> listarFavoritos() {
        return ResponseEntity.ok(favoritoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un favorito por ID", description = "Requiere autenticación (Bearer Token).")
    public ResponseEntity<FavoritoResponseDTO> obtenerFavorito(@PathVariable Long id) {
        return ResponseEntity.ok(favoritoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un favorito existente", description = "Requiere autenticación (Bearer Token).")
    public ResponseEntity<FavoritoResponseDTO> actualizarFavorito(
            @PathVariable Long id, 
            @Valid @RequestBody FavoritoRequestDTO request) {
        FavoritoResponseDTO response = favoritoService.actualizarFavorito(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un favorito", description = "Requiere autenticación (Bearer Token).")
    public ResponseEntity<Void> eliminarFavorito(@PathVariable Long id) {
        favoritoService.eliminarFavorito(id);
        return ResponseEntity.noContent().build(); 
    }
}