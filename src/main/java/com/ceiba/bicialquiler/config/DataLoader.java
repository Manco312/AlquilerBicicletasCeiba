package com.ceiba.bicialquiler.config;

import com.ceiba.bicialquiler.repository.BicicletaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final BicicletaRepository bicicletaRepository;

    public DataLoader(BicicletaRepository bicicletaRepository) {
        this.bicicletaRepository = bicicletaRepository;
    }

    @Override
    public void run(String... args) {
        if (bicicletaRepository.count() > 0) {
            return;
        }
        bicicletaRepository.saveAll(DatosReferencia.bicicletasDeReferencia());
    }
}
