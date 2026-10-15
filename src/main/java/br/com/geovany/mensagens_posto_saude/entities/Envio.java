package br.com.geovany.mensagens_posto_saude.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
//historico
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String texto;

    private LocalDateTime dataHora;
    private int totalDestinatarios;

    protected Envio() {}

    public Envio(String texto, int totalDestinatarios) {
        this.texto = texto;
        this.totalDestinatarios = totalDestinatarios;
        this.dataHora = LocalDateTime.now();
    }

}
