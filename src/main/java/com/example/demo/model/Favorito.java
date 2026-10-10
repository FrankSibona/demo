package com.example.demo.model;

import java.time.LocalDateTime;

public class Favorito {
    private Long id;
    private Long productoId;
    private Long listaId;
   

    private String nota;
    private LocalDateTime fechaCreacion;

    public Favorito() {
    }

    public Favorito(Long id, Long productoId,Long listaId, String nota) {
        this.id = id;
        this.productoId = productoId;
        this.listaId = listaId;
        this.nota = nota;
        this.fechaCreacion = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
     public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }
}
