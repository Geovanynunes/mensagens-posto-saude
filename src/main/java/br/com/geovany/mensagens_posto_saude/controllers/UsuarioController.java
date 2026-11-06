
package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;

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
            redirectAttributes.addFlashAttribute("erro", "A senha atual está incorreta.");
            return "redirect:/usuario/senha";
        }

        if (novaSenha.length() < 8) {
            redirectAttributes.addFlashAttribute("erro", "A nova senha deve ter pelo menos 8 caracteres.");
            return "redirect:/usuario/senha";
        }

        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("erro", "A confirmação da nova senha não corresponde.");
            return "redirect:/usuario/senha";
        }

        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            redirectAttributes.addFlashAttribute("erro", "A nova senha deve ser diferente da senha atual.");
            return "redirect:/usuario/senha";
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensagem", "Senha alterada com sucesso!");

        return "redirect:/usuario/senha";
    }

    @PostMapping("/nome")
    public String alterarNome(
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam String senhaAtual,
            @RequestParam String novoNome,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        novoNome = novoNome.trim();

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            redirectAttributes.addFlashAttribute(
                    "erro", "A senha atual está incorreta.");
            return "redirect:/usuario/senha";
        }

        if (novoNome.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "erro", "O nome de usuário não pode ficar vazio.");
            return "redirect:/usuario/senha";
        }

        if (novoNome.length() > 50) {
            redirectAttributes.addFlashAttribute(
                    "erro", "O nome de usuário deve ter no máximo 50 caracteres.");
            return "redirect:/usuario/senha";
        }

        if (novoNome.equals(usuario.getUsername())) {
            redirectAttributes.addFlashAttribute(
                    "erro", "O novo nome deve ser diferente do atual.");
            return "redirect:/usuario/senha";
        }

        if (usuarioRepository.findByUsername(novoNome).isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Esse nome de usuário já está em uso.");
            return "redirect:/usuario/senha";
        }

        usuario.setUsername(novoNome);
        usuarioRepository.save(usuario);

        UsernamePasswordAuthenticationToken novaAutenticacao =
                UsernamePasswordAuthenticationToken.authenticated(
                        usuario.getUsername(),
                        null,
                        authentication.getAuthorities()
                );

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(novaAutenticacao);
        SecurityContextHolder.setContext(contexto);

        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                contexto
        );

        redirectAttributes.addFlashAttribute(
                "mensagem", "Nome de usuário alterado com sucesso!");

        return "redirect:/usuario/senha";
    }
}
