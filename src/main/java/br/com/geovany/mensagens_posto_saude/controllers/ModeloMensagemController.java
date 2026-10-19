package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.ModeloMensagem;
import br.com.geovany.mensagens_posto_saude.repository.ModeloMensagemRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/modelos")
public class ModeloMensagemController {

    private final ModeloMensagemRepository repository;

    public ModeloMensagemController(ModeloMensagemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("modelos", repository.findAllByOrderByTituloAsc());
        model.addAttribute("modelo", new ModeloMensagem());
        return "modelos";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("modelo") ModeloMensagem modelo,
                        BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("modelos", repository.findAllByOrderByTituloAsc());
            return "modelos";
        }
        repository.save(modelo);
        return "redirect:/modelos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("modelo", buscar(id));
        model.addAttribute("id", id);
        return "modelo-editar";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("modelo") ModeloMensagem form,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            return "modelo-editar";
        }
        ModeloMensagem existente = buscar(id);
        existente.setTitulo(form.getTitulo());
        existente.setTexto(form.getTexto());
        repository.save(existente);
        return "redirect:/modelos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/modelos";
    }

    private ModeloMensagem buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
