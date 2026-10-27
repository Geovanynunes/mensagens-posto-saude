package br.com.geovany.mensagens_posto_saude;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"mensagem.sender=mock",
		"evolution.api.key=chave-de-teste"
})
class MensagensPostoSaudeApplicationApplicationTests {

	@Test
	void contextLoads() {
	}
}