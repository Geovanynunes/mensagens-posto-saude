package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.ModeloMensagemRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.EnvioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mensagens")
public class EnvioController {

    private final EnvioService service;
    private final EnvioRepository repository;
    private final PacienteRepository pacienteRepository;
    private final EnvioDestinatarioRepository envioDestinatarioRepository;
    private final ModeloMensagemRepository modeloMensagemRepository;

    public EnvioController(EnvioService service, EnvioRepository repository, PacienteRepository pacienteRepository, EnvioDestinatarioRepository envioDestinatarioRepository, ModeloMensagemRepository modeloMensagemRepository) {
        this.service = service;
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.envioDestinatarioRepository = envioDestinatarioRepository;
        this.modeloMensagemRepository = modeloMensagemRepository;
    }

    @GetMapping
    public String tela(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findByAtivoTrueOrderByNomeAsc());
        model.addAttribute("envios", repository.findAllByOrderByDataHoraDesc());
        model.addAttribute("modelos", modeloMensagemRepository.findAllByOrderByTituloAsc());
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
            Envio envio = service.enviarParaSelecionados(ids, texto.trim());
            int falhas = envio.getFalhas();
            int ok = envio.getTotalDestinatarios() - falhas;
            redirect.addFlashAttribute("mensagem", ok + " enviadas.");
            if (falhas > 0) {
                redirect.addFlashAttribute("erro", falhas + " falharam. Veja os detalhes no histórico.");
            }
        }
        return "redirect:/mensagens";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Envio envio = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("envio", envio);
        model.addAttribute("destinatarios", envioDestinatarioRepository.findByEnvioIdOrderByNomeAsc(id));
        return "envio";
    }
}