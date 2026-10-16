package br.com.geovany.mensagens_posto_saude.sender;

public record ResultadoEnvio(boolean sucesso, String erro) {

    public static ResultadoEnvio ok() {
        return new ResultadoEnvio(true, null);
    }

    public static ResultadoEnvio falha(String erro) {
        return new ResultadoEnvio(false, erro);
    }
}