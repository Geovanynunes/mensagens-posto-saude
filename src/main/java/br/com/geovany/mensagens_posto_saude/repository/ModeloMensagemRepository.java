package br.com.geovany.mensagens_posto_saude.repository;

import br.com.geovany.mensagens_posto_saude.entities.ModeloMensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModeloMensagemRepository extends JpaRepository<ModeloMensagem, Long> {
        List<ModeloMensagem> findAllByOrderByTituloAsc();
}
