package br.com.geovany.mensagens_posto_saude.config;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.enums.SituacaoEnvio;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnvioRecuperacao implements CommandLineRunner {

    private final EnvioRepository repository;

    public EnvioRecuperacao(EnvioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        List<Envio> emAndamento = repository.findBySituacao(SituacaoEnvio.ENVIANDO);
        emAndamento.forEach(Envio::interromper);
        repository.saveAll(emAndamento);
    }
}