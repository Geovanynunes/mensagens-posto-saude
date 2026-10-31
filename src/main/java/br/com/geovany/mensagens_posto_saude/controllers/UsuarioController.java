package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/senha")
    public String exibirAlteracaoSenha() {
        return "alterar-senha";
    }

    @PostMapping("/senha")
    public String alterarSenha(
            Authentication authentication,
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha,
            @RequestParam String confirmarSenha,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            redirectAttributes.addFlashAttribute(
                    "erro", "A senha atual está incorreta.");
            return "redirect:/usuario/senha";
        }

        if (novaSenha.length() < 8) {
            redirectAttributes.addFlashAttribute(
                    "erro", "A nova senha deve ter pelo menos 8 caracteres.");
            return "redirect:/usuario/senha";
        }

        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute(
                    "erro", "A confirmação da nova senha não corresponde.");
            return "redirect:/usuario/senha";
        }

        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            redirectAttributes.addFlashAttribute(
                    "erro", "A nova senha deve ser diferente da senha atual.");
            return "redirect:/usuario/senha";
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "mensagem", "Senha alterada com sucesso!");

        return "redirect:/usuario/senha";
    }
}