package br.com.geovany.mensagens_posto_saude.config;

import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import br.com.geovany.mensagens_posto_saude.service.LoginAttemptService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginEventos {

    private final LoginAttemptService tentativas;
    private final UsuarioRepository usuarioRepository;


    public LoginEventos(LoginAttemptService tentativas, UsuarioRepository usuarioRepository) {
        this.tentativas = tentativas;
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener
    public void aoFalhar(AuthenticationFailureBadCredentialsEvent evento) {
        String username = evento.getAuthentication().getName();

        if (username != null && usuarioRepository.findByUsername(username).isPresent()) {
            tentativas.registrarFalha(username);
        }
    }

    @EventListener
    public void aoEntrar(AuthenticationSuccessEvent evento) {
        String username = evento.getAuthentication().getName();
        if (username != null) {
            tentativas.registrarSucesso(username);
        }
    }
}