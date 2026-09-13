package com.ceiba.bicialquiler.service;

import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.BicicletaRequest;
import com.ceiba.bicialquiler.dto.BicicletaResponse;
import com.ceiba.bicialquiler.enums.TipoBicicleta;

import java.util.List;

public interface BicicletaService {

    BicicletaResponse crear(BicicletaRequest request);

    List<BicicletaResponse> listarTodas();

    /**
     * bicicletas DISPONIBLES en este momento, opcionalmente filtradas por tipo.
     *
     * @param tipo puede ser null para no filtrar.
     */
    List<BicicletaResponse> consultarDisponibles(TipoBicicleta tipo);

    List<AlquilerResponse> obtenerHistorial(String codigo);
}
