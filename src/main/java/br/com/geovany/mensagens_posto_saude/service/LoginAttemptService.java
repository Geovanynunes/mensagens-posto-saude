package br.com.geovany.mensagens_posto_saude.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private static class Registro {
        int falhas;
        Instant bloqueadoAte;
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public void registrarFalha(String username) {
        registros.compute(username.toLowerCase(), (chave, registro) -> {
            if (registro == null
                    || (registro.bloqueadoAte != null && Instant.now().isAfter(registro.bloqueadoAte))) {
                registro = new Registro();
            }
            registro.falhas++;
            if (registro.falhas >= MAX_TENTATIVAS) {
                registro.bloqueadoAte = Instant.now().plus(BLOQUEIO);
            }
            return registro;
        });
    }

    public void registrarSucesso(String username) {
        registros.remove(username.toLowerCase());
    }

    public boolean estaBloqueado(String username) {
        Registro registro = registros.get(username.toLowerCase());
        return registro != null
                && registro.bloqueadoAte != null
                && Instant.now().isBefore(registro.bloqueadoAte);
    }
}