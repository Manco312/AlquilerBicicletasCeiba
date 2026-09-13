package com.ceiba.bicialquiler.dto;

import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.model.Bicicleta;

public record BicicletaResponse(
        Long id,
        String codigo,
        TipoBicicleta tipo,
        EstadoBicicleta estado
) {
    public static BicicletaResponse desde(Bicicleta bicicleta) {
        return new BicicletaResponse(bicicleta.getId(), bicicleta.getCodigo(), bicicleta.getTipo(), bicicleta.getEstado());
    }
}
