package br.com.geovany.mensagens_posto_saude.repository;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvioRepository extends JpaRepository<Envio, Long> {
    List<Envio> findAllByOrderByDataHoraDesc();
}
