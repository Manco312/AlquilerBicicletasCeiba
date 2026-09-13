package com.ceiba.bicialquiler.exception;

public class AlquilerYaFinalizadoException extends RuntimeException {

    public AlquilerYaFinalizadoException(Long id) {
        super("El alquiler con id " + id + " ya fue finalizado previamente");
    }
}
