package com.example.demo.infra;

import com.example.demo.model.Favorito; // Tu record del TP1
import com.example.demo.repository.FavoritoRepository; // Tu puerto del TP1

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.stream.Collectors;

public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, Long> {
    List<FavoritoEntity> findByListaId(Long listaId);
}