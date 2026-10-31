package br.com.geovany.mensagens_posto_saude.services;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.enums.SituacaoEnvio;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.EnvioProcessor;
import br.com.geovany.mensagens_posto_saude.service.EnvioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnvioServicesTest {

    private PacienteRepository pacienteRepository;
    private EnvioRepository envioRepository;
    private EnvioDestinatarioRepository destinatarioRepository;
    private EnvioProcessor processor;
    private EnvioService envioService;

    @BeforeEach
    void configurar() {
        pacienteRepository = mock(PacienteRepository.class);
        envioRepository = mock(EnvioRepository.class);
        destinatarioRepository = mock(EnvioDestinatarioRepository.class);
        processor = mock(EnvioProcessor.class);

        envioService = new EnvioService(
                pacienteRepository,
                envioRepository,
                destinatarioRepository,
                processor
        );
    }

    @Test
    void deveCriarEnvioSomenteParaPacientesAtivos() {
        Paciente ativo = mock(Paciente.class);
        when(ativo.isAtivo()).thenReturn(true);
        when(ativo.getNome()).thenReturn("Maria Silva");
        when(ativo.getTelefone()).thenReturn("5514999999999");

        Paciente inativo = mock(Paciente.class);
        when(inativo.isAtivo()).thenReturn(false);

        when(pacienteRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(ativo, inativo));

        when(envioRepository.save(any(Envio.class)))
                .thenAnswer(invocacao -> {
                    Envio envio = invocacao.getArgument(0);
                    envio.setId(10L);
                    return envio;
                });

        Envio resultado = envioService.iniciar(
                List.of(1L, 2L),
                "Olá, {nome}!"
        );

        assertEquals(1, resultado.getTotalDestinatarios());
        assertEquals("Olá, {nome}!", resultado.getTexto());
        assertEquals(SituacaoEnvio.ENVIANDO, resultado.getSituacao());

        verify(envioRepository).save(any(Envio.class));
        verify(destinatarioRepository).saveAll(anyList());
        verify(processor).processar(10L);

        verify(ativo).getNome();
        verify(ativo).getTelefone();
        verify(inativo, never()).getNome();
    }

    @Test
    void deveImpedirEnvioQuandoNaoExistiremPacientesAtivos() {
        Paciente inativo = mock(Paciente.class);
        when(inativo.isAtivo()).thenReturn(false);

        when(pacienteRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(inativo));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> envioService.iniciar(List.of(1L), "Mensagem")
        );

        assertEquals(
                "Nenhum paciente ativo entre os selecionados.",
                excecao.getMessage()
        );

        verify(envioRepository, never()).save(any());
        verify(destinatarioRepository, never()).saveAll(anyList());
        verifyNoInteractions(processor);
    }

    @Test
    void deveInterromperEnvioQuandoFilaEstiverCheia() {
        Paciente ativo = mock(Paciente.class);
        when(ativo.isAtivo()).thenReturn(true);
        when(ativo.getNome()).thenReturn("João");
        when(ativo.getTelefone()).thenReturn("5514999999999");

        when(pacienteRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(ativo));

        when(envioRepository.save(any(Envio.class)))
                .thenAnswer(invocacao -> {
                    Envio envio = invocacao.getArgument(0);
                    envio.setId(20L);
                    return envio;
                });

        doThrow(new TaskRejectedException("Fila cheia"))
                .when(processor).processar(20L);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> envioService.iniciar(List.of(1L), "Mensagem")
        );

        assertEquals(
                "Há muitos envios na fila. Tente novamente em instantes.",
                excecao.getMessage()
        );

        verify(destinatarioRepository).saveAll(anyList());
        verify(envioRepository, times(2)).save(any(Envio.class));

        // Confirma que a campanha foi interrompida.
        assertEquals(SituacaoEnvio.INTERROMPIDO,
                capturarEnvioSalvo().getSituacao());
    }

    private Envio capturarEnvioSalvo() {
        org.mockito.ArgumentCaptor<Envio> captor =
                org.mockito.ArgumentCaptor.forClass(Envio.class);

        verify(envioRepository, times(2)).save(captor.capture());

        return captor.getAllValues().get(1);
    }
}