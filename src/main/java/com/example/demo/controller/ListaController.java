package com.example.demo.controller;

import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.service.ListaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Operaciones relacionadas con las listas de favoritos")
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @Operation(summary = "Crear una nueva lista vacía")
    @PostMapping
    public ResponseEntity<Lista> crearLista(@RequestBody Lista lista) {
        Lista nuevaLista = listaService.crear(lista);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaLista);
    }

    @Operation(summary = "Obtener todas las listas existentes")
    @GetMapping
    public ResponseEntity<List<Lista>> listarListas() {
        return ResponseEntity.ok(listaService.buscarTodas());
    }

    @Operation(summary = "Obtener una lista específica por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<Lista> obtenerUnaLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.buscarPorId(id));
    }

    @Operation(summary = "Obtener todos los favoritos dentro de una lista específica")
    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<Favorito>> favoritosDeUnaLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.buscarFavoritosDeLista(id));
    }

    @Operation(summary = "Eliminar una lista (falla si contiene favoritos)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLista(@PathVariable Long id) {
        listaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mover todos los favoritos de una lista a otra y eliminar la original")
    @PostMapping("/{origenId}/mover-favoritos")
    public ResponseEntity<Void> moverFavoritos(
            @PathVariable Long origenId, 
            @RequestBody com.example.demo.dto.MoverFavoritosRequest request) {
        listaService.moverFavoritos(origenId, request.listaDestinoId());
        return ResponseEntity.ok().build();
    }
}