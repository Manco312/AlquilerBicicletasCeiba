package com.ceiba.bicialquiler.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Petición para iniciar un alquiler (RF-02).
 *
 * @param horaInicio opcional: si no se informa, se usa la hora actual del servidor.
 *                   Se permite indicarla explícitamente para facilitar pruebas
 *                   automatizadas y registros administrativos tardíos (supuesto
 *                   documentado en el README).
 */
public record IniciarAlquilerRequest(
        @NotBlank(message = "El código de la bicicleta es obligatorio") String codigoBicicleta,
        @NotBlank(message = "El nombre del cliente es obligatorio") String clienteNombre,
        @NotNull(message = "La duración estimada es obligatoria")
        @Min(value = 1, message = "La duración estimada debe ser de al menos 1 hora") Integer duracionEstimadaHoras,
        LocalDateTime horaInicio
) {
}
