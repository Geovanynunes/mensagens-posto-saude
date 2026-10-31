package br.com.geovany.mensagens_posto_saude.services;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import br.com.geovany.mensagens_posto_saude.service.LoginAttemptService;
import br.com.geovany.mensagens_posto_saude.service.UsuarioDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioDetailsServiceTest {

    private UsuarioRepository repository;
    private LoginAttemptService tentativas;
    private UsuarioDetailsService service;

    @BeforeEach
    void configurar() {
        repository = mock(UsuarioRepository.class);
        tentativas = new LoginAttemptService();
        service = new UsuarioDetailsService(repository, tentativas);
    }

    @Test
    void deveCarregarUsuarioCorretamente() {
        Usuario usuario = new Usuario("admin", "senha-criptografada");

        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado = service.loadUserByUsername("admin");

        assertEquals("admin", resultado.getUsername());
        assertEquals("senha-criptografada", resultado.getPassword());
        assertTrue(resultado.isEnabled());
        assertTrue(resultado.isAccountNonLocked());
        assertTrue(resultado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findByUsername("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("inexistente"));
    }

    @Test
    void deveDesabilitarUsuarioInativo() {
        Usuario usuario = new Usuario("admin", "senha-criptografada");
        usuario.setAtivo(false);

        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado = service.loadUserByUsername("admin");

        assertFalse(resultado.isEnabled());
    }

    @Test
    void deveBloquearUsuarioComCincoTentativas() {
        Usuario usuario = new Usuario("admin", "senha-criptografada");

        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(usuario));

        for (int i = 0; i < 5; i++) {
            tentativas.registrarFalha("admin");
        }

        UserDetails resultado = service.loadUserByUsername("admin");

        assertFalse(resultado.isAccountNonLocked());
    }
}