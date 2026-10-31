package br.com.geovany.mensagens_posto_saude;

import br.com.geovany.mensagens_posto_saude.entities.Usuario;
import br.com.geovany.mensagens_posto_saude.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "evolution.api.key=chave-de-teste",
        "mensagem.sender=mock"
})
class AutenticacaoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveRealizarLoginComCredenciaisCorretas() throws Exception {

        Usuario usuario = new Usuario(
                "teste_integracao",
                passwordEncoder.encode("SenhaTeste123")
        );

        usuarioRepository.save(usuario);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "teste_integracao")
                        .param("password", "SenhaTeste123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pacientes"));
    }

    @Test
    void naoDeveRealizarLoginComSenhaIncorreta() throws Exception {

        Usuario usuario = new Usuario(
                "teste_integracao",
                passwordEncoder.encode("SenhaTeste123")
        );

        usuarioRepository.save(usuario);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "teste_integracao")
                        .param("password", "SenhaErrada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}