package com.example.demo.exception;

public class ListaConFavoritosException extends RuntimeException {
    public ListaConFavoritosException(String mensaje) {
        super(mensaje);
    }
}