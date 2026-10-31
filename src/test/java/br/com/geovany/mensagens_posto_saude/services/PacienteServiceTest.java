package br.com.geovany.mensagens_posto_saude.services;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.PacienteService;
import br.com.geovany.mensagens_posto_saude.service.TelefoneDuplicadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PacienteServiceTest {

    private PacienteRepository pacienteRepository;
    private PacienteService pacienteService;

    @BeforeEach
    void configurar() {
        pacienteRepository = mock(PacienteRepository.class);
        pacienteService = new PacienteService(pacienteRepository);
    }

    @Test
    void devePadronizarNomeETelefoneAoSalvarPaciente() {
        Paciente paciente = mock(Paciente.class);

        when(paciente.getNome()).thenReturn("  maria   da silva ");
        when(paciente.getTelefone()).thenReturn("(14) 99999-0000");
        when(pacienteRepository.existsByTelefone("5514999990000"))
                .thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = pacienteService.salvar(paciente);

        verify(paciente).setNome("Maria da Silva");
        verify(paciente).setTelefone("5514999990000");
        verify(pacienteRepository).save(paciente);

        assertSame(paciente, resultado);
    }

    @Test
    void deveRejeitarNomeVazio() {
        Paciente paciente = mock(Paciente.class);

        when(paciente.getNome()).thenReturn("   ");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> pacienteService.salvar(paciente)
        );

        assertEquals("O nome é obrigatório.", excecao.getMessage());

        verify(pacienteRepository, never()).save(any());
        verify(pacienteRepository, never()).existsByTelefone(anyString());
    }

    @Test
    void deveRejeitarTelefoneInvalido() {
        Paciente paciente = mock(Paciente.class);

        when(paciente.getNome()).thenReturn("Maria Silva");
        when(paciente.getTelefone()).thenReturn("12345");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> pacienteService.salvar(paciente)
        );

        assertEquals(
                "Informe um telefone válido com DDD.",
                excecao.getMessage()
        );

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void deveRejeitarTelefoneDuplicado() {
        Paciente paciente = mock(Paciente.class);

        when(paciente.getNome()).thenReturn("Maria Silva");
        when(paciente.getTelefone()).thenReturn("(14) 99999-0000");
        when(pacienteRepository.existsByTelefone("5514999990000"))
                .thenReturn(true);

        assertThrows(
                TelefoneDuplicadoException.class,
                () -> pacienteService.salvar(paciente)
        );

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void deveAceitarTelefoneComCodigoDoPais() {
        Paciente paciente = mock(Paciente.class);

        when(paciente.getNome()).thenReturn("joao da silva");
        when(paciente.getTelefone()).thenReturn("+55 (14) 99999-0000");
        when(pacienteRepository.existsByTelefone("5514999990000"))
                .thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        pacienteService.salvar(paciente);

        verify(paciente).setNome("Joao da Silva");
        verify(paciente).setTelefone("5514999990000");
        verify(pacienteRepository).save(paciente);
    }
}