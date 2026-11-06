package com.example.demo.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_conTokenValido_estableceAutenticacionEnContexto() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token.jwt.valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validarToken("token.jwt.valido")).thenReturn(true);
        when(jwtService.obtenerClaims("token.jwt.valido")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("usuario@test.com");
        when(claims.get("id")).thenReturn(99L);
        when(claims.get("rol", String.class)).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getPrincipal() instanceof UsuarioPrincipal);

        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        assertEquals("usuario@test.com", principal.getEmail());
        assertEquals(99L, principal.getId());
        assertEquals("USER", principal.getRol());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_sinHeaderAuthorization_noEstableceAutenticacion() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_conTokenInvalido_noEstableceAutenticacion() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token.invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validarToken("token.invalido")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
