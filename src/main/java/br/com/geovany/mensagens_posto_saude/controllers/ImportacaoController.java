
package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.PacienteService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/pacientes/importar")
public class ImportacaoController {

    private final PacienteRepository pacienteRepository;
    private final PacienteService pacienteService;

    public ImportacaoController(
            PacienteRepository pacienteRepository,
            PacienteService pacienteService) {
        this.pacienteRepository = pacienteRepository;
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public String importar(
            @RequestParam("arquivo") MultipartFile arquivo,
            RedirectAttributes redirect) throws IOException {

        List<Paciente> novos = new ArrayList<>();
        Set<String> telefonesDoArquivo = new HashSet<>();

        int duplicados = 0;
        int invalidos = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        arquivo.getInputStream(),
                        StandardCharsets.UTF_8))) {

            reader.readLine(); // Pula o cabeçalho

            String linha;

            while ((linha = reader.readLine()) != null) {

                if (linha.isBlank()) {
                    continue;
                }

                String[] colunas = linha.split("[;,]", -1);

                if (colunas.length < 2) {
                    invalidos++;
                    continue;
                }

                String nome = colunas[0].trim();

                Paciente paciente = new Paciente();
                paciente.setNome(nome);
                paciente.setTelefone(colunas[1]);

                try {
                    pacienteService.validarEPadronizar(paciente);

                } catch (IllegalArgumentException e) {

                    if (e.getMessage() != null
                            && e.getMessage().contains("Já existe")) {
                        duplicados++;
                    } else {
                        invalidos++;
                    }

                    continue;
                }

                String telefone = paciente.getTelefone();

                // Verifica duplicados dentro do próprio CSV
                if (!telefonesDoArquivo.add(telefone)) {
                    duplicados++;
                    continue;
                }

                novos.add(paciente);
            }
        }

        // Salva todos os pacientes válidos de uma vez
        pacienteRepository.saveAll(novos);

        redirect.addFlashAttribute(
                "mensagem",
                novos.size() + " pacientes importados. "
                        + duplicados + " duplicados ignorados. "
                        + invalidos + " registros inválidos ignorados."
        );

        return "redirect:/pacientes";
    }
}
