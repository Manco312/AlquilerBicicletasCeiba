package com.ceiba.bicialquiler.repository;

import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.model.Bicicleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {

    Optional<Bicicleta> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Bicicleta> findByEstado(EstadoBicicleta estado);

    List<Bicicleta> findByEstadoAndTipo(EstadoBicicleta estado, TipoBicicleta tipo);
}
