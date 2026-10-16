package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.EnvioDestinatario;
import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.sender.MensagemSender;
import br.com.geovany.mensagens_posto_saude.sender.ResultadoEnvio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnvioService {

    private final PacienteRepository pacienteRepository;
    private final EnvioRepository envioRepository;
    private final MensagemSender sender;
    private final EnvioDestinatarioRepository destinatarioRepository;

    public EnvioService(PacienteRepository pacienteRepository,
                        EnvioRepository envioRepository,
                        EnvioDestinatarioRepository destinatarioRepository,
                        MensagemSender sender) {
        this.pacienteRepository = pacienteRepository;
        this.envioRepository = envioRepository;
        this.destinatarioRepository = destinatarioRepository;
        this.sender = sender;
    }

    public Envio enviarParaSelecionados(List<Long> ids, String texto) {
        List<Paciente> pacientes = pacienteRepository.findAllById(ids).stream()
                .filter(Paciente::isAtivo)
                .toList();

        Envio envio = envioRepository.save(new Envio(texto, pacientes.size()));

        List<EnvioDestinatario> destinatarios = new ArrayList<>();
        int falhas = 0;

        for (Paciente p : pacientes) {
            ResultadoEnvio resultado;
            try {
                resultado = sender.enviar(p.getTelefone(), texto);
            } catch (Exception e) {
                resultado = ResultadoEnvio.falha(e.getMessage());
            }
            if (!resultado.sucesso()) falhas++;
            destinatarios.add(new EnvioDestinatario(envio, p, resultado));
        }

        destinatarioRepository.saveAll(destinatarios);
        envio.registrarFalhas(falhas);
        return envioRepository.save(envio);
    }
}

