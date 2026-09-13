package com.ceiba.bicialquiler.service;

import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.FinalizarAlquilerRequest;
import com.ceiba.bicialquiler.dto.IniciarAlquilerRequest;

public interface AlquilerService {

    AlquilerResponse iniciar(IniciarAlquilerRequest request);

    AlquilerResponse finalizar(Long alquilerId, FinalizarAlquilerRequest request);
}
