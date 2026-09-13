package com.ceiba.bicialquiler.exception;

public class BicicletaNoEncontradaException extends RuntimeException {

    public BicicletaNoEncontradaException(String codigo) {
        super("No existe una bicicleta con código '" + codigo + "'");
    }
}
