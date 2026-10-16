package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.sender.MensagemSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvioService {

    private final PacienteRepository pacienteRepository;
    private final EnvioRepository envioRepository;
    private final MensagemSender sender;

    public EnvioService(PacienteRepository pacienteRepository,
                        EnvioRepository envioRepository,
                        MensagemSender sender) {
        this.pacienteRepository = pacienteRepository;
        this.envioRepository = envioRepository;
        this.sender = sender;
    }

    public int enviarParaSelecionados(List<Long> ids, String texto) {
        List<Paciente> pacientes = pacienteRepository.findAllById(ids).stream()
                .filter(Paciente::isAtivo)
                .toList();

        pacientes.forEach(p -> sender.enviar(p.getTelefone(), texto));
        envioRepository.save(new Envio(texto, pacientes.size()));
        return pacientes.size();
    }
}
