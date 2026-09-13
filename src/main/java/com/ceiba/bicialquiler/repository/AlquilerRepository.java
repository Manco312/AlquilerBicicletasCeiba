package com.ceiba.bicialquiler.repository;

import com.ceiba.bicialquiler.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    List<Alquiler> findByBicicletaCodigoOrderByHoraInicioDesc(String codigo);
}
