package com.example.demo.dto;

import com.example.demo.model.Favorito;
import java.time.LocalDateTime;

public record FavoritoResponseDTO(
    Long id,
    Long productoId,
    String nota,
    LocalDateTime fechaCreacion
) {
    
    public static FavoritoResponseDTO desdeEntidad(Favorito favorito) {
        if (favorito == null) {
            return null;
        }
        return new FavoritoResponseDTO(
            favorito.getId(),
            favorito.getProductoId(),
            favorito.getNota(),
            favorito.getFechaCreacion()
        );
    }
}