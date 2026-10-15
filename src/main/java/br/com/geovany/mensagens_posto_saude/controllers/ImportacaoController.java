package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pacientes/importar")
public class ImportacaoController {

    private final PacienteRepository pacienteRepository;

    public ImportacaoController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @PostMapping
    public String importar(@RequestParam("arquivo") MultipartFile arquivo,
                           RedirectAttributes redirect) throws IOException {
        List<Paciente> novos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // pula o cabeçalho
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.isBlank()) continue;
                String[] colunas = linha.split("[;,]");
                if (colunas.length < 2) continue;

                Paciente p = new Paciente();
                p.setNome(colunas[0].trim());
                p.setTelefone(colunas[1].replaceAll("\\D", "")); // só dígitos
                novos.add(p);
            }
        }

        pacienteRepository.saveAll(novos);
        redirect.addFlashAttribute("mensagem", novos.size() + " pacientes importados.");
        return "redirect:/pacientes";
    }
}