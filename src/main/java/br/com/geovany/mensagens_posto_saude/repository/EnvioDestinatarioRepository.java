package br.com.geovany.mensagens_posto_saude.repository;

import br.com.geovany.mensagens_posto_saude.entities.EnvioDestinatario;
import br.com.geovany.mensagens_posto_saude.enums.StatusEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvioDestinatarioRepository extends JpaRepository<EnvioDestinatario, Integer> {
    List<EnvioDestinatario> findByEnvioIdOrderByNomeAsc(Long envioId);
    List<EnvioDestinatario> findByEnvioIdAndStatusOrderByIdAsc(Long envioId, StatusEnvio status);

}
