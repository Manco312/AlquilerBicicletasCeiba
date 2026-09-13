package com.ceiba.bicialquiler.controller;

import com.ceiba.bicialquiler.config.DatosReferencia;
import com.ceiba.bicialquiler.repository.AlquilerRepository;
import com.ceiba.bicialquiler.repository.BicicletaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de extremo a extremo del flujo de alquiler, contra un H2 real
 * (perfil "test"): registrar bicicleta -> iniciar alquiler -> finalizar ->
 * verificar costo/multa y disponibilidad, además de RN-04 y RN-05.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlquilerFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private AlquilerRepository alquilerRepository;

    @BeforeEach
    void setUp() {
        // Mismos datos de referencia del enunciado (BIC-001 a BIC-005) que usa
        // el DataLoader en desarrollo: así se prueban los flujos exactamente
        // sobre el dataset con el que se evaluará la aplicación.
        alquilerRepository.deleteAll();
        bicicletaRepository.deleteAll();
        bicicletaRepository.saveAll(DatosReferencia.bicicletasDeReferencia());
    }

    @Test
    void flujoCompletoDebeCalcularCostoConMultaYLiberarLaBicicleta() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().minusHours(3).minusMinutes(20);
        String cuerpoIniciar = """
                {"codigoBicicleta":"BIC-002","clienteNombre":"Carlos","duracionEstimadaHoras":2,"horaInicio":"%s"}
                """.formatted(inicio);

        MvcResult resultadoIniciar = mockMvc.perform(post("/api/alquileres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoIniciar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andReturn();

        long alquilerId = objectMapper.readTree(resultadoIniciar.getResponse().getContentAsString()).get("id").asLong();

        // La bicicleta ya no debe figurar como disponible mientras el alquiler está activo.
        mockMvc.perform(get("/api/bicicletas/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='BIC-002')]").isEmpty());

        // Se finaliza sin indicar horaFin explícita: el servidor usa la hora actual,
        // que respecto a "inicio" (hace ~3h20min) reproduce el ejemplo del enunciado.
        mockMvc.perform(put("/api/alquileres/{id}/finalizar", alquilerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.tuvoMulta").value(true))
                .andExpect(jsonPath("$.costoTotal").value(25000));

        // La bicicleta vuelve a estar disponible.
        mockMvc.perform(get("/api/bicicletas/disponibles").param("tipo", "MONTAÑA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='BIC-002')]").isNotEmpty());

        // RF-05: el historial debe reflejar el alquiler ya finalizado.
        mockMvc.perform(get("/api/bicicletas/{codigo}/historial", "BIC-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteNombre").value("Carlos"))
                .andExpect(jsonPath("$[0].tuvoMulta").value(true));

        // RN-05: no se puede finalizar dos veces el mismo alquiler.
        mockMvc.perform(put("/api/alquileres/{id}/finalizar", alquilerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void noDebePermitirIniciarAlquilerSobreBicicletaEnMantenimiento() throws Exception {
        String cuerpo = """
                {"codigoBicicleta":"BIC-004","clienteNombre":"Ana","duracionEstimadaHoras":1}
                """;

        mockMvc.perform(post("/api/alquileres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void debeResponder404AlFinalizarUnAlquilerQueNoExiste() throws Exception {
        mockMvc.perform(put("/api/alquileres/{id}/finalizar", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void debeRechazarRegistroDeBicicletaSinTipo() throws Exception {
        String cuerpo = """
                {"codigo":"BIC-999"}
                """;

        mockMvc.perform(post("/api/bicicletas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRechazarCodigoDeBicicletaDuplicado() throws Exception {
        String cuerpo = """
                {"codigo":"BIC-002","tipo":"URBANA"}
                """;

        mockMvc.perform(post("/api/bicicletas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict());
    }
}
