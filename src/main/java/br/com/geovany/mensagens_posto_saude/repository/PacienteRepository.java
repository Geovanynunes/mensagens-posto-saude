package br.com.geovany.mensagens_posto_saude.repository;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
