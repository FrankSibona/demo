package com.example.demo.controller;

import com.example.demo.dto.PageDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Obtener catálogo paginado", description = "Devuelve productos paginados por page y size")
    public ResponseEntity<PageDTO<ProductoDTO>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageDTO<ProductoDTO> respuestaPaginada = productoService.obtenerTodos(page, size);
        return ResponseEntity.ok(respuestaPaginada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Busca un único producto en DummyJSON")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }
}