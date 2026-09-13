package com.ceiba.bicialquiler.service.impl;

import com.ceiba.bicialquiler.calculo.CalculadoraTarifas;
import com.ceiba.bicialquiler.calculo.ResultadoCalculo;
import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.FinalizarAlquilerRequest;
import com.ceiba.bicialquiler.dto.IniciarAlquilerRequest;
import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.exception.AlquilerNoEncontradoException;
import com.ceiba.bicialquiler.exception.AlquilerYaFinalizadoException;
import com.ceiba.bicialquiler.exception.BicicletaNoDisponibleException;
import com.ceiba.bicialquiler.exception.BicicletaNoEncontradaException;
import com.ceiba.bicialquiler.exception.SolicitudInvalidaException;
import com.ceiba.bicialquiler.model.Alquiler;
import com.ceiba.bicialquiler.model.Bicicleta;
import com.ceiba.bicialquiler.repository.AlquilerRepository;
import com.ceiba.bicialquiler.repository.BicicletaRepository;
import com.ceiba.bicialquiler.service.AlquilerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AlquilerServiceImpl implements AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final BicicletaRepository bicicletaRepository;
    private final CalculadoraTarifas calculadoraTarifas;

    public AlquilerServiceImpl(AlquilerRepository alquilerRepository,
                                BicicletaRepository bicicletaRepository,
                                CalculadoraTarifas calculadoraTarifas) {
        this.alquilerRepository = alquilerRepository;
        this.bicicletaRepository = bicicletaRepository;
        this.calculadoraTarifas = calculadoraTarifas;
    }

    @Override
    @Transactional
    public AlquilerResponse iniciar(IniciarAlquilerRequest request) {
        Bicicleta bicicleta = bicicletaRepository.findByCodigo(request.codigoBicicleta())
                .orElseThrow(() -> new BicicletaNoEncontradaException(request.codigoBicicleta()));

        // RN-04
        if (!bicicleta.estaDisponible()) {
            throw new BicicletaNoDisponibleException(bicicleta.getCodigo(), bicicleta.getEstado());
        }

        LocalDateTime horaInicio = request.horaInicio() != null ? request.horaInicio() : LocalDateTime.now();

        Alquiler alquiler = new Alquiler(bicicleta, request.clienteNombre(), horaInicio, request.duracionEstimadaHoras());
        bicicleta.setEstado(EstadoBicicleta.ALQUILADA);

        bicicletaRepository.save(bicicleta);
        Alquiler guardado = alquilerRepository.save(alquiler);

        return AlquilerResponse.desde(guardado);
    }

    @Override
    @Transactional
    public AlquilerResponse finalizar(Long alquilerId, FinalizarAlquilerRequest request) {
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
                .orElseThrow(() -> new AlquilerNoEncontradoException(alquilerId));

        if (!alquiler.estaActivo()) {
            throw new AlquilerYaFinalizadoException(alquilerId);
        }

        LocalDateTime horaFin = (request != null && request.horaFin() != null) ? request.horaFin() : LocalDateTime.now();

        if (horaFin.isBefore(alquiler.getHoraInicio())) {
            throw new SolicitudInvalidaException("La hora de devolución no puede ser anterior a la hora de inicio del alquiler");
        }

        Duration duracionReal = Duration.between(alquiler.getHoraInicio(), horaFin);
        Duration duracionEstimada = Duration.ofHours(alquiler.getDuracionEstimadaHoras());

        ResultadoCalculo resultado = calculadoraTarifas.calcular(alquiler.getBicicleta().getTipo(), duracionReal, duracionEstimada);

        alquiler.finalizar(horaFin, resultado);
        alquiler.getBicicleta().setEstado(EstadoBicicleta.DISPONIBLE);

        bicicletaRepository.save(alquiler.getBicicleta());
        Alquiler guardado = alquilerRepository.save(alquiler);

        return AlquilerResponse.desde(guardado);
    }
}
