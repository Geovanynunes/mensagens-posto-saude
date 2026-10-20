package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public void validarEPadronizar(Paciente paciente) {

        if (paciente.getNome() == null
                || paciente.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        String telefone = normalizarTelefone(paciente.getTelefone());

        if (telefone == null) {
            throw new IllegalArgumentException(
                    "Informe um telefone válido com DDD."
            );
        }

        if (pacienteRepository.existsByTelefone(telefone)) {
            throw new IllegalArgumentException(
                    "Já existe um paciente cadastrado com esse telefone."
            );
        }

        paciente.setTelefone(telefone);
    }

    private String normalizarTelefone(String telefoneOriginal) {

        if (telefoneOriginal == null) {
            return null;
        }

        String telefone = telefoneOriginal.replaceAll("\\D", "");

        if (telefone.startsWith("00")) {
            telefone = telefone.substring(2);
        }

        if (telefone.startsWith("55")) {
            telefone = telefone.substring(2);
        }

        if (telefone.length() != 10 && telefone.length() != 11) {
            return null;
        }

        return "55" + telefone;
    }

    @Transactional
    public Paciente salvar(Paciente paciente) {
        validarEPadronizar(paciente);
        return pacienteRepository.save(paciente);
    }
}