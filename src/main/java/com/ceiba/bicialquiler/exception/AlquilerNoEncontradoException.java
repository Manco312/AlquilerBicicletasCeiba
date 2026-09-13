package com.ceiba.bicialquiler.exception;

public class AlquilerNoEncontradoException extends RuntimeException {

    public AlquilerNoEncontradoException(Long id) {
        super("No existe un alquiler con id " + id);
    }
}
