package br.com.geovany.mensagens_posto_saude.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome.")
    private String nome;

    @NotBlank(message = "Informe o telefone.")
    @Column(unique = true)
    private String telefone;

    private boolean ativo = true;

}
