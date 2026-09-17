package com.example.demo.repository;

import com.example.demo.model.Favorito;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFavoritoRepository implements FavoritoRepository {

    private final Map<Long, Favorito> db = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Favorito save(Favorito favorito) {
        if (favorito.getId() == null) {
            favorito.setId(idGenerator.getAndIncrement());
        }
        db.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public List<Favorito> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    public void deleteById(Long id) {
        db.remove(id);
    }
}

