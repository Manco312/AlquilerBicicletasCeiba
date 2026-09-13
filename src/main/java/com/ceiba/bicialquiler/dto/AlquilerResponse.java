package com.ceiba.bicialquiler.dto;

import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.model.Alquiler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public record AlquilerResponse(
        Long id,
        String codigoBicicleta,
        TipoBicicleta tipoBicicleta,
        String clienteNombre,
        LocalDateTime horaInicio,
        LocalDateTime horaFin,
        Integer duracionEstimadaHoras,
        Long duracionRealMinutos,
        BigDecimal costoBase,
        BigDecimal montoMulta,
        BigDecimal costoTotal,
        boolean tuvoMulta,
        boolean activo
) {
    public static AlquilerResponse desde(Alquiler alquiler) {
        Long duracionRealMinutos = alquiler.getHoraFin() != null
                ? Duration.between(alquiler.getHoraInicio(), alquiler.getHoraFin()).toMinutes()
                : null;

        return new AlquilerResponse(
                alquiler.getId(),
                alquiler.getBicicleta().getCodigo(),
                alquiler.getBicicleta().getTipo(),
                alquiler.getClienteNombre(),
                alquiler.getHoraInicio(),
                alquiler.getHoraFin(),
                alquiler.getDuracionEstimadaHoras(),
                duracionRealMinutos,
                alquiler.getCostoBase(),
                alquiler.getMontoMulta(),
                alquiler.getCostoTotal(),
                alquiler.isTuvoMulta(),
                alquiler.estaActivo()
        );
    }
}
