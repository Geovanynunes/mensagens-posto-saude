package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.EnvioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mensagens")
public class EnvioController {

    private final EnvioService service;
    private final EnvioRepository repository;
    private final PacienteRepository pacienteRepository;

    public EnvioController(EnvioService service, EnvioRepository repository, PacienteRepository pacienteRepository) {
        this.service = service;
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
    }

    @GetMapping
    public String tela(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findByAtivoTrueOrderByNomeAsc());
        model.addAttribute("envios", repository.findAllByOrderByDataHoraDesc());
        return "mensagens";
    }

    @PostMapping
    public String enviar(@RequestParam String texto,
                         @RequestParam(required = false) List<Long> ids,
                         RedirectAttributes redirect) {
        if (texto.isBlank()) {
            redirect.addFlashAttribute("erro", "Escreva a mensagem.");
        } else if (ids == null || ids.isEmpty()) {
            redirect.addFlashAttribute("erro", "Selecione pelo menos um paciente.");
        } else {
            int total = service.enviarParaSelecionados(ids, texto.trim());
            redirect.addFlashAttribute("mensagem", "Mensagem enviada para " + total + " pacientes.");
        }
        return "redirect:/mensagens";
    }
}