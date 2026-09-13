package com.ceiba.bicialquiler.calculo;

import com.ceiba.bicialquiler.enums.TipoBicicleta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de negocio RN-01 a RN-03. No requieren Spring ni base
 * de datos: {@link CalculadoraTarifas} es una clase de cálculo puro.
 */
class CalculadoraTarifasTest {

    private final CalculadoraTarifas calculadora = new CalculadoraTarifas();

    @Test
    void debeRedondearAlAlzaCuandoSobranMinutos() {
        // 1h10min -> se cobra como 2 horas (RN-02)
        ResultadoCalculo resultado = calculadora.calcular(TipoBicicleta.URBANA, Duration.ofMinutes(70), Duration.ofHours(3));

        assertEquals(2, resultado.horasCobradas());
        assertEquals(0, new BigDecimal("7000").compareTo(resultado.costoBase()));
        assertFalse(resultado.tuvoMulta());
    }

    @Test
    void noDebeRedondearCuandoLaDuracionEsExactaEnHoras() {
        // 2h exactas -> se cobra como 2 horas, no 3 (RN-02, caso borde)
        ResultadoCalculo resultado = calculadora.calcular(TipoBicicleta.URBANA, Duration.ofHours(2), Duration.ofHours(3));

        assertEquals(2, resultado.horasCobradas());
        assertEquals(0, new BigDecimal("7000").compareTo(resultado.costoBase()));
    }

    @Test
    void debeReproducirElEjemploDelEnunciado() {
        // MONTAÑA, estimada 2h, devuelta a las 3h20min -> base 20000 + multa 5000 = 25000
        ResultadoCalculo resultado = calculadora.calcular(
                TipoBicicleta.MONTAÑA, Duration.ofHours(3).plusMinutes(20), Duration.ofHours(2));

        assertEquals(4, resultado.horasCobradas());
        assertEquals(0, new BigDecimal("20000").compareTo(resultado.costoBase()));
        assertEquals(2, resultado.horasRetraso());
        assertEquals(0, new BigDecimal("5000").compareTo(resultado.montoMulta()));
        assertEquals(0, new BigDecimal("25000").compareTo(resultado.costoTotal()));
        assertTrue(resultado.tuvoMulta());
    }

    @Test
    void noDebeAplicarMultaSiSeDevuelveATiempoOAntes() {
        ResultadoCalculo resultado = calculadora.calcular(TipoBicicleta.ELÉCTRICA, Duration.ofHours(2), Duration.ofHours(3));

        assertEquals(0, resultado.horasRetraso());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.montoMulta()));
        assertFalse(resultado.tuvoMulta());
    }

    @Test
    void unSegundoDeRetrasoDebeCobrarUnaHoraCompletaDeMulta() {
        // RN-03: el retraso mínimo facturable es de 1 hora
        ResultadoCalculo resultado = calculadora.calcular(
                TipoBicicleta.URBANA, Duration.ofHours(2).plusSeconds(1), Duration.ofHours(2));

        assertEquals(1, resultado.horasRetraso());
        assertTrue(resultado.tuvoMulta());
        assertEquals(0, new BigDecimal("1750").compareTo(resultado.montoMulta())); // 3500 * 0.5 * 1h
    }

    @Test
    void unAlquilerMuyCortoDebeFacturarComoMinimoUnaHora() {
        ResultadoCalculo resultado = calculadora.calcular(TipoBicicleta.ELÉCTRICA, Duration.ofMinutes(10), Duration.ofHours(1));

        assertEquals(1, resultado.horasCobradas());
        assertEquals(0, new BigDecimal("7500").compareTo(resultado.costoBase()));
    }

    @Test
    void debeRechazarDuracionRealNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(TipoBicicleta.URBANA, Duration.ofMinutes(-5), Duration.ofHours(1)));
    }

    @Test
    void debeRechazarDuracionEstimadaNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(TipoBicicleta.URBANA, Duration.ofHours(1), Duration.ofMinutes(-1)));
    }

    @Test
    void debeAplicarLaTarifaCorrectaSegunElTipoDeBicicleta() {
        assertEquals(0, new BigDecimal("3500").compareTo(
                calculadora.calcular(TipoBicicleta.URBANA, Duration.ofHours(1), Duration.ofHours(1)).costoBase()));
        assertEquals(0, new BigDecimal("5000").compareTo(
                calculadora.calcular(TipoBicicleta.MONTAÑA, Duration.ofHours(1), Duration.ofHours(1)).costoBase()));
        assertEquals(0, new BigDecimal("7500").compareTo(
                calculadora.calcular(TipoBicicleta.ELÉCTRICA, Duration.ofHours(1), Duration.ofHours(1)).costoBase()));
    }
}
