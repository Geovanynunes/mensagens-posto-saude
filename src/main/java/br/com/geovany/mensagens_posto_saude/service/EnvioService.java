package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.EnvioDestinatario;
import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.sender.MensagemSender;
import br.com.geovany.mensagens_posto_saude.sender.ResultadoEnvio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

public class EnvioService {

    private final PacienteRepository pacienteRepository;
    private final EnvioRepository envioRepository;
    private final MensagemSender sender;
    private final EnvioDestinatarioRepository destinatarioRepository;
    private final long intervaloMs;

    public EnvioService(PacienteRepository pacienteRepository,
                        EnvioRepository envioRepository,
                        EnvioDestinatarioRepository destinatarioRepository,
                        MensagemSender sender,
                        @Value("${mensagem.intervalo-ms:0}") long intervaloMs) {
        this.pacienteRepository = pacienteRepository;
        this.envioRepository = envioRepository;
        this.destinatarioRepository = destinatarioRepository;
        this.sender = sender;
        this.intervaloMs = intervaloMs;

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
        }
    }

    public Envio enviarParaSelecionados(List<Long> ids, String texto) {
        List<Paciente> pacientes = pacienteRepository.findAllById(ids).stream()
                .filter(Paciente::isAtivo)
                .toList();

        Envio envio = envioRepository.save(new Envio(texto, pacientes.size()));

        List<EnvioDestinatario> destinatarios = new ArrayList<>();
        int falhas = 0;

        for (Paciente p : pacientes) {
            String textoFinal = texto.replace("{nome}", primeiroNome(p.getNome()));

            ResultadoEnvio resultado;
            try {
                resultado = sender.enviar(p.getTelefone(), textoFinal);
            } catch (Exception e) {
                resultado = ResultadoEnvio.falha(e.getMessage());
            }
            if (!resultado.sucesso()) falhas++;
            destinatarios.add(new EnvioDestinatario(envio, p, resultado));

            pausar();
        }

        destinatarioRepository.saveAll(destinatarios);
        envio.registrarFalhas(falhas);
        return envioRepository.save(envio);
    }
}

