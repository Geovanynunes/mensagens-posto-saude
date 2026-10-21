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
            throw new TelefoneDuplicadoException("Já existe um paciente cadastrado com esse telefone.");
        }

        paciente.setTelefone(telefone);
    }

    private String normalizarTelefone(String original) {
        if (original == null) {
            return null;
        }

        String digitos = original.replaceAll("\\D", "").replaceFirst("^0+", "");

        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = "55" + digitos;
        }

        if (!digitos.startsWith("55") || (digitos.length() != 12 && digitos.length() != 13)) {
            return null;
        }

        int ddd = Integer.parseInt(digitos.substring(2, 4));
        if (ddd < 11) {
            return null;
        }

        return digitos;
    }

    @Transactional
    public Paciente salvar(Paciente paciente) {
        validarEPadronizar(paciente);
        return pacienteRepository.save(paciente);
    }
}