package br.com.geovany.mensagens_posto_saude.entities;

import br.com.geovany.mensagens_posto_saude.enums.SituacaoEnvio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String texto;

    private LocalDateTime dataHora;
    private int totalDestinatarios;

    private Integer falhas = 0;
    private Integer processados;

    @Enumerated(EnumType.STRING)
    private SituacaoEnvio situacao;

    public Envio(String texto, int totalDestinatarios) {
        this.texto = texto;
        this.totalDestinatarios = totalDestinatarios;
        this.dataHora = LocalDateTime.now();
        this.situacao = SituacaoEnvio.ENVIANDO;
        this.processados = 0;
        this.falhas = 0;
    }


    public int getFalhas() { return falhas == null ? 0 : falhas; }
    public int getProcessados() { return processados == null ? totalDestinatarios : processados; }
    public SituacaoEnvio getSituacao() { return situacao == null ? SituacaoEnvio.CONCLUIDO : situacao; }

    public void registrarProgresso(int processados, int falhas) {
        this.processados = processados;
        this.falhas = falhas;
    }

    public void concluir() { this.situacao = SituacaoEnvio.CONCLUIDO; }
    public void interromper() { this.situacao = SituacaoEnvio.INTERROMPIDO; }
}