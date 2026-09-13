package com.ceiba.bicialquiler.config;

import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.model.Bicicleta;

import java.util.List;

public final class DatosReferencia {

    private DatosReferencia() {
    }

    public static List<Bicicleta> bicicletasDeReferencia() {
        return List.of(
                new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE),
                new Bicicleta("BIC-002", TipoBicicleta.MONTAÑA, EstadoBicicleta.DISPONIBLE),
                new Bicicleta("BIC-003", TipoBicicleta.ELÉCTRICA, EstadoBicicleta.DISPONIBLE),
                new Bicicleta("BIC-004", TipoBicicleta.MONTAÑA, EstadoBicicleta.EN_MANTENIMIENTO),
                new Bicicleta("BIC-005", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE)
        );
    }
}
