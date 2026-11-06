package com.example.demo.service;

import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaConFavoritosException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.SecurityUtils;
import com.example.demo.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaService(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    public Lista crear(Lista lista) {
        UsuarioPrincipal usuario = SecurityUtils.getUsuarioAutenticadoOError();
        lista.setUsuarioId(usuario.getId());
        return listaRepository.save(lista);
    }

    public List<Lista> buscarTodas() {
        UsuarioPrincipal usuario = SecurityUtils.getUsuarioAutenticadoOError();
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return listaRepository.findAll();
        }
        return listaRepository.findByUsuarioId(usuario.getId());
    }

    public Lista buscarPorId(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada con id: " + id));

        UsuarioPrincipal usuario = SecurityUtils.getUsuarioAutenticadoOError();
        validarPropiedad(lista, usuario);
        return lista;
    }

    public List<Favorito> buscarFavoritosDeLista(Long listaId) {
        // Primero verificamos que la lista exista y pertenezca al usuario (o admin)
        buscarPorId(listaId);
        return favoritoRepository.findByListaId(listaId);
    }

    public void eliminar(Long id) {
        // Valida existencia (404) y pertenencia (403)
        buscarPorId(id);

        List<Favorito> favoritos = favoritoRepository.findByListaId(id);
        if (!favoritos.isEmpty()) {
            throw new ListaConFavoritosException("No se puede eliminar la lista porque tiene favoritos asociados.");
        }

        listaRepository.deleteById(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        // Valida existencia (404) y pertenencia (403) para ambas listas
        Lista origen = buscarPorId(origenId);
        Lista destino = buscarPorId(destinoId);

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito fav : favoritos) {
            fav.setListaId(destinoId);
            favoritoRepository.save(fav);
        }

        listaRepository.deleteById(origenId);
    }

    private void validarPropiedad(Lista lista, UsuarioPrincipal usuario) {
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return;
        }
        if (lista.getUsuarioId() == null || !lista.getUsuarioId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("No tienes permisos para acceder o modificar esta lista");
        }
    }
}
