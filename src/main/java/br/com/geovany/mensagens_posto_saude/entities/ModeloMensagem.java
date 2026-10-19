package br.com.geovany.mensagens_posto_saude.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ModeloMensagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Column(length = 100)
    @Size(min = 1, max = 100)
    private String titulo;

    @NotBlank
    @Column(length = 100)
    @Size(min = 1, max = 1000)
    private String texto;
}
