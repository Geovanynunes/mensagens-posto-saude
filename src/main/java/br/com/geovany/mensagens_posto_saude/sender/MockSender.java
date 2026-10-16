package br.com.geovany.mensagens_posto_saude.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mensagem.sender", havingValue = "mock", matchIfMissing = true)
public class MockSender implements MensagemSender {

    private static final Logger log = LoggerFactory.getLogger(MockSender.class);

    @Override
    public ResultadoEnvio enviar(String telefone, String texto) {
        // para testar o fluxo de falha: telefones terminados em 0000 "falham"
        if (telefone.endsWith("0000")) {
            return ResultadoEnvio.falha("Número inválido (simulado)");
        }
        log.info("[MOCK] Enviando para {}: {}", telefone, texto);
        return ResultadoEnvio.ok();
    }
}