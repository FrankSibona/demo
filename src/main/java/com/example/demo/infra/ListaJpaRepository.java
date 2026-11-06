package com.example.demo.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
    List<ListaEntity> findByUsuarioId(Long usuarioId);
}