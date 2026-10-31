package br.com.geovany.mensagens_posto_saude.repository;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}