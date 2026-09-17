package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import com.example.demo.model.Favorito;

public interface FavoritoRepository {
    Favorito save(Favorito favorito);
    Optional<Favorito> findById(Long id);
    List<Favorito> findAll();
    void deleteById(Long id);
}
