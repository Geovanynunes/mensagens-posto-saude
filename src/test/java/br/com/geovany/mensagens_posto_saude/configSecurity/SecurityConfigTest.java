package br.com.geovany.mensagens_posto_saude.configSecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "mensagem.sender=mock",
        "evolution.api.key=chave-de-teste"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirAcessoAoLoginSemAutenticacao() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void deveExigirAutenticacaoParaAcessarPacientes() throws Exception {
        mockMvc.perform(get("/pacientes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));    }
    @Test
    void devePermitirAcessoAosPacientesQuandoAutenticado() throws Exception {
        mockMvc.perform(get("/pacientes")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirAcessoAoCssSemAutenticacao() throws Exception {
        mockMvc.perform(get("/css/estilo.css"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRedirecionarParaLoginQuandoCredenciaisForemInvalidas() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "usuario-inexistente")
                        .param("password", "senha-incorreta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}