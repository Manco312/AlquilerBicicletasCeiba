package com.ceiba.bicialquiler.dto;

import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para registrar una bicicleta (RF-01).
 *
 * @param estado opcional: si no se informa, se asume {@link EstadoBicicleta#DISPONIBLE}.
 */
public record BicicletaRequest(
        @NotBlank(message = "El código de la bicicleta es obligatorio") String codigo,
        @NotNull(message = "El tipo de bicicleta es obligatorio (URBANA, MONTAÑA o ELÉCTRICA)") TipoBicicleta tipo,
        EstadoBicicleta estado
) {
}
