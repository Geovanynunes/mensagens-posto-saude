# ConectaSaúde

Sistema web para um posto de saúde avisar pacientes por WhatsApp (por exemplo: *chegou um medicamento*, *um exame foi autorizado*). O funcionário cadastra ou importa os pacientes por planilha, escolhe uma mensagem pronta (ou escreve uma nova), seleciona quem vai receber e o sistema envia uma mensagem por vez, com acompanhamento do andamento.

Projeto acadêmico desenvolvido com Java e Spring Boot.

> **Atenção:** este projeto lida com telefones de pacientes, que são dados pessoais ligados à saúde (LGPD). Use **apenas dados fictícios** em desenvolvimento e testes. Veja a seção [Avisos importantes](#avisos-importantes).

## Funcionalidades

- **Login de administrador** (Spring Security, senha com BCrypt).
- **Pacientes:** cadastro manual, importação por CSV, filtro por nome ou telefone. Telefones são padronizados (`55` + DDD + número) e não podem se repetir. Nomes são normalizados (primeira letra maiúscula).
- **Modelos de mensagem:** textos prontos para os avisos mais frequentes (criar, editar, excluir).
- **Envio de mensagens:**
  - escolha dos destinatários com caixas de seleção, filtro e "marcar todos";
  - personalização com `{nome}` (vira o primeiro nome do paciente);
  - envio em segundo plano, **um destinatário por vez**, com intervalo configurável;
  - andamento em tempo real no histórico (Enviando 3/10) e status por destinatário (Pendente, Enviado, Falhou).
- **Canal de envio trocável por configuração:** simulado (mock) ou Evolution API.

## Tecnologias

| Parte | Tecnologia |
|---|---|
| Linguagem / framework | Java 21, Spring Boot 4 |
| Web e telas | Spring Web MVC, Thymeleaf, HTML e CSS próprios |
| Banco de dados | PostgreSQL, Spring Data JPA (Hibernate) |
| Segurança | Spring Security |
| Envio de mensagens | Evolution API (via Docker Compose) ou envio simulado |
| Build | Maven (wrapper incluído) |

## Como o sistema é organizado

```
src/main/java/br/com/geovany/mensagens_posto_saude/
  controllers/   telas e rotas (pacientes, mensagens, modelos, login, importação)
  service/       regras de negócio (PacienteService, EnvioService, EnvioProcessor)
  repository/    acesso ao banco (Spring Data JPA)
  entities/      tabelas (Paciente, Envio, EnvioDestinatario, ModeloMensagem, Usuario)
  sender/        canais de envio (MensagemSender, MockSender, EvolutionApiSender)
  config...      segurança, execução em segundo plano e criação do admin
src/main/resources/
  templates/     páginas Thymeleaf
  static/css/    estilo.css
evolution-api/   docker-compose.yml e .env.example da Evolution API
```

**Fluxo de um envio:**

1. O usuário escolhe a mensagem e os destinatários e clica em enviar.
2. O `EnvioService` grava o envio e os destinatários como *Pendente* e responde na hora.
3. O `EnvioProcessor` (tarefa em segundo plano, uma de cada vez) percorre os pendentes: troca o `{nome}`, chama o `MensagemSender`, grava o resultado e espera o intervalo.
4. A tela de histórico atualiza o andamento sozinha.

**Trocar o canal de envio** não exige mexer no resto do código: basta criar uma classe que implemente `MensagemSender` e escolher o canal em `mensagem.sender`.

## Como rodar

### Pré-requisitos

- Java 21 ou superior
- PostgreSQL instalado localmente
- Docker Desktop (para a Evolution API)
- IntelliJ IDEA (ou outro editor com suporte a Maven)

### 1. Clonar o projeto

```bash
git clone https://github.com/Geovanynunes/mensagens-posto-saude.git
cd mensagens-posto-saude
```

### 2. Criar o banco de dados

Crie um banco chamado `posto` no PostgreSQL (pelo pgAdmin ou pelo `psql`):

```sql
CREATE DATABASE posto;
```

As tabelas são criadas automaticamente na primeira execução.

### 3. Configurar o arquivo `local.properties`

Crie o arquivo `src/main/resources/local.properties`. Ele guarda segredos e **não vai para o Git** (está no `.gitignore`):

```properties
# senha do usuário "postgres" do seu PostgreSQL local
spring.datasource.password=SUA_SENHA_DO_POSTGRES

# primeiro administrador do sistema (criado só se não existir nenhum usuário)
admin.usuario=admin
admin.senha=ESCOLHA_UMA_SENHA_FORTE

# chave da Evolution API (a mesma AUTHENTICATION_API_KEY do .env da Evolution)
evolution.api.key=SUA_CHAVE_DA_EVOLUTION
```

Outras propriedades úteis, em `application.properties`:

| Propriedade | Para que serve | Exemplo |
|---|---|---|
| `server.port` | porta do sistema | `8082` |
| `mensagem.sender` | canal de envio: `mock` (simulado) ou `evolution` | `evolution` |
| `mensagem.intervalo-ms` | pausa entre uma mensagem e outra, em milissegundos | `3000` |
| `evolution.api.url` | endereço da Evolution API | `http://localhost:8081` |
| `evolution.api.instance` | nome da instância criada na Evolution | `posto-teste` |

> Para testar sem WhatsApp, use `mensagem.sender=mock`: as mensagens aparecem apenas no log do sistema.

### 4. Subir a Evolution API (só se for usar `mensagem.sender=evolution`)

A Evolution API roda em Docker, junto com um PostgreSQL e um Redis próprios. Não precisa instalar nada além do Docker Desktop.

```bash
cd evolution-api
cp .env.example .env        # no Windows (PowerShell): Copy-Item .env.example .env
```

Edite o `.env` e troque os valores de exemplo, principalmente `AUTHENTICATION_API_KEY` (uma chave longa e sua) e `POSTGRES_PASSWORD`. A senha do Postgres precisa ser a mesma usada na URL de conexão do banco dentro do `.env`; evite caracteres especiais nela.

```bash
docker compose up -d
```

Abra o painel em <http://localhost:8081/manager>, entre com a chave da API e:

1. crie uma instância com o nome `posto-teste` (ou o nome que estiver em `evolution.api.instance`);
2. leia o QR code pelo WhatsApp do celular: *Configurações → Dispositivos conectados → Conectar um dispositivo*.

Comandos úteis (dentro da pasta `evolution-api`):

```bash
docker compose ps           # o que está rodando
docker compose logs -f      # logs ao vivo
docker compose down         # parar (mantém os dados e a sessão do WhatsApp)
```

> Não use `docker compose down -v`: ele apaga os volumes e você precisará ler o QR code de novo.

### 5. Rodar o sistema

Pelo IntelliJ, execute a classe `MensagensPostoSaudeApplication`, ou pelo terminal:

```bash
./mvnw spring-boot:run        # Windows (PowerShell): .\mvnw spring-boot:run
```

Acesse <http://localhost:8082> e entre com o usuário e a senha definidos em `admin.usuario` e `admin.senha`.

## Como usar

1. **Pacientes:** cadastre manualmente ou importe um CSV.
   Formato do CSV (primeira linha é o cabeçalho; aceita `;` ou `,` como separador):
   ```
   nome;telefone
   Maria Silva;(14) 99999-0001
   João Souza;14999990002
   ```
   Telefones repetidos e inválidos são ignorados, e a mensagem de retorno informa as quantidades. Nomes com vírgula não são suportados.
2. **Modelos:** cadastre os textos mais usados. Escreva `{nome}` onde o primeiro nome do paciente deve aparecer.
3. **Mensagens:** escolha um modelo (ou escreva o texto), filtre e marque os pacientes, e envie. Acompanhe o andamento no histórico e abra **Detalhes** para ver o status de cada pessoa.

## Avisos importantes

**Evolution API não é oficial.** Ela se conecta ao WhatsApp como se fosse o WhatsApp Web. Isso vai contra os termos de uso do WhatsApp: o número conectado pode ser bloqueado e uma atualização do WhatsApp pode quebrar a integração. Nos testes deste projeto ela está ligada ao WhatsApp pessoal do desenvolvedor. Portanto:

- use só para demonstração e testes, com poucos destinatários que concordaram em receber;
- não use para envio em massa nem para avisar pacientes reais;
- para uso de verdade, o caminho é a **API oficial do WhatsApp (WhatsApp Business Cloud API)**, que exige conta verificada na Meta, mensagens baseadas em templates aprovados e consentimento dos pacientes. Como o canal é trocável (`mensagem.sender`), essa mudança fica concentrada em uma classe nova.

**Dados pessoais.** Telefones de pacientes são dados pessoais ligados à saúde. Antes de qualquer uso real, é preciso definir base legal e consentimento (LGPD), restringir o acesso, proteger o banco e os backups, e hospedar com HTTPS.

**Segredos.** Nunca versione `local.properties` nem `.env`. Só o `.env.example` (com valores de exemplo) vai para o Git.

## Limitações conhecidas

- Funciona com **uma única instância** do sistema rodando (a fila de envio fica na memória do processo).
- Se o sistema for reiniciado durante um envio, o envio é marcado como *Interrompido* e os pendentes não são retomados automaticamente.
- A resposta da Evolution API indica que a mensagem foi **aceita**, e não que foi entregue ou lida; o status final de entrega ainda não é acompanhado.
- Números de DDDs fora de SP, RJ e ES podem ter o 9º dígito tratado de forma diferente pelo WhatsApp.

## Próximos passos

- [ ] Integração com a API oficial do WhatsApp (Cloud API)
- [ ] Status de entrega por webhook (entregue, lida, falhou)
- [ ] Botão para retomar envios interrompidos
- [ ] Troca de senha e múltiplos usuários
- [ ] Publicação em nuvem (HTTPS, banco gerenciado, variáveis de ambiente)

## Autor

Geovany — projeto acadêmico proposto pelo professor Marcos Mendes.
