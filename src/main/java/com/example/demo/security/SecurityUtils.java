package com.example.demo.security;

import com.example.demo.exception.AccesoDenegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UsuarioPrincipal> getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UsuarioPrincipal getUsuarioAutenticadoOError() {
        return getUsuarioAutenticado()
                .orElseThrow(() -> new AccesoDenegadoException("No se encontró usuario autenticado en la sesión"));
    }
}
