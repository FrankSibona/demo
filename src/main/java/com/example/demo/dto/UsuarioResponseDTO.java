package com.example.demo.dto;

import com.example.demo.model.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String email,
    String rol
) {
    public static UsuarioResponseDTO desdeDominio(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getEmail(), usuario.getRol());
    }
}
