package br.com.geovany.mensagens_posto_saude.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "mensagem.sender", havingValue = "evolution")
public class EvolutionApiSender implements MensagemSender {

    private final RestClient restClient;
    private final String instance;

    public EvolutionApiSender(
            @Value("${evolution.api.url:http://localhost:8081}") String baseUrl,
            @Value("${evolution.api.key}") String apiKey,
            @Value("${evolution.api.instance:Sistema}") String instance) {

        this.instance = instance;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .build();
    }

    @Override
    public ResultadoEnvio enviar(String telefone, String texto) {
        try {
            restClient.post()
                    .uri("/message/sendText/{instance}", instance)
                    .body(Map.of("number", telefone, "text", texto))
                    .retrieve()
                    .toBodilessEntity();
            return ResultadoEnvio.ok();

        } catch (RestClientResponseException e) {
            String corpo = e.getResponseBodyAsString();

            if (corpo.toLowerCase().contains("connection closed")) {
                return ResultadoEnvio.falha(
                        "WhatsApp desconectado. Conecte novamente o WhatsApp para realizar o envio."
                );
            }

            return ResultadoEnvio.falha(
                    "Não foi possível enviar a mensagem. Erro na comunicação com o WhatsApp."
            );

        } catch (ResourceAccessException e) {
            return ResultadoEnvio.falha(
                    "Evolution API fora do ar ou sem resposta. Confira a conexão."
            );

        } catch (Exception e) {
            return ResultadoEnvio.falha(
                    "Erro inesperado: " + e.getClass().getSimpleName()
            );
            
        }
    }
}