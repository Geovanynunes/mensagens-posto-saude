package br.com.geovany.mensagens_posto_saude.repository;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    //Os ativos em ordem alfabetica
    List<Paciente> findByAtivoTrueOrderByNomeAsc();
    boolean existsByTelefone(String telefone);
}
