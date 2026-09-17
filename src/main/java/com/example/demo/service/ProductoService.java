package com.example.demo.service;

import com.example.demo.client.dummyjson.DummyJsonProducto;
import com.example.demo.client.dummyjson.DummyJsonProductosResponse;
import com.example.demo.dto.PageDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ServicioExternoException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ProductoService {

    private final RestClient restClient;

    public ProductoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductoDTO obtenerPorId(Long id) {
        try {
            DummyJsonProducto p = restClient.get()
                    .uri("/products/{id}", id) 
                    .retrieve()
                    .body(DummyJsonProducto.class);

            if (p == null) {
                throw new RecursoNoEncontradoException("El producto con ID " + id + " no existe.");
            }

            return new ProductoDTO(
                    p.id(),
                    p.title(),
                    p.description(),
                    p.brand(),
                    p.price()
            );

        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("El producto con ID " + id + " no fue encontrado en DummyJSON.");
        }
        catch (org.springframework.web.client.RestClientException e) {
            throw new ServicioExternoException ("Error al intentar conectarse con DummyJSON: " + e.getMessage(), e);
        }
    }

    public PageDTO<ProductoDTO> obtenerTodos(int page, int size) {
        int limit = size;
        int skip = page * size;

        DummyJsonProductosResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products")
                        .queryParam("limit", "{limit}")
                        .queryParam("skip", "{skip}")
                        .build(limit, skip))
                .retrieve()
                .body(DummyJsonProductosResponse.class);

        if (response == null || response.products() == null) {
            return new PageDTO<>(List.of(), page, size, 0, 0);
        }

        List<ProductoDTO> productosMapeados = response.products().stream()
                .map(p -> new ProductoDTO(
                        p.id(),
                        p.title(),
                        p.description(),
                        p.brand(),
                        p.price()
                ))
                .toList();

        int totalPages = (int) Math.ceil((double) response.total() / size);

        return new PageDTO<>(
                productosMapeados,
                page,
                size,
                response.total(),
                totalPages
        );
    }
}