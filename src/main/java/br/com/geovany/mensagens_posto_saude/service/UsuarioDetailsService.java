package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final LoginAttemptService tentativas;

    public UsuarioDetailsService(UsuarioRepository repository, LoginAttemptService tentativas) {
        this.repository = repository;
        this.tentativas = tentativas;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(u.getUsername())
                .password(u.getSenha())
                .roles("ADMIN")
                .disabled(!u.isAtivo())
                .accountLocked(tentativas.estaBloqueado(u.getUsername()))
                .build();
    }
}