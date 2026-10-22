package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.EnvioDestinatario;
import br.com.geovany.mensagens_posto_saude.enums.StatusEnvio;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.sender.MensagemSender;
import br.com.geovany.mensagens_posto_saude.sender.ResultadoEnvio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnvioProcessor {

    private static final Logger log = LoggerFactory.getLogger(EnvioProcessor.class);

    private final EnvioRepository envioRepository;
    private final EnvioDestinatarioRepository destinatarioRepository;
    private final MensagemSender sender;
    private final long intervaloMs;

    public EnvioProcessor(EnvioRepository envioRepository,
                          EnvioDestinatarioRepository destinatarioRepository,
                          MensagemSender sender,
                          @Value("${mensagem.intervalo-ms:0}") long intervaloMs) {
        this.envioRepository = envioRepository;
        this.destinatarioRepository = destinatarioRepository;
        this.sender = sender;
        this.intervaloMs = intervaloMs;
    }

    @Async("envioExecutor")
    public void processar(Long envioId) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null) return;

        try {
            List<EnvioDestinatario> pendentes = destinatarioRepository
                    .findByEnvioIdAndStatusOrderByIdAsc(envioId, StatusEnvio.PENDENTE);

            int processados = 0;
            int falhas = 0;

            for (int i = 0; i < pendentes.size(); i++) {
                EnvioDestinatario d = pendentes.get(i);
                String textoFinal = envio.getTexto().replace("{nome}", primeiroNome(d.getNome()));

                ResultadoEnvio resultado;
                try {
                    resultado = sender.enviar(d.getTelefone(), textoFinal);
                } catch (Exception e) {
                    resultado = ResultadoEnvio.falha(e.getMessage());
                }

                d.registrarResultado(resultado);
                destinatarioRepository.save(d);

                processados++;
                if (!resultado.sucesso()) falhas++;
                envio.registrarProgresso(processados, falhas);
                envioRepository.save(envio);

                if (i < pendentes.size() - 1) pausar();
            }
            envio.concluir();
        } catch (Exception e) {
            log.warn("Envio {} interrompido: {}", envioId, e.getMessage());
            envio.interromper();
        }
        envioRepository.save(envio);
    }

    private String primeiroNome(String nome) {
        String primeiro = nome.trim().split("\\s+")[0].toLowerCase();
        if (primeiro.isEmpty()) return "";
        return primeiro.substring(0, 1).toUpperCase() + primeiro.substring(1);
    }

    private void pausar() {
        if (intervaloMs <= 0) return;
        try {
            Thread.sleep(intervaloMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envio interrompido");
        }
    }
}