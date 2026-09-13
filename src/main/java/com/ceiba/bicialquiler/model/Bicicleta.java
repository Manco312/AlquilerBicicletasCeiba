package com.ceiba.bicialquiler.model;

import com.ceiba.bicialquiler.enums.EstadoBicicleta;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "bicicletas", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class Bicicleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBicicleta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBicicleta estado;

    /**
     * evita que dos alquileres concurrentes sobre la misma
     * bicicleta la dejen en un estado inconsistente.
     */
    @Version
    private Long version;

    protected Bicicleta() {
        // Requerido por JPA
    }

    public Bicicleta(String codigo, TipoBicicleta tipo, EstadoBicicleta estado) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.estado = estado;
    }

    public boolean estaDisponible() {
        return estado == EstadoBicicleta.DISPONIBLE;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoBicicleta getTipo() {
        return tipo;
    }

    public EstadoBicicleta getEstado() {
        return estado;
    }

    public void setEstado(EstadoBicicleta estado) {
        this.estado = estado;
    }
}
