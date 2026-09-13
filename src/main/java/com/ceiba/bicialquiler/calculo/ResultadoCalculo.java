package com.ceiba.bicialquiler.calculo;

import java.math.BigDecimal;

/**
 * Desglose del costo de un alquiler ya finalizado (RN-01 a RN-03).
 *
 * @param horasCobradas horas reales de uso, redondeadas al alza
 * @param costoBase      tarifa/hora del tipo de bicicleta multiplicada por {@code horasCobradas}
 * @param horasRetraso   horas de retraso sobre la duración estimada, redondeadas al alza (0 si no hubo retraso)
 * @param montoMulta     50% de la tarifa/hora multiplicado por {@code horasRetraso}
 * @param costoTotal     {@code costoBase + montoMulta}
 * @param tuvoMulta      true si {@code horasRetraso > 0}
 */
public record ResultadoCalculo(
        long horasCobradas,
        BigDecimal costoBase,
        long horasRetraso,
        BigDecimal montoMulta,
        BigDecimal costoTotal,
        boolean tuvoMulta
) {
}
