package br.com.geovany.mensagens_posto_saude.dto;

public record EvolutionQrCode(
        String pairingCode,
        String code,
        String base64
) {
}