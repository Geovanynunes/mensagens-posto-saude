package br.com.geovany.mensagens_posto_saude.controllers;

import br.com.geovany.mensagens_posto_saude.entities.Paciente;
import br.com.geovany.mensagens_posto_saude.repository.PacienteRepository;
import br.com.geovany.mensagens_posto_saude.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ImportacaoControllerTest {

    private PacienteRepository pacienteRepository;
    private PacienteService pacienteService;
    private ImportacaoController controller;

    @BeforeEach
    void configurar() {
        pacienteRepository = mock(PacienteRepository.class);
        pacienteService = new PacienteService(pacienteRepository);

        controller = new ImportacaoController(
                pacienteRepository,
                pacienteService
        );
    }

    @Test
    void deveImportarPacientesValidosEIgnorarDuplicadosEInvalidos()
            throws Exception {

        String csv = """
                nome,telefone
                maria da silva,(14) 99999-1111
                joao de souza,14999991111
                ana oliveira,14999992222
                carlos,12345
                registro sem telefone

                """;

        when(pacienteRepository.existsByTelefone("5514999992222"))
                .thenReturn(true);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "pacientes.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String resultado = controller.importar(arquivo, redirect);

        assertEquals("redirect:/pacientes", resultado);

        assertEquals(
                "1 pacientes importados. 2 duplicados ignorados. 2 registros inválidos ignorados.",
                redirect.getFlashAttributes().get("mensagem")
        );

        verify(pacienteRepository).saveAll(argThat(pacientes -> {
            List<Paciente> lista = new ArrayList<>();
            pacientes.forEach(lista::add);

            if (lista.size() != 1) {
                return false;
            }

            Paciente paciente = lista.get(0);

            return paciente.getNome().equals("Maria da Silva")
                    && paciente.getTelefone().equals("5514999991111");
        }));
    }

    @Test
    void deveIgnorarLinhasEmBranco() throws Exception {

        String csv = """
                nome,telefone

                maria da silva,14999991111


                """;

        when(pacienteRepository.existsByTelefone("5514999991111"))
                .thenReturn(false);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "pacientes.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String resultado = controller.importar(arquivo, redirect);

        assertEquals("redirect:/pacientes", resultado);

        assertEquals(
                "1 pacientes importados. 0 duplicados ignorados. 0 registros inválidos ignorados.",
                redirect.getFlashAttributes().get("mensagem")
        );

        verify(pacienteRepository).saveAll(anyList());
    }

    @Test
    void deveAceitarArquivoSemPacientes() throws Exception {

        String csv = "nome,telefone\n";

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "vazio.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String resultado = controller.importar(arquivo, redirect);

        assertEquals("redirect:/pacientes", resultado);

        assertEquals(
                "0 pacientes importados. 0 duplicados ignorados. 0 registros inválidos ignorados.",
                redirect.getFlashAttributes().get("mensagem")
        );

        verify(pacienteRepository).saveAll(anyList());
    }
}