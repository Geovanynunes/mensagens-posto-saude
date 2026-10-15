package br.com.geovany.mensagens_posto_saude.sender;

public interface MensagemSender {
    void enviar(String telefone, String texto);
}
