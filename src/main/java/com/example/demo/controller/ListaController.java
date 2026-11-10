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
@Tag(name = "Listas", description = "Operaciones sobre listas de favoritos. Requiere autenticación (JWT Bearer Token). Aplica validación de propiedad de recursos (dueño) y rol ADMIN.")
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @Operation(
            summary = "Crear una nueva lista vacía",
            description = "Requiere autenticación. La lista creada queda asociada automáticamente al usuario autenticado (nunca se toma del body)."
    )
    @PostMapping
    public ResponseEntity<Lista> crearLista(@RequestBody Lista lista) {
        Lista nuevaLista = listaService.crear(lista);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaLista);
    }

    @Operation(
            summary = "Obtener todas las listas",
            description = "Requiere autenticación. Devuelve únicamente las listas pertenecientes al usuario autenticado (o todas si el rol es ADMIN)."
    )
    @GetMapping
    public ResponseEntity<List<Lista>> listarListas() {
        return ResponseEntity.ok(listaService.buscarTodas());
    }

    @Operation(
            summary = "Obtener una lista específica por su ID",
            description = "Requiere autenticación. Solo el dueño de la lista o un usuario con rol ADMIN pueden acceder (403 Forbidden si pertenece a otro usuario)."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Lista> obtenerUnaLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.buscarPorId(id));
    }

    @Operation(
            summary = "Obtener todos los favoritos dentro de una lista específica",
            description = "Requiere autenticación. Solo el dueño de la lista o un usuario con rol ADMIN pueden consultar sus favoritos (403 Forbidden si es ajena)."
    )
    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<Favorito>> favoritosDeUnaLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.buscarFavoritosDeLista(id));
    }

    @Operation(
            summary = "Eliminar una lista (falla si contiene favoritos)",
            description = "Requiere autenticación. Solo el dueño de la lista o un usuario ADMIN pueden eliminarla. Si tiene favoritos, devuelve 400 Bad Request."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLista(@PathVariable Long id) {
        listaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Mover todos los favoritos de una lista a otra y eliminar la original",
            description = "Requiere autenticación. Ambas listas deben pertenecer al usuario autenticado (salvo rol ADMIN). Operación transaccional."
    )
    @PostMapping("/{origenId}/mover-favoritos")
    public ResponseEntity<Void> moverFavoritos(
            @PathVariable Long origenId, 
            @RequestBody com.example.demo.dto.MoverFavoritosRequest request) {
        listaService.moverFavoritos(origenId, request.listaDestinoId());
        return ResponseEntity.ok().build();
    }
}