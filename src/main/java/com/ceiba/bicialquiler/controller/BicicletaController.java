package com.ceiba.bicialquiler.controller;

import com.ceiba.bicialquiler.dto.AlquilerResponse;
import com.ceiba.bicialquiler.dto.BicicletaRequest;
import com.ceiba.bicialquiler.dto.BicicletaResponse;
import com.ceiba.bicialquiler.enums.TipoBicicleta;
import com.ceiba.bicialquiler.service.BicicletaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bicicletas")
public class BicicletaController {

    private final BicicletaService bicicletaService;

    public BicicletaController(BicicletaService bicicletaService) {
        this.bicicletaService = bicicletaService;
    }

    @PostMapping
    public ResponseEntity<BicicletaResponse> crear(@Valid @RequestBody BicicletaRequest request) {
        BicicletaResponse creada = bicicletaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(creada.codigo())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<BicicletaResponse>> listarTodas() {
        return ResponseEntity.ok(bicicletaService.listarTodas());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<BicicletaResponse>> consultarDisponibles(
            @RequestParam(required = false) TipoBicicleta tipo) {
        return ResponseEntity.ok(bicicletaService.consultarDisponibles(tipo));
    }

    @GetMapping("/{codigo}/historial")
    public ResponseEntity<List<AlquilerResponse>> historial(@PathVariable String codigo) {
        return ResponseEntity.ok(bicicletaService.obtenerHistorial(codigo));
    }
}
