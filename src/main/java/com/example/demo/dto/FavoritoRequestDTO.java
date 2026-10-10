package com.example.demo.dto;

import com.example.demo.model.Favorito;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FavoritoRequestDTO(
    @NotNull(message = "El ID del producto es obligatorio") 
    Long productoId,

    @NotNull (message = "")
    Long listaId,
    
    @NotBlank(message = "La nota no puede estar en blanco o vacía.")
    @Size(max = 255, message = "La nota no puede superar los 255 caracteres.")
    String nota
) {
    public Favorito aEntidad() {
        return new Favorito(null, this.productoId, this.listaId ,this.nota);
    }
}