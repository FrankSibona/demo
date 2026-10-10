package com.example.demo.infra;

import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository 
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Favorito save(Favorito favorito) {
        FavoritoEntity entity = toEntity(favorito);
        FavoritoEntity entityGuardada = jpaRepository.save(entity);
        return toDomain(entityGuardada);
    }

    @Override
    public List<Favorito> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        // Si tu interfaz devuelve Favorito en lugar de Optional, cambia el tipo de retorno 
        // a 'Favorito' y usa: return jpaRepository.findById(id).map(this::toDomain).orElse(null);
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return jpaRepository.findByListaId(listaId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // --- Métodos privados de mapeo ---

  private FavoritoEntity toEntity(Favorito favorito) {
        FavoritoEntity entity = new FavoritoEntity();
        entity.setId(favorito.getId()); 
        entity.setProductoId(favorito.getProductoId());
        entity.setNota(favorito.getNota());
        entity.setFechaAlta(favorito.getFechaCreacion());
        
        // Mapeo del ID de la lista a la entidad relacionada
        if (favorito.getListaId() != null) {
            ListaEntity listaRef = new ListaEntity();
            listaRef.setId(favorito.getListaId());
            entity.setLista(listaRef);
        }
        
        return entity; 
    }

    private Favorito toDomain(FavoritoEntity entity) {
        Favorito favorito = new Favorito();
        favorito.setId(entity.getId());
        favorito.setProductoId(entity.getProductoId());
        favorito.setNota(entity.getNota());
        favorito.setFechaCreacion(entity.getFechaAlta());
        
        // Extracción del ID de la lista si existe
        if (entity.getLista() != null) {
            favorito.setListaId(entity.getLista().getId());
        }
        
        return favorito;
    }
}