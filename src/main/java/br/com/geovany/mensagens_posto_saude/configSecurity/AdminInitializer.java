package br.com.geovany.mensagens_posto_saude.configSecurity;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final String usuario;
    private final String senha;

    public AdminInitializer(UsuarioRepository repository,
                            PasswordEncoder encoder,
                            @Value("${admin.usuario:}") String usuario,
                            @Value("${admin.senha:}") String senha) {
        this.repository = repository;
        this.encoder = encoder;
        this.usuario = usuario;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        if (usuario.isBlank() || senha.isBlank()) {
            log.warn("Nenhum usuário cadastrado e admin.usuario/admin.senha não definidos: ninguém conseguirá entrar.");
            return;
        }

        repository.save(new Usuario(usuario.trim(), encoder.encode(senha)));
        log.info("Usuário administrador '{}' criado.", usuario);
    }
}