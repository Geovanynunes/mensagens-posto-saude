package br.com.geovany.mensagens_posto_saude.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
//historico
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String texto;

    private LocalDateTime dataHora;
    private int totalDestinatarios;

    private Integer falhas = 0;

    public int getFalhas() {
        return falhas == null ? 0 : falhas;
    }
    public void registrarFalhas(int falhas) {
        this.falhas = falhas;
    }

    public Envio(String texto, int totalDestinatarios) {
        this.texto = texto;
        this.totalDestinatarios = totalDestinatarios;
        this.dataHora = LocalDateTime.now();
    }

}
