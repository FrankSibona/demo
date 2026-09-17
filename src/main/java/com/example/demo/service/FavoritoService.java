package com.example.demo.service;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProductoService productoService;

    public FavoritoService(FavoritoRepository favoritoRepository, ProductoService productoService) {
        this.favoritoRepository = favoritoRepository;
        this.productoService = productoService;
    }

    public FavoritoResponseDTO guardarFavorito(FavoritoRequestDTO request) {
        productoService.obtenerPorId(request.productoId());
        Favorito guardado = favoritoRepository.save(request.aEntidad());
        return FavoritoResponseDTO.desdeEntidad(guardado);
    }

    public List<FavoritoResponseDTO> obtenerTodos() {
        return favoritoRepository.findAll().stream()
                .map(FavoritoResponseDTO::desdeEntidad)
                .toList();
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El favorito con ID " + id + " no existe."));
        return FavoritoResponseDTO.desdeEntidad(favorito);
    }

    public FavoritoResponseDTO actualizarFavorito(Long id, FavoritoRequestDTO request) {
        Favorito existente = favoritoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El favorito con ID " + id + " no existe."));

        if (!existente.getProductoId().equals(request.productoId())) {
            productoService.obtenerPorId(request.productoId());
        }

        existente.setProductoId(request.productoId());
        existente.setNota(request.nota());

        Favorito guardado = favoritoRepository.save(existente);
        return FavoritoResponseDTO.desdeEntidad(guardado);
    }

    public void eliminarFavorito(Long id) {
        favoritoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El favorito con ID " + id + " no existe."));
        
        favoritoRepository.deleteById(id);
    }
}