package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {
    private final PacienteRepository pacienteRepository;

    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;

    }
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("paciente", new Paciente());
        return "pacientes";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Paciente paciente,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteRepository.findAll());
            return "pacientes";
        }
        pacienteRepository.save(paciente);
        return "redirect:/pacientes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        pacienteRepository.deleteById(id);
        return "redirect:/pacientes";
    }
}
