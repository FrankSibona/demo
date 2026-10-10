package com.example.demo.service;

import com.example.demo.exception.ListaConFavoritosException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
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
        return listaRepository.save(lista);
    }

    public List<Lista> buscarTodas() {
        return listaRepository.findAll();
    }

    public Lista buscarPorId(Long id) {
        return listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
    }

    public List<Favorito> buscarFavoritosDeLista(Long listaId) {
        // Primero verificamos que la lista exista (reutilizando el método de arriba)
        buscarPorId(listaId); 
        return favoritoRepository.findByListaId(listaId);
    }

    public void eliminar(Long id) {
        List<Favorito> favoritos = favoritoRepository.findByListaId(id);
        
        if (!favoritos.isEmpty()) {
            throw new ListaConFavoritosException("No se puede eliminar la lista porque tiene favoritos asociados.");
        }
        
        listaRepository.deleteById(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        Lista origen = buscarPorId(origenId);
        Lista destino = buscarPorId(destinoId);

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);

        for (Favorito fav : favoritos) {
            fav.setListaId(destinoId);
            favoritoRepository.save(fav);
        }

        listaRepository.deleteById(origenId);
    }
}