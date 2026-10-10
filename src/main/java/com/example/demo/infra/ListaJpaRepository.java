package com.example.demo.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
}