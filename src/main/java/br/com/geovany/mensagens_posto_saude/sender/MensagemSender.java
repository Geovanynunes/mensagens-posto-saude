package br.com.geovany.mensagens_posto_saude.sender;

public interface MensagemSender {
    ResultadoEnvio enviar(String telefone, String texto);
}
