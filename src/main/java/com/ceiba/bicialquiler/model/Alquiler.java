package com.ceiba.bicialquiler.model;

import com.ceiba.bicialquiler.calculo.ResultadoCalculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alquileres")
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bicicleta_id", nullable = false)
    private Bicicleta bicicleta;

    @Column(nullable = false, length = 120)
    private String clienteNombre;

    @Column(nullable = false)
    private LocalDateTime horaInicio;

    @Column(nullable = false)
    private Integer duracionEstimadaHoras;

    /** Null mientras el alquiler está activo. */
    @Column
    private LocalDateTime horaFin;

    @Column(precision = 12, scale = 2)
    private BigDecimal costoBase;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoMulta;

    @Column(precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(nullable = false)
    private boolean tuvoMulta = false;

    protected Alquiler() {
        // Requerido por JPA
    }

    public Alquiler(Bicicleta bicicleta, String clienteNombre, LocalDateTime horaInicio, Integer duracionEstimadaHoras) {
        this.bicicleta = bicicleta;
        this.clienteNombre = clienteNombre;
        this.horaInicio = horaInicio;
        this.duracionEstimadaHoras = duracionEstimadaHoras;
    }

    public boolean estaActivo() {
        return horaFin == null;
    }

    /**
     * Cierra el alquiler de forma consistente: no es posible dejarlo con una
     * hora de fin sin su costo calculado.
     */
    public void finalizar(LocalDateTime horaFin, ResultadoCalculo resultado) {
        if (!estaActivo()) {
            throw new IllegalStateException("El alquiler ya se encuentra finalizado");
        }
        this.horaFin = horaFin;
        this.costoBase = resultado.costoBase();
        this.montoMulta = resultado.montoMulta();
        this.costoTotal = resultado.costoTotal();
        this.tuvoMulta = resultado.tuvoMulta();
    }

    public Long getId() {
        return id;
    }

    public Bicicleta getBicicleta() {
        return bicicleta;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public Integer getDuracionEstimadaHoras() {
        return duracionEstimadaHoras;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public BigDecimal getMontoMulta() {
        return montoMulta;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public boolean isTuvoMulta() {
        return tuvoMulta;
    }
}
