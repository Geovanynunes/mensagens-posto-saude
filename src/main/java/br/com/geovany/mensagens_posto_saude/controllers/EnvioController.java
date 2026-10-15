package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.service.EnvioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mensagens")
public class EnvioController {

    private final EnvioService service;
    private final EnvioRepository repository;

    public EnvioController(EnvioService service, EnvioRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public String tela(Model model) {
        model.addAttribute("envios", repository.findAllByOrderByDataHoraDesc());
        return "mensagens";
    }

    @PostMapping
    public String enviar(@RequestParam String texto, RedirectAttributes redirect) {
        if (texto.isBlank()) {
            redirect.addFlashAttribute("erro", "Escreva a mensagem.");
        } else {
            int total = service.enviarParaTodos(texto.trim());
            redirect.addFlashAttribute("mensagem", "Mensagem enviada para " + total + " pacientes.");
        }
        return "redirect:/mensagens";
    }
}