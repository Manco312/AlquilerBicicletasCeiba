package com.ceiba.bicialquiler.controller;

import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.FinalizarAlquilerRequest;
import com.ceiba.bicialquiler.dto.IniciarAlquilerRequest;
import com.ceiba.bicialquiler.service.AlquilerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alquileres")
public class AlquilerController {

    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerService alquilerService) {
        this.alquilerService = alquilerService;
    }

    @PostMapping
    public ResponseEntity<AlquilerResponse> iniciar(@Valid @RequestBody IniciarAlquilerRequest request) {
        AlquilerResponse creado = alquilerService.iniciar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<AlquilerResponse> finalizar(@PathVariable Long id,
                                                       @RequestBody(required = false) FinalizarAlquilerRequest request) {
        return ResponseEntity.ok(alquilerService.finalizar(id, request));
    }
}
