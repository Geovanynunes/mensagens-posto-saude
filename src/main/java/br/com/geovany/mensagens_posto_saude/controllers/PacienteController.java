package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {
    private final PacienteRepository pacienteRepository;
    private final PacienteService pacienteService;

    public PacienteController(PacienteRepository pacienteRepository, PacienteService pacienteService) {
        this.pacienteRepository = pacienteRepository;
        this.pacienteService = pacienteService;


    }
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("paciente", new Paciente());
        return "pacientes";
    }

    @PostMapping
    public String salvar(
            @Valid @ModelAttribute Paciente paciente,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteRepository.findAll());
            return "pacientes";
        }

        try {
            pacienteService.salvar(paciente);
            return "redirect:/pacientes";

        } catch (IllegalArgumentException e) {
            result.rejectValue("telefone", "telefone.invalido", e.getMessage());

            model.addAttribute("pacientes", pacienteRepository.findAll());
            return "pacientes";
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        pacienteRepository.deleteById(id);
        return "redirect:/pacientes";
    }
}
