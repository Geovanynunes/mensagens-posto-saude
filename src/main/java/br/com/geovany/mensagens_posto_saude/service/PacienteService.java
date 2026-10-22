package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    private static final Set<String> PARTICULAS = Set.of("da", "de", "di", "do", "du", "das", "dos", "e");

    private String padronizarNome(String nome) {
        String[] palavras = nome.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < palavras.length; i++) {
            String palavra = palavras[i];
            if (i > 0 && PARTICULAS.contains(palavra)) {
                resultado.append(palavra);
            } else {
                resultado.append(Character.toUpperCase(palavra.charAt(0))).append(palavra.substring(1));
            }
            if (i < palavras.length - 1) resultado.append(' ');
        }
        return resultado.toString();
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

        paciente.setNome(padronizarNome(paciente.getNome()));
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