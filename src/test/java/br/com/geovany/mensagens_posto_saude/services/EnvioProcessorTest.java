package br.com.geovany.mensagens_posto_saude.service;

import br.com.geovany.mensagens_posto_saude.entities.Envio;
import br.com.geovany.mensagens_posto_saude.entities.EnvioDestinatario;
import br.com.geovany.mensagens_posto_saude.enums.SituacaoEnvio;
import br.com.geovany.mensagens_posto_saude.enums.StatusEnvio;
import br.com.geovany.mensagens_posto_saude.repository.EnvioDestinatarioRepository;
import br.com.geovany.mensagens_posto_saude.repository.EnvioRepository;
import br.com.geovany.mensagens_posto_saude.sender.MensagemSender;
import br.com.geovany.mensagens_posto_saude.sender.ResultadoEnvio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnvioProcessorTest {

    private EnvioRepository envioRepository;
    private EnvioDestinatarioRepository destinatarioRepository;
    private MensagemSender sender;
    private EnvioProcessor processor;

    @BeforeEach
    void configurar() {
        envioRepository = mock(EnvioRepository.class);
        destinatarioRepository = mock(EnvioDestinatarioRepository.class);
        sender = mock(MensagemSender.class);

        processor = new EnvioProcessor(
                envioRepository,
                destinatarioRepository,
                sender,
                0
        );
    }

    @Test
    void deveProcessarEnvioComSucesso() {
        Envio envio = new Envio("Olá, {nome}!", 1);
        envio.setId(1L);

        EnvioDestinatario destinatario = new EnvioDestinatario();
        destinatario.setNome("Maria Silva");
        destinatario.setTelefone("5514999999999");
        destinatario.setStatus(StatusEnvio.PENDENTE);

        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(destinatarioRepository
                .findByEnvioIdAndStatusOrderByIdAsc(1L, StatusEnvio.PENDENTE))
                .thenReturn(List.of(destinatario));

        when(sender.enviar("5514999999999", "Olá, Maria!"))
                .thenReturn(ResultadoEnvio.ok());

        processor.processar(1L);

        assertEquals(StatusEnvio.ENVIADO, destinatario.getStatus());
        assertEquals(1, envio.getProcessados());
        assertEquals(0, envio.getFalhas());
        assertEquals(SituacaoEnvio.CONCLUIDO, envio.getSituacao());

        verify(sender).enviar("5514999999999", "Olá, Maria!");
        verify(destinatarioRepository).save(destinatario);
        verify(envioRepository, atLeastOnce()).save(envio);
    }

    @Test
    void deveRegistrarFalhaQuandoRemetenteRetornarErro() {
        Envio envio = new Envio("Olá, {nome}!", 1);
        envio.setId(2L);

        EnvioDestinatario destinatario = new EnvioDestinatario();
        destinatario.setNome("João Silva");
        destinatario.setTelefone("5514999999999");
        destinatario.setStatus(StatusEnvio.PENDENTE);

        when(envioRepository.findById(2L)).thenReturn(Optional.of(envio));
        when(destinatarioRepository
                .findByEnvioIdAndStatusOrderByIdAsc(2L, StatusEnvio.PENDENTE))
                .thenReturn(List.of(destinatario));

        when(sender.enviar(anyString(), anyString()))
                .thenReturn(ResultadoEnvio.falha("Número inválido"));

        processor.processar(2L);

        assertEquals(StatusEnvio.FALHOU, destinatario.getStatus());
        assertEquals("Número inválido", destinatario.getErro());
        assertEquals(1, envio.getProcessados());
        assertEquals(1, envio.getFalhas());
        assertEquals(SituacaoEnvio.CONCLUIDO, envio.getSituacao());
    }

    @Test
    void deveRegistrarFalhaQuandoRemetenteLancarExcecao() {
        Envio envio = new Envio("Olá, {nome}!", 1);
        envio.setId(3L);

        EnvioDestinatario destinatario = new EnvioDestinatario();
        destinatario.setNome("Carlos");
        destinatario.setTelefone("5514999999999");
        destinatario.setStatus(StatusEnvio.PENDENTE);

        when(envioRepository.findById(3L)).thenReturn(Optional.of(envio));
        when(destinatarioRepository
                .findByEnvioIdAndStatusOrderByIdAsc(3L, StatusEnvio.PENDENTE))
                .thenReturn(List.of(destinatario));

        when(sender.enviar(anyString(), anyString()))
                .thenThrow(new RuntimeException("Erro de conexão"));

        processor.processar(3L);

        assertEquals(StatusEnvio.FALHOU, destinatario.getStatus());
        assertEquals("Erro de conexão", destinatario.getErro());
        assertEquals(1, envio.getFalhas());
        assertEquals(SituacaoEnvio.CONCLUIDO, envio.getSituacao());
    }

    @Test
    void naoDeveProcessarQuandoEnvioNaoExistir() {
        when(envioRepository.findById(4L)).thenReturn(Optional.empty());

        processor.processar(4L);

        verifyNoInteractions(sender);
        verifyNoInteractions(destinatarioRepository);
        verify(envioRepository, never()).save(any());
    }
}