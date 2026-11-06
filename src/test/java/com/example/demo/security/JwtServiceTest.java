package com.example.demo.security;

import com.example.demo.model.Usuario;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "clave-secreta-para-tests-de-al-menos-256-bits-segura-1234567890";
    private final long expiration = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expiration);
    }

    @Test
    void generarToken_generaTokenValidoConClaimsCorrectos() {
        Usuario usuario = new Usuario(42L, "usuario@test.com", "hash", "USER");

        String token = jwtService.generarToken(usuario);

        assertNotNull(token);
        assertTrue(jwtService.validarToken(token));
        assertEquals("usuario@test.com", jwtService.obtenerEmail(token));
        assertEquals(42L, jwtService.obtenerId(token));
        assertEquals("USER", jwtService.obtenerRol(token));

        Claims claims = jwtService.obtenerClaims(token);
        assertEquals("usuario@test.com", claims.getSubject());
        assertEquals(42, ((Number) claims.get("id")).intValue());
        assertEquals("USER", claims.get("rol"));
        assertNotNull(claims.getExpiration());
    }

    @Test
    void validarToken_tokenInvalidoOAlterado_retornaFalse() {
        assertFalse(jwtService.validarToken("token.invalido.falso"));
        assertFalse(jwtService.validarToken(""));
        assertFalse(jwtService.validarToken(null));
    }

    @Test
    void validarToken_tokenExpirado_retornaFalse() {
        JwtService expiredJwtService = new JwtService(secret, -1000); // expirado
        Usuario usuario = new Usuario(1L, "expirado@test.com", "hash", "USER");
        String tokenExpirado = expiredJwtService.generarToken(usuario);

        assertFalse(jwtService.validarToken(tokenExpirado));
    }
}
