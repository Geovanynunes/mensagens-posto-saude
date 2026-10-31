
package br.com.geovany.mensagens_posto_saude.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockSenderTest {

    private MockSender sender;

    @BeforeEach
    void configurar() {
        sender = new MockSender();
    }

    @Test
    void deveEnviarMensagemComSucesso() {
        ResultadoEnvio resultado = sender.enviar(
                "5514999999999",
                "Mensagem de teste"
        );

        assertTrue(resultado.sucesso());
        assertNull(resultado.erro());
    }

    @Test
    void deveFalharQuandoTelefoneTerminarEm0000() {
        ResultadoEnvio resultado = sender.enviar(
                "5514999990000",
                "Mensagem de teste"
        );

        assertFalse(resultado.sucesso());
        assertEquals(
                "Número inválido (simulado)",
                resultado.erro()
        );
    }
}
