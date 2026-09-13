package com.ceiba.bicialquiler.service.impl;

import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.BicicletaRequest;
import com.ceiba.bicialquiler.dto.BicicletaResponse;
import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.exception.BicicletaCodigoDuplicadoException;
import com.ceiba.bicialquiler.exception.BicicletaNoEncontradaException;
import com.ceiba.bicialquiler.model.Bicicleta;
import com.ceiba.bicialquiler.repository.AlquilerRepository;
import com.ceiba.bicialquiler.repository.BicicletaRepository;
import com.ceiba.bicialquiler.service.BicicletaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BicicletaServiceImpl implements BicicletaService {

    private final BicicletaRepository bicicletaRepository;
    private final AlquilerRepository alquilerRepository;

    public BicicletaServiceImpl(BicicletaRepository bicicletaRepository, AlquilerRepository alquilerRepository) {
        this.bicicletaRepository = bicicletaRepository;
        this.alquilerRepository = alquilerRepository;
    }

    @Override
    @Transactional
    public BicicletaResponse crear(BicicletaRequest request) {
        if (bicicletaRepository.existsByCodigo(request.codigo())) {
            throw new BicicletaCodigoDuplicadoException(request.codigo());
        }
        EstadoBicicleta estadoInicial = request.estado() != null ? request.estado() : EstadoBicicleta.DISPONIBLE;
        Bicicleta bicicleta = new Bicicleta(request.codigo(), request.tipo(), estadoInicial);
        return BicicletaResponse.desde(bicicletaRepository.save(bicicleta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BicicletaResponse> listarTodas() {
        return bicicletaRepository.findAll().stream()
                .map(BicicletaResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BicicletaResponse> consultarDisponibles(TipoBicicleta tipo) {
        List<Bicicleta> bicicletas = tipo != null
                ? bicicletaRepository.findByEstadoAndTipo(EstadoBicicleta.DISPONIBLE, tipo)
                : bicicletaRepository.findByEstado(EstadoBicicleta.DISPONIBLE);
        return bicicletas.stream().map(BicicletaResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlquilerResponse> obtenerHistorial(String codigo) {
        if (!bicicletaRepository.existsByCodigo(codigo)) {
            throw new BicicletaNoEncontradaException(codigo);
        }
        return alquilerRepository.findByBicicletaCodigoOrderByHoraInicioDesc(codigo).stream()
                .map(AlquilerResponse::desde)
                .toList();
    }
}
