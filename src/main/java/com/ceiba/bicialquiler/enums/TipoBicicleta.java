package com.ceiba.bicialquiler.enums;

import java.math.BigDecimal;

public enum TipoBicicleta {

    URBANA(new BigDecimal("3500")),
    MONTAÑA(new BigDecimal("5000")),
    ELÉCTRICA(new BigDecimal("7500"));

    private final BigDecimal tarifaPorHora;

    TipoBicicleta(BigDecimal tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public BigDecimal getTarifaPorHora() {
        return tarifaPorHora;
    }
}
