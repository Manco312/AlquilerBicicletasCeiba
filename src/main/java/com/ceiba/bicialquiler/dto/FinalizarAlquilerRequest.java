package com.ceiba.bicialquiler.dto;

import java.time.LocalDateTime;

/**
 * Petición para finalizar un alquiler (RF-03).
 *
 * @param horaFin opcional: si no se informa (o si no se envía cuerpo), se usa
 *                la hora actual del servidor. Permitir indicarla facilita las
 *                pruebas automatizadas de RN-02/RN-03 sin depender del reloj real.
 */
public record FinalizarAlquilerRequest(
        LocalDateTime horaFin
) {
}
