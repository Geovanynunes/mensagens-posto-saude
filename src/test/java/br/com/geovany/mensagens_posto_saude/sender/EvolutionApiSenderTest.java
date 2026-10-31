
package br.com.geovany.mensagens_posto_saude.sender;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EvolutionApiSenderTest {

    private HttpServer servidor;
    private EvolutionApiSender sender;

    @BeforeEach
    void configurar() throws IOException {
        servidor = HttpServer.create(
                new InetSocketAddress("localhost", 0), 0
        );

        servidor.createContext(
                "/message/sendText/Sistema",
                requisicao -> {
                    byte[] corpo = """
                            {"status": "success"}
                            """.getBytes(StandardCharsets.UTF_8);

                    requisicao.sendResponseHeaders(201, corpo.length);
                    requisicao.getResponseBody().write(corpo);
                    requisicao.close();
                }
        );

        servidor.start();

        String url = "http://localhost:"
                + servidor.getAddress().getPort();

        sender = new EvolutionApiSender(
                url,
                "chave-de-teste",
                "Sistema"
        );
    }

    @AfterEach
    void finalizar() {
        if (servidor != null) {
            servidor.stop(0);
        }
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
    void deveIdentificarWhatsAppDesconectado() throws IOException {
        responderComErro(
                500,
                "connection closed"
        );

        ResultadoEnvio resultado = sender.enviar(
                "5514999999999",
                "Mensagem de teste"
        );

        assertFalse(resultado.sucesso());
        assertEquals(
                "WhatsApp desconectado. Conecte novamente o WhatsApp para realizar o envio.",
                resultado.erro()
        );
    }

    @Test
    void deveTratarErroGenericoDaEvolutionApi() throws IOException {
        responderComErro(
                500,
                "Erro interno do servidor"
        );

        ResultadoEnvio resultado = sender.enviar(
                "5514999999999",
                "Mensagem de teste"
        );

        assertFalse(resultado.sucesso());
        assertEquals(
                "Não foi possível enviar a mensagem. Erro na comunicação com o WhatsApp.",
                resultado.erro()
        );
    }

    @Test
    void deveIdentificarEvolutionApiForaDoAr() throws IOException {
        servidor.stop(0);

        ResultadoEnvio resultado = sender.enviar(
                "5514999999999",
                "Mensagem de teste"
        );

        assertFalse(resultado.sucesso());
        assertEquals(
                "Evolution API fora do ar ou sem resposta. Confira a conexão.",
                resultado.erro()
        );
    }

    private void responderComErro(
            int status,
            String mensagem
    ) throws IOException {

        servidor.removeContext("/message/sendText/Sistema");

        servidor.createContext(
                "/message/sendText/Sistema",
                requisicao -> {
                    byte[] corpo = mensagem.getBytes(StandardCharsets.UTF_8);

                    requisicao.sendResponseHeaders(status, corpo.length);
                    requisicao.getResponseBody().write(corpo);
                    requisicao.close();
                }
        );
    }
}
