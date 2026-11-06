package com.example.demo.service;

import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaServiceTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    private ListaService listaService;

    @BeforeEach
    void setUp() {
        listaService = new ListaService(listaRepository, favoritoRepository);
        autenticarComo(1L, "user@test.com", "USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Long id, String email, String rol) {
        UsuarioPrincipal principal = new UsuarioPrincipal(id, email, rol);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void crear_asignaUsuarioIdDelAutenticado() {
        Lista entrada = new Lista(null, "Mis Compras", 999L); // Intenta enviar otro usuarioId
        when(listaRepository.save(any(Lista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lista guardada = listaService.crear(entrada);

        assertNotNull(guardada);
        assertEquals(1L, guardada.getUsuarioId()); // Sobrescrito por el autenticado
        assertEquals("Mis Compras", guardada.getNombre());
    }

    @Test
    void buscarTodas_usuarioNormal_devuelveSoloSusListas() {
        Lista miLista = new Lista(10L, "Mi Lista", 1L);
        when(listaRepository.findByUsuarioId(1L)).thenReturn(List.of(miLista));

        List<Lista> resultado = listaService.buscarTodas();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        verify(listaRepository).findByUsuarioId(1L);
        verify(listaRepository, never()).findAll();
    }

    @Test
    void buscarTodas_admin_devuelveTodasLasListas() {
        autenticarComo(99L, "admin@test.com", "ADMIN");
        Lista lista1 = new Lista(1L, "L1", 1L);
        Lista lista2 = new Lista(2L, "L2", 2L);
        when(listaRepository.findAll()).thenReturn(List.of(lista1, lista2));

        List<Lista> resultado = listaService.buscarTodas();

        assertEquals(2, resultado.size());
        verify(listaRepository).findAll();
        verify(listaRepository, never()).findByUsuarioId(any());
    }

    @Test
    void buscarPorId_propietario_retornaLista() {
        Lista miLista = new Lista(10L, "Mi Lista", 1L);
        when(listaRepository.findById(10L)).thenReturn(Optional.of(miLista));

        Lista resultado = listaService.buscarPorId(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    void buscarPorId_admin_retornaListaDeOtroUsuario() {
        autenticarComo(99L, "admin@test.com", "ADMIN");
        Lista listaDeOtro = new Lista(20L, "Lista Ajena", 5L);
        when(listaRepository.findById(20L)).thenReturn(Optional.of(listaDeOtro));

        Lista resultado = listaService.buscarPorId(20L);

        assertNotNull(resultado);
        assertEquals(20L, resultado.getId());
    }

    @Test
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado404() {
        when(listaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> listaService.buscarPorId(999L));
    }

    @Test
    void buscarPorId_noEsDuenioNiAdmin_lanzaAccesoDenegado403() {
        Lista listaDeOtro = new Lista(20L, "Lista de Otro", 2L); // Pertenece al usuario 2, estamos logueados como 1
        when(listaRepository.findById(20L)).thenReturn(Optional.of(listaDeOtro));

        assertThrows(AccesoDenegadoException.class, () -> listaService.buscarPorId(20L));
    }

    @Test
    void eliminar_noEsDuenio_lanzaAccesoDenegado403() {
        Lista listaDeOtro = new Lista(20L, "Lista de Otro", 2L);
        when(listaRepository.findById(20L)).thenReturn(Optional.of(listaDeOtro));

        assertThrows(AccesoDenegadoException.class, () -> listaService.eliminar(20L));
        verify(listaRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_esDuenioYSinFavoritos_eliminaCorrectamente() {
        Lista miLista = new Lista(10L, "Mi Lista", 1L);
        when(listaRepository.findById(10L)).thenReturn(Optional.of(miLista));
        when(favoritoRepository.findByListaId(10L)).thenReturn(Collections.emptyList());

        listaService.eliminar(10L);

        verify(listaRepository).deleteById(10L);
    }
}
