package br.com.geovany.mensagens_posto_saude.service;

import org.springframework.stereotype.Service;


public class TelefoneDuplicadoException extends IllegalArgumentException {
    public TelefoneDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
