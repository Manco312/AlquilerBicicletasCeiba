package com.ceiba.bicialquiler.service;

import com.ceiba.bicialquiler.calculo.CalculadoraTarifas;
import com.ceiba.bicialquiler.calculo.ResultadoCalculo;
import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.FinalizarAlquilerRequest;
import com.ceiba.bicialquiler.dto.IniciarAlquilerRequest;
import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.exception.AlquilerNoEncontradoException;
import com.ceiba.bicialquiler.exception.AlquilerYaFinalizadoException;
import com.ceiba.bicialquiler.exception.BicicletaNoDisponibleException;
import com.ceiba.bicialquiler.exception.BicicletaNoEncontradaException;
import com.ceiba.bicialquiler.exception.SolicitudInvalidaException;
import com.ceiba.bicialquiler.model.Alquiler;
import com.ceiba.bicialquiler.model.Bicicleta;
import com.ceiba.bicialquiler.repository.AlquilerRepository;
import com.ceiba.bicialquiler.repository.BicicletaRepository;
import com.ceiba.bicialquiler.service.impl.AlquilerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la orquestación de {@link AlquilerServiceImpl}: RN-04
 * (no alquilar bici no disponible) y RN-05 (no finalizar alquiler inexistente
 * o ya finalizado). Los repositorios se mockean; la calculadora de tarifas se
 * usa real porque es una clase de cálculo puro sin dependencias externas.
 */
@ExtendWith(MockitoExtension.class)
class AlquilerServiceImplTest {

    @Mock
    private AlquilerRepository alquilerRepository;

    @Mock
    private BicicletaRepository bicicletaRepository;

    private AlquilerServiceImpl alquilerService;

    @BeforeEach
    void setUp() {
        alquilerService = new AlquilerServiceImpl(alquilerRepository, bicicletaRepository, new CalculadoraTarifas());
    }

    @Test
    void debeRechazarInicioSiLaBicicletaNoExiste() {
        when(bicicletaRepository.findByCodigo("BIC-999")).thenReturn(Optional.empty());

        IniciarAlquilerRequest request = new IniciarAlquilerRequest("BIC-999", "Juan", 2, null);

        assertThrows(BicicletaNoEncontradaException.class, () -> alquilerService.iniciar(request));
    }

    @Test
    void debeRechazarInicioSiLaBicicletaNoEstaDisponible() {
        Bicicleta bici = new Bicicleta("BIC-004", TipoBicicleta.MONTAÑA, EstadoBicicleta.EN_MANTENIMIENTO);
        when(bicicletaRepository.findByCodigo("BIC-004")).thenReturn(Optional.of(bici));

        IniciarAlquilerRequest request = new IniciarAlquilerRequest("BIC-004", "Ana", 1, null);

        assertThrows(BicicletaNoDisponibleException.class, () -> alquilerService.iniciar(request));
    }

    @Test
    void debeIniciarAlquilerYMarcarBicicletaComoAlquilada() {
        Bicicleta bici = new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE);
        when(bicicletaRepository.findByCodigo("BIC-001")).thenReturn(Optional.of(bici));
        when(alquilerRepository.save(any(Alquiler.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime inicio = LocalDateTime.now();
        AlquilerResponse response = alquilerService.iniciar(new IniciarAlquilerRequest("BIC-001", "Ana", 2, inicio));

        assertEquals(EstadoBicicleta.ALQUILADA, bici.getEstado());
        assertEquals("BIC-001", response.codigoBicicleta());
        assertTrue(response.activo());
        verify(bicicletaRepository).save(bici);
    }

    @Test
    void debeRechazarFinalizacionDeAlquilerInexistente() {
        when(alquilerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AlquilerNoEncontradoException.class, () -> alquilerService.finalizar(99L, null));
    }

    @Test
    void debeRechazarFinalizacionDeAlquilerYaFinalizado() {
        Bicicleta bici = new Bicicleta("BIC-002", TipoBicicleta.MONTAÑA, EstadoBicicleta.DISPONIBLE);
        Alquiler alquiler = new Alquiler(bici, "Luis", LocalDateTime.now().minusHours(3), 2);
        alquiler.finalizar(LocalDateTime.now(), new ResultadoCalculo(3, BigDecimal.TEN, 0, BigDecimal.ZERO, BigDecimal.TEN, false));
        when(alquilerRepository.findById(5L)).thenReturn(Optional.of(alquiler));

        assertThrows(AlquilerYaFinalizadoException.class, () -> alquilerService.finalizar(5L, null));
    }

    @Test
    void debeFinalizarAlquilerCalcularCostoYLiberarLaBicicleta() {
        Bicicleta bici = new Bicicleta("BIC-002", TipoBicicleta.MONTAÑA, EstadoBicicleta.ALQUILADA);
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 10, 0);
        Alquiler alquiler = new Alquiler(bici, "Luis", inicio, 2);
        when(alquilerRepository.findById(1L)).thenReturn(Optional.of(alquiler));
        when(alquilerRepository.save(any(Alquiler.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime fin = inicio.plusHours(3).plusMinutes(20);
        AlquilerResponse response = alquilerService.finalizar(1L, new FinalizarAlquilerRequest(fin));

        assertEquals(EstadoBicicleta.DISPONIBLE, bici.getEstado());
        assertEquals(0, new BigDecimal("25000").compareTo(response.costoTotal()));
        assertTrue(response.tuvoMulta());
        assertFalse(response.activo());
    }

    @Test
    void debeRechazarFinalizacionConHoraFinAnteriorAlInicio() {
        Bicicleta bici = new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.ALQUILADA);
        LocalDateTime inicio = LocalDateTime.now();
        Alquiler alquiler = new Alquiler(bici, "Marta", inicio, 1);
        when(alquilerRepository.findById(2L)).thenReturn(Optional.of(alquiler));

        FinalizarAlquilerRequest request = new FinalizarAlquilerRequest(inicio.minusMinutes(5));

        assertThrows(SolicitudInvalidaException.class, () -> alquilerService.finalizar(2L, request));
    }
}
