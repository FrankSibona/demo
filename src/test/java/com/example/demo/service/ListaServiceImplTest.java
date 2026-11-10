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
import org.mockito.ArgumentCaptor;
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
class ListaServiceImplTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    private ListaService listaService;

    @BeforeEach
    void setUp() {
        listaService = new ListaService(listaRepository, favoritoRepository);
        autenticarComo(10L, "usuario@test.com", "USER");
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
    void crear_conUsuarioAutenticado_asociaListaAUsuarioAutenticado() {
        // El cliente envía una lista e intenta asignar un usuarioId falso (999L)
        Lista listaAcrear = new Lista(null, "Mi Lista de Deseos", 999L);
        when(listaRepository.save(any(Lista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lista creada = listaService.crear(listaAcrear);

        assertNotNull(creada);
        // Debe asociar el usuarioId del usuario autenticado (10L), nunca el enviado en el body (999L)
        assertEquals(10L, creada.getUsuarioId());
        assertEquals("Mi Lista de Deseos", creada.getNombre());

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getUsuarioId());
    }

    @Test
    void obtener_listaPropia_retornaLista() {
        Lista miLista = new Lista(1L, "Mi Lista", 10L);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(miLista));

        Lista resultado = listaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getUsuarioId());
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        // La lista pertenece al usuario 99L, el usuario autenticado es 10L
        Lista listaAjena = new Lista(2L, "Lista de Otro", 99L);
        when(listaRepository.findById(2L)).thenReturn(Optional.of(listaAjena));

        assertThrows(AccesoDenegadoException.class, () -> listaService.buscarPorId(2L));
    }

    @Test
    void obtener_listaInexistente_lanzaRecursoNoEncontrado() {
        when(listaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> listaService.buscarPorId(999L));
    }

    @Test
    void obtener_listaAjenaComoAdmin_retornaLista() {
        autenticarComo(500L, "admin@test.com", "ADMIN");
        Lista listaDeOtro = new Lista(2L, "Lista de Otro", 99L);
        when(listaRepository.findById(2L)).thenReturn(Optional.of(listaDeOtro));

        Lista resultado = listaService.buscarPorId(2L);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
    }

    @Test
    void eliminar_listaAjena_lanzaAccesoDenegado() {
        Lista listaAjena = new Lista(2L, "Lista Ajena", 99L);
        when(listaRepository.findById(2L)).thenReturn(Optional.of(listaAjena));

        assertThrows(AccesoDenegadoException.class, () -> listaService.eliminar(2L));
        verify(listaRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_listaPropiaSinFavoritos_eliminaExitosamente() {
        Lista miLista = new Lista(1L, "Mi Lista", 10L);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(miLista));
        when(favoritoRepository.findByListaId(1L)).thenReturn(Collections.emptyList());

        listaService.eliminar(1L);

        verify(listaRepository).deleteById(1L);
    }
}
