package com.ceiba.bicialquiler.exception;

public class BicicletaCodigoDuplicadoException extends RuntimeException {

    public BicicletaCodigoDuplicadoException(String codigo) {
        super("Ya existe una bicicleta registrada con el código '" + codigo + "'");
    }
}
