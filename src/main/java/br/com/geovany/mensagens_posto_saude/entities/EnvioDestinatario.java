package br.com.geovany.mensagens_posto_saude.entities;

import br.com.geovany.mensagens_posto_saude.enums.StatusEnvio;
import br.com.geovany.mensagens_posto_saude.sender.ResultadoEnvio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class EnvioDestinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Envio envio;

    private String nome;
    private String telefone;

    @Enumerated(EnumType.STRING)
    private StatusEnvio status;

    @Column(length = 500)
    private String erro;

    protected EnvioDestinatario() {
    }

    public EnvioDestinatario(Envio envio, Paciente paciente, ResultadoEnvio resultado) {
        this.envio = envio;
        this.nome = paciente.getNome();
        this.telefone = paciente.getTelefone();
        this.status = resultado.sucesso() ? StatusEnvio.ENVIADO : StatusEnvio.FALHOU;
        String msg = resultado.erro();
        this.erro = (msg != null && msg.length() > 500) ? msg.substring(0, 500) : msg;
    }
}