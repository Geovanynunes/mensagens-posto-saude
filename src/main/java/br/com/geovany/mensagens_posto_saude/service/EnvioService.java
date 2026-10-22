package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.EnvioDestinatario;
import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvioService {

    private final PacienteRepository pacienteRepository;
    private final EnvioRepository envioRepository;
    private final EnvioDestinatarioRepository destinatarioRepository;
    private final EnvioProcessor processor;

    public EnvioService(PacienteRepository pacienteRepository,
                        EnvioRepository envioRepository,
                        EnvioDestinatarioRepository destinatarioRepository,
                        EnvioProcessor processor) {
        this.pacienteRepository = pacienteRepository;
        this.envioRepository = envioRepository;
        this.destinatarioRepository = destinatarioRepository;
        this.processor = processor;
    }

    // Sem @Transactional de propósito: tudo precisa estar gravado no banco
    // antes de o processador começar a ler em outra thread.
    public Envio iniciar(List<Long> ids, String texto) {
        List<Paciente> pacientes = pacienteRepository.findAllById(ids).stream()
                .filter(Paciente::isAtivo)
                .toList();

        if (pacientes.isEmpty()) {
            throw new IllegalArgumentException("Nenhum paciente ativo entre os selecionados.");
        }

        Envio envio = envioRepository.save(new Envio(texto, pacientes.size()));

        List<EnvioDestinatario> destinatarios = pacientes.stream()
                .map(p -> new EnvioDestinatario(envio, p))
                .toList();
        destinatarioRepository.saveAll(destinatarios);

        try {
            processor.processar(envio.getId());
        } catch (TaskRejectedException e) {
            envio.interromper();
            envioRepository.save(envio);
            throw new IllegalStateException("Há muitos envios na fila. Tente novamente em instantes.");
        }
        return envio;
    }
}