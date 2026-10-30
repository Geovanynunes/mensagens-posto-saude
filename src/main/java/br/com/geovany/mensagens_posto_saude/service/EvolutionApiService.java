package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.dto.EvolutionConnectionState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import br.com.geovany.mensagens_posto_saude.dto.EvolutionQrCode;

@Service
public class EvolutionApiService {

    private final RestClient restClient;
    private final String instance;

    public EvolutionApiService(
            @Value("${evolution.api.url}") String baseUrl,
            @Value("${evolution.api.key}") String apiKey,
            @Value("${evolution.api.instance}") String instance) {

        this.instance = instance;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .build();
    }

    public EvolutionQrCode gerarQrCode() {

        return restClient.get()
                .uri("/instance/connect/{instance}", instance)
                .retrieve()
                .body(EvolutionQrCode.class);
    }

    public EvolutionConnectionState consultarEstadoConexao() {
        return restClient.get()
                .uri("/instance/connectionState/{instance}", instance)
                .retrieve()
                .body(EvolutionConnectionState.class);
    }

    public void desconectarWhatsApp() {
        restClient.delete()
                .uri("/instance/logout/{instance}", instance)
                .retrieve()
                .toBodilessEntity();
    }
}