package br.com.geovany.mensagens_posto_saude.dto;

public record EvolutionConnectionState(
        Instance instance
) {
    public record Instance(
            String instanceName,
            String state
    ) {}
}