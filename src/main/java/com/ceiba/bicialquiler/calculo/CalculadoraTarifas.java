package com.ceiba.bicialquiler.calculo;

import com.ceiba.bicialquiler.enums.TipoBicicleta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class CalculadoraTarifas {

    private static final BigDecimal PORCENTAJE_MULTA = new BigDecimal("0.5");
    private static final long SEGUNDOS_POR_HORA = 3600L;

    /**
     * Calcula el desglose de costo de un alquiler.
     *
     * @param tipo              tipo de bicicleta (define la tarifa/hora, RN-01)
     * @param duracionReal      tiempo transcurrido entre inicio y devolución
     * @param duracionEstimada  duración estimada informada al iniciar el alquiler
     */
    public ResultadoCalculo calcular(TipoBicicleta tipo, Duration duracionReal, Duration duracionEstimada) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de bicicleta es obligatorio para calcular la tarifa");
        }
        if (duracionReal == null || duracionReal.isNegative()) {
            throw new IllegalArgumentException("La duración real del alquiler no puede ser negativa o nula");
        }
        if (duracionEstimada == null || duracionEstimada.isNegative()) {
            throw new IllegalArgumentException("La duración estimada del alquiler no puede ser negativa o nula");
        }

        BigDecimal tarifaHora = tipo.getTarifaPorHora();

        // redondeo al alza a la hora completa; un alquiler de menos de una
        // hora igual factura 1 hora completa.
        long horasCobradas = Math.max(1, redondearHorasAlAlza(duracionReal.getSeconds()));
        BigDecimal costoBase = tarifaHora.multiply(BigDecimal.valueOf(horasCobradas));

        // solo hay multa si la devolución fue posterior a lo estimado.
        long segundosRetraso = duracionReal.getSeconds() - duracionEstimada.getSeconds();
        long horasRetraso = segundosRetraso > 0 ? redondearHorasAlAlza(segundosRetraso) : 0;
        boolean tuvoMulta = horasRetraso > 0;

        BigDecimal montoMulta = tuvoMulta
                ? tarifaHora.multiply(PORCENTAJE_MULTA).multiply(BigDecimal.valueOf(horasRetraso))
                : BigDecimal.ZERO;

        BigDecimal costoTotal = costoBase.add(montoMulta);

        return new ResultadoCalculo(horasCobradas, costoBase, horasRetraso, montoMulta, costoTotal, tuvoMulta);
    }

    /**
     * Redondeo al alza (techo) usando aritmética entera para evitar los
     * problemas de precisión de {@code Math.ceil} con {@code double}.
     * 2h exactas (7200s) devuelve 2, no 3; 1h10min (4200s) devuelve 2.
     */
    private long redondearHorasAlAlza(long segundos) {
        if (segundos <= 0) {
            return 0;
        }
        return (segundos + SEGUNDOS_POR_HORA - 1) / SEGUNDOS_POR_HORA;
    }
}
