package com.ceiba.bicialquiler.exception;

import com.ceiba.bicialquiler.enums.EstadoBicicleta;

public class BicicletaNoDisponibleException extends RuntimeException {

    public BicicletaNoDisponibleException(String codigo, EstadoBicicleta estadoActual) {
        super("La bicicleta '" + codigo + "' no está disponible para alquiler (estado actual: " + estadoActual + ")");
    }
}
