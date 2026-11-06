package com.example.demo.infra;

import com.example.demo.model.Lista;
import com.example.demo.repository.ListaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity entity = toEntity(lista);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Lista> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private ListaEntity toEntity(Lista lista) {
        ListaEntity entity = new ListaEntity();
        entity.setId(lista.getId());
        entity.setNombre(lista.getNombre());
        if (lista.getUsuarioId() != null) {
            UsuarioEntity usuarioRef = new UsuarioEntity();
            usuarioRef.setId(lista.getUsuarioId());
            entity.setUsuario(usuarioRef);
        }
        return entity;
    }

    private Lista toDomain(ListaEntity entity) {
        Lista lista = new Lista(entity.getId(), entity.getNombre());
        if (entity.getUsuario() != null) {
            lista.setUsuarioId(entity.getUsuario().getId());
        }
        return lista;
    }
}
