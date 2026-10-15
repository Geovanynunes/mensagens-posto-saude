package br.com.geovany.mensagens_posto_saude.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockSender implements MensagemSender {

    private static final Logger log = LoggerFactory.getLogger(MockSender.class);

    @Override
    public void enviar(String telefone, String texto) {
        log.info("[MOCK] Enviando para {}: {}", telefone, texto);
    }
}