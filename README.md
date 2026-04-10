# Pedidos — Demo Arquitetura Hexagonal (Versão Simplificada)

> Projeto demonstrativo de **Arquitetura Hexagonal (Ports & Adapters)** com Java 21 e Spring Boot 3.
> Esta é a versão **pragmática e simplificada**, resultado de análise técnica sobre o que
> é realmente necessário para garantir o padrão sem overhead desnecessário.

---

## Sumário

1. [O que foi simplificado e por quê](#o-que-foi-simplificado-e-por-que)
2. [O que nunca pode ser simplificado](#o-que-nunca-pode-ser-simplificado)
3. [Decisões arquiteturais](#decisoes-arquiteturais)
4. [Estrutura de pacotes](#estrutura-de-pacotes)
5. [Responsabilidade de cada pacote](#responsabilidade-de-cada-pacote)
6. [Fluxo de uma requisição](#fluxo-de-uma-requisicao)
7. [Stack](#stack)
8. [Endpoints](#endpoints)
9. [Como executar](#como-executar)
10. [Referências](#referencias)

---

## O que foi simplificado e por quê

O hexagonal tem **uma única regra inegociável:**

> As dependências sempre apontam para dentro.
> Infraestrutura depende do domínio — nunca o contrário.

Tudo que garante essa regra é essencial. O resto é convenção, não lei.
A análise abaixo avaliou cada elemento do modelo completo e decidiu o que remover.

---

### Removido: Port In (interfaces de Use Case)

**Antes:** `CriarPedidoUseCase`, `ConsultarPedidoUseCase`, `AtualizarStatusPedidoUseCase`
como interfaces em `application/port/in/`.

**Motivo da remoção:** o port in existe para que o Controller dependa de uma abstração,
não da implementação concreta. Mas o Spring já resolve isso nativamente via injeção de
dependência — o Controller nunca instancia o service diretamente.

A interface adicional só traria valor real se houvesse múltiplas implementações do mesmo
use case (ex: CriarPedidoParaClientePF vs CriarPedidoParaClientePJ), o que não é o caso.
Em projetos com um único adapter de entrada (HTTP), é overhead sem benefício prático.

**Impacto:** zero. A regra de dependência é mantida — o Controller continua sem
conhecer detalhes de JPA, REST ou qualquer infraestrutura.

---

### Removido: Command record

**Antes:** cada use case tinha um `Command` record que encapsulava os parâmetros de entrada.

**Motivo da remoção:** o `Command` serve para desacoplar o use case do adapter de entrada,
permitindo que diferentes adapters (HTTP, Kafka, CLI) montem o mesmo objeto. Na prática,
se há apenas um adapter de entrada, esse desacoplamento nunca será exercitado.

**O que o substituiu:** parâmetros diretos no método do use case.

```java
// Antes
Pedido executar(Command command);

// Depois
Pedido executar(String descricao, BigDecimal valor, String cep);
```

**Quando reintroduzir:** se um segundo adapter de entrada for adicionado (consumer Kafka,
job agendado), o `Command` volta a fazer sentido como objeto de transferência neutro.

---

### Removido: PedidoDomainService

**Antes:** classe separada com as regras de validação de criação de pedido.

**Motivo da remoção:** as regras eram simples e estáveis (tamanho mínimo de descrição,
valor máximo). A entidade `Pedido.criar()` absorveu essa responsabilidade sem prejudicar
a coesão. Um Domain Service faz sentido quando a lógica envolve múltiplas entidades
ou quando cresce a ponto de comprometer a legibilidade da entidade.

**Consequência:** `DomainConfig.java` também foi removido, pois existia apenas para
expor o Domain Service como bean Spring.

**Quando reintroduzir:** se a lógica de criação crescer, envolver outras entidades
(ex: verificar política de crédito do cliente) ou se a entidade ficar grande demais.

---

### Movido: Port Out de `application/port/out/` para `application/`

**Antes:** `PedidoRepositoryPort` e `EnderecoServicePort` em `application/port/out/`.

**Motivo:** com a remoção do `port/in/`, o pacote `port/` perdeu razão de existir.
Os ports out foram movidos para `application/` diretamente, onde convivem com os
use cases que os utilizam. Menos profundidade de pacote, mesma clareza semântica.

---

## O que nunca pode ser simplificado

Estes elementos são a essência do hexagonal. Remover qualquer um deles viola o padrão.

| Elemento | Por quê é inegociável |
|---|---|
| `PedidoRepositoryPort` | Sem essa interface, `CriarPedidoService` importaria `JpaRepository` — infraestrutura dentro da aplicação. Quebra o hexagonal. |
| `EnderecoServicePort` | Sem essa interface, o use case saberia que existe HTTP e ViaCEP. Mesmo problema. |
| `PedidoJpaAdapter` implementando o port | É a inversão de dependência. O adapter depende do contrato — não o contrário. |
| `PedidoEntity` separada de `Pedido` | Sem separação, `@Entity` entra no domínio. O domínio passaria a depender do Hibernate. |
| `PedidoEntityMapper` | Sem ele, o adapter precisaria conhecer os internos da entidade de domínio para construí-la. |
| Domínio sem imports de Spring/JPA | A regra fundamental. Se `Pedido.java` importar `@Entity`, o hexagonal está quebrado. |

---

## Decisões Arquiteturais

### DA-01 — Ports out em `application/`, não em `domain/`

**Decisão:** `PedidoRepositoryPort` e `EnderecoServicePort` vivem em `application/`.

**Contexto:** há duas escolas:
- **Escola DDD (Evans):** repositório é conceito de domínio → port out em `domain/`
- **Escola Hexagonal (Hombergs):** port existe para servir o use case → em `application/`

**Motivo:** `Pedido.java` não usa `PedidoRepositoryPort` em nenhum momento. Quem usa é
o `CriarPedidoService`. Colocar a interface em `domain/` seria convenção sem benefício
técnico — a dependência continuaria partindo do use case, não do domínio.

**Referência:** reflectoring.io, Medium/@alex9954161.

---

### DA-02 — Sem interface de Port In

**Decisão:** Controller injeta `CriarPedidoService` diretamente, sem interface intermediária.

**Motivo:** o Spring resolve o desacoplamento via injeção. A interface só seria necessária
com múltiplos adapters de entrada ou para mockar sem `@MockBean` em testes — ambos
não se aplicam aqui.

**Quando reverter:** ao adicionar um segundo adapter de entrada (Kafka consumer, CLI, job).

---

### DA-03 — Validação de criação absorvida pela entidade

**Decisão:** `Pedido.criar()` valida as regras de criação. Sem `PedidoDomainService` separado.

**Motivo:** as regras são simples e coesas com a entidade. Domain Service é a solução
certa quando a lógica envolve múltiplas entidades ou cresce além da responsabilidade
de uma única classe.

**Quando reverter:** ao adicionar regras que envolvam outras entidades (ex: cliente, estoque).

---

### DA-04 — `PedidoEntity` separada de `Pedido`

**Decisão:** entidade JPA e entidade de domínio são classes distintas com mapper entre elas.

**Motivo:** se a entidade de domínio carregasse `@Entity`, `@Column`, etc., ela dependeria
do Hibernate. Mudanças no schema impactariam o domínio. O custo de um mapper a mais
é amplamente compensado pela independência do núcleo.

---

### DA-05 — Um use case por operação

**Decisão:** três services separados em vez de um `PedidoService` genérico.

**Motivo:** cada use case tem exatamente o que precisa. `ConsultarPedidoService` não
tem `EnderecoServicePort` como dependência — porque não precisa. Um service genérico
com 10 métodos acumula todas as dependências, mesmo as desnecessárias.

---

### DA-06 — Invariantes de estado na entidade

**Decisão:** `Pedido.confirmar()` e `Pedido.cancelar()` protegem suas próprias transições.

**Motivo:** a entidade é quem conhece seu estado. Regras do tipo "só PENDENTE pode ser
confirmado" pertencem à entidade — não ao use case, não ao adapter.

---

### DA-07 — Fallback silencioso no REST Client

**Decisão:** falha na ViaCEP retorna `Endereco.vazio()` em vez de propagar exceção.

**Motivo:** endereço é dado enriquecedor, não bloqueante. Instabilidade em API externa
não deve impedir a criação do pedido. Log de erro garante visibilidade operacional.

---

### DA-08 — `@Transactional(readOnly = true)` em consultas

**Decisão:** `ConsultarPedidoService` usa `readOnly = true`.

**Motivo:** impede flush desnecessário do Hibernate, reduz locks e habilita réplicas
de leitura se o datasource estiver configurado para isso.

---

## Estrutura de Pacotes

```
src/main/java/com/empresa/pedidos/
│
├── domain/
│   └── model/
│       ├── Pedido.java                  Entity + validação de criação + invariantes
│       ├── Endereco.java               Value Object imutável
│       ├── StatusPedido.java           Enum de domínio
│       └── PedidoNaoEncontradoException.java
│
├── application/
│   ├── PedidoRepositoryPort.java        Port out — contrato de persistência
│   ├── EnderecoServicePort.java         Port out — contrato de API externa
│   └── usecase/
│       ├── CriarPedidoService.java      Orquestra criação
│       ├── ConsultarPedidoService.java  Orquestra consultas
│       └── AtualizarStatusPedidoService.java
│
├── adapter/
│   ├── in/web/
│   │   ├── PedidoController.java        Recebe HTTP
│   │   ├── PedidoRequest.java           DTO de entrada
│   │   ├── PedidoResponse.java          DTO de saída
│   │   └── GlobalExceptionHandler.java
│   └── out/
│       ├── persistence/
│       │   ├── PedidoJpaAdapter.java    Implementa PedidoRepositoryPort
│       │   ├── PedidoJpaRepository.java Spring Data
│       │   ├── PedidoEntity.java        @Entity separada do domínio
│       │   └── PedidoEntityMapper.java  Entity <-> Domain
│       └── restclient/
│           ├── EnderecoRestAdapter.java  Implementa EnderecoServicePort
│           └── ViaCepResponse.java      DTO da API externa
│
├── config/
│   └── RestTemplateConfig.java
│
├── shared/
│   ├── ApiError.java
│   └── CepUtil.java
│
└── PedidosApplication.java
```

**Resultado da simplificação:**

| | Versão completa | Versão simplificada |
|---|---|---|
| Arquivos Java | 33 | 27 |
| Pacotes | 7 profundos | 5 enxutos |
| Interfaces de use case | 3 | 0 |
| Command records | 3 | 0 |
| Domain Services | 1 | 0 |
| **Hexagonal intacto?** | ✅ | ✅ |

---

## Responsabilidade de Cada Pacote

### `domain/model/`

**Núcleo — zero importações de Spring, JPA ou qualquer framework.**

| Classe | Tipo | Responsabilidade |
|--------|------|-----------------|
| `Pedido` | Entity | Estado do pedido, validação de criação (`criar()`), invariantes de transição (`confirmar()`, `cancelar()`). Construtor privado, acesso via factory methods. |
| `Endereco` | Value Object | Imutável. Igualdade por valor (equals/hashCode nos campos). `vazio()` como factory para ausência. |
| `StatusPedido` | Enum | Estados válidos com descrição legível. Linguagem ubíqua do negócio. |
| `PedidoNaoEncontradoException` | Exceção de domínio | Conceito de negócio ("pedido não existe"), não erro de infraestrutura. |

**Regra:** qualquer `import org.springframework` ou `import jakarta.persistence` aqui é violação.

---

### `application/` (raiz)

**Contratos de saída — o que os use cases precisam do mundo externo.**

| Classe | Responsabilidade |
|--------|-----------------|
| `PedidoRepositoryPort` | Interface de persistência. Define `salvar`, `buscarPorId`, `buscarTodos`. O use case dita o contrato; o adapter JPA implementa. |
| `EnderecoServicePort` | Interface de consulta de CEP. O use case não sabe que existe HTTP, ViaCEP ou qualquer outra tecnologia. |

---

### `application/usecase/`

**Orquestração — coordena o fluxo sem conter regra de negócio.**

| Classe | Responsabilidade |
|--------|-----------------|
| `CriarPedidoService` | Busca endereço → cria entidade de domínio → persiste. Sem `if` de negócio. |
| `ConsultarPedidoService` | Delega ao repositório, lança exceção de domínio se não encontrado. `readOnly = true`. |
| `AtualizarStatusPedidoService` | Busca → delega transição à entidade → persiste. A regra vive em `Pedido.confirmar()`. |

**Sinal de alerta:** se aparecer um `if` de negócio aqui, ele pertence ao domínio.

---

### `adapter/in/web/`

**Entrada HTTP — traduz requisição em chamada ao use case.**

| Classe | Responsabilidade |
|--------|-----------------|
| `PedidoController` | Recebe HTTP, valida com `@Valid`, chama use case, retorna response. Zero lógica de negócio. |
| `PedidoRequest` | DTO de entrada com validações de formato (`@NotBlank`, `@DecimalMin`, `@Pattern`). |
| `PedidoResponse` | DTO de saída. Controla o contrato da API. Factory method `de(Pedido)`. |
| `GlobalExceptionHandler` | Traduz exceções em respostas HTTP padronizadas. `PedidoNaoEncontradoException` → 404. |

---

### `adapter/out/persistence/`

**Saída para banco — implementa o port de repositório com JPA.**

| Classe | Responsabilidade |
|--------|-----------------|
| `PedidoJpaAdapter` | Implementa `PedidoRepositoryPort`. Usa mapper para converter entre mundos. |
| `PedidoJpaRepository` | Interface Spring Data JPA. Opera sobre `PedidoEntity`. |
| `PedidoEntity` | `@Entity` com mapeamento de colunas. Separada do domínio — mudanças no schema não impactam `Pedido`. |
| `PedidoEntityMapper` | Converte `Pedido` ↔ `PedidoEntity`. Explícito e testável. |

---

### `adapter/out/restclient/`

**Saída para API externa — implementa o port de endereço.**

| Classe | Responsabilidade |
|--------|-----------------|
| `EnderecoRestAdapter` | Implementa `EnderecoServicePort`. HTTP para ViaCEP com fallback silencioso (DA-07). |
| `ViaCepResponse` | DTO do JSON da ViaCEP. Isolado aqui — mudanças na API externa não chegam ao domínio. |

---

### `config/`

**Configurações Spring — beans de infraestrutura.**

| Classe | Responsabilidade |
|--------|-----------------|
| `RestTemplateConfig` | `RestTemplate` com timeout de conexão (3s) e leitura (5s). |

---

### `shared/`

**Utilitários sem estado, sem regra de negócio, usados por múltiplas camadas.**

| Classe | Responsabilidade |
|--------|-----------------|
| `ApiError` | Envelope de erro padronizado: `status`, `erro`, `mensagem`, `timestamp`, `campos`. |
| `CepUtil` | Limpeza e formatação de CEP. Estático, sem Spring. |

---

## Fluxo de uma Requisição

```
POST /api/v1/pedidos
        │
        ▼
┌──────────────────────┐
│  PedidoController    │  Adapter In — valida @Valid, chama use case
│  adapter/in/web      │
└────────┬─────────────┘
         │ chama diretamente
         ▼
┌──────────────────────┐
│  CriarPedidoService  │  Use Case — orquestra o fluxo
│  application/usecase │
└──┬───────────────────┘
   │                  │
   │ busca CEP        │ persiste
   ▼                  ▼
┌──────────────┐  ┌────────────────────┐
│EnderecoService│  │ PedidoRepository   │  Ports Out — interfaces
│Port           │  │ Port               │
└──────┬───────┘  └────────┬───────────┘
       │ implementado por  │ implementado por
       ▼                   ▼
┌──────────────┐  ┌────────────────────┐
│EnderecoRest  │  │ PedidoJpaAdapter   │  Adapters Out
│Adapter       │  │ → PedidoEntity     │
│→ ViaCEP HTTP │  │ → PostgreSQL       │
└──────────────┘  └────────────────────┘
```

---

## Stack

| Tecnologia   | Versão | Uso                                      |
|--------------|--------|------------------------------------------|
| Java         | 21     | Records, pattern matching                |
| Spring Boot  | 3.2.x  | Web, Data JPA, Validation, Actuator      |
| PostgreSQL   | 16     | Banco relacional                         |
| Flyway       | —      | Migrations versionadas                   |
| RestTemplate | —      | HTTP client para ViaCEP                  |
| H2           | —      | Banco em memória nos testes              |
| WireMock     | 3.4.2  | Mock de APIs externas nos testes         |

---

## Endpoints

| Método | URL | Descrição | Status |
|--------|-----|-----------|--------|
| POST | `/api/v1/pedidos` | Cria pedido. Consulta CEP se fornecido. | 201 |
| GET | `/api/v1/pedidos` | Lista todos os pedidos | 200 |
| GET | `/api/v1/pedidos/{id}` | Busca por UUID | 200/404 |
| PATCH | `/api/v1/pedidos/{id}/confirmar` | Confirma (apenas PENDENTE) | 200/422 |
| PATCH | `/api/v1/pedidos/{id}/cancelar` | Cancela | 200/422 |

### Exemplo

```bash
curl -X POST http://localhost:8080/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{"descricao": "Monitor 4K", "valor": 3500.00, "cep": "01310-100"}'
```

---

## Como Executar

```bash
# 1. Banco de dados
docker run --name pedidos-db \
  -e POSTGRES_DB=pedidos_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:16

# 2. Aplicação
./mvnw spring-boot:run

# 3. Testes
./mvnw test
```

---

## Referências

### Padrão original

- **Hexagonal Architecture — Alistair Cockburn**
  https://alistair.cockburn.us/hexagonal-architecture/
  *A fonte primária do padrão (2005). Define ports, adapters e a regra de inversão de dependência.*

### Implementação em Spring Boot

- **Hexagonal Architecture with Java and Spring — Tom Hombergs (reflectoring.io)**
  https://reflectoring.io/spring-hexagonal/
  *Referência mais citada para Spring Boot. Fundamentou a decisão de ports out em `application/`
  e use cases como cidadãos de primeira classe.*

- **Hexagonal Architecture With Spring Boot — Arho Huttunen**
  https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/
  *Demonstra `@UseCase` customizado para evitar `@Service` no domínio. Fundamentou DA-02 e DA-03.*

- **Hexagonal Architecture in Spring Boot Microservices — Medium/@alex9954161**
  https://medium.com/@alex9954161/hexagonal-architecture-in-spring-boot-microservices-a-complete-guide-with-folder-structure-be23eb11c739
  *Referência para estrutura `application/usecase/` — padrão mais adotado em 2024/2025.*

### Simplificação e pragmatismo

- **Clean and Modular Java: A Hexagonal Architecture Approach — foojay.io**
  https://foojay.io/today/clean-and-modular-java-a-hexagonal-architecture-approach/
  *Abordagem pragmática: `usecase/` como local canônico, sem overhead de interfaces de port in.*

- **Hexagonal Architecture Template — Kamil Mazurek**
  https://kamilmazurek.pl/hexagonal-architecture-template
  *Template público com separação clara de adapters, ports e use cases.*

### Conceitos relacionados

- **Clean Architecture — Robert C. Martin**
  https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
  *Hexagonal e Clean Architecture compartilham inversão de dependências. Muitos projetos
  misturam as nomenclaturas (Core/Infra vs Domain/Adapters).*

- **Domain-Driven Design Reference — Eric Evans**
  https://www.domainlanguage.com/ddd/reference/
  *Fundamenta Entity, Value Object e Domain Service. Origem da discussão sobre port out
  em `domain/` vs `application/`.*

---

*Este projeto é intencionalmente didático. Cada decisão está documentada com contexto,
motivo e condição de reversão. O objetivo é que o time entenda o porquê — não apenas copie a estrutura.*
