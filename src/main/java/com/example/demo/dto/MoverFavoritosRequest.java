package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record MoverFavoritosRequest(
    @NotNull(message = "El ID de la lista destino es obligatorio")
    Long listaDestinoId
) {}