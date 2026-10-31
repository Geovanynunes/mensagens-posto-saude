package br.com.geovany.mensagens_posto_saude.services;

import br.com.geovany.mensagens_posto_saude.service.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void configurar() {
        service = new LoginAttemptService();
    }

    @Test
    void naoDeveBloquearAntesDeCincoTentativas() {
        for (int i = 0; i < 4; i++) {
            service.registrarFalha("admin");
        }

        assertFalse(service.estaBloqueado("admin"));
    }

    @Test
    void deveBloquearAposCincoTentativas() {
        for (int i = 0; i < 5; i++) {
            service.registrarFalha("admin");
        }

        assertTrue(service.estaBloqueado("admin"));
    }

    @Test
    void deveIgnorarDiferencaEntreMaiusculasEMinusculas() {
        for (int i = 0; i < 5; i++) {
            service.registrarFalha("Admin");
        }

        assertTrue(service.estaBloqueado("ADMIN"));
    }

    @Test
    void deveRemoverBloqueioAposLoginBemSucedido() {
        for (int i = 0; i < 5; i++) {
            service.registrarFalha("admin");
        }

        service.registrarSucesso("admin");

        assertFalse(service.estaBloqueado("admin"));
    }
}