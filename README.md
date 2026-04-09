# Pedidos — Demo Arquitetura Hexagonal

> Projeto demonstrativo de **Arquitetura Hexagonal (Ports & Adapters)** com Java 21 e Spring Boot 3.
> Criado para servir como referência de estrutura e tomada de decisão arquitetural para o time.

---

## Sumário

1. [Por que Hexagonal?](#por-que-hexagonal)
2. [Decisões arquiteturais](#decisoes-arquiteturais)
3. [Estrutura de pacotes](#estrutura-de-pacotes)
4. [Responsabilidade de cada pacote](#responsabilidade-de-cada-pacote)
5. [Fluxo de uma requisição](#fluxo-de-uma-requisicao)
6. [Stack](#stack)
7. [Endpoints](#endpoints)
8. [Como executar](#como-executar)
9. [Referências](#referencias)

---

## Por que Hexagonal?

Em arquiteturas tradicionais em camadas (Controller → Service → Repository), o domínio
de negócio fica acoplado à infraestrutura: troca de banco de dados exige alterar serviços,
testar uma regra de negócio obriga subir JPA, mudar um endpoint vaza para dentro do core.

A Arquitetura Hexagonal resolve isso invertendo as dependências:
**a infraestrutura depende do domínio, nunca o contrário.**

```
Camadas tradicionais:          Hexagonal:
Controller                     Adapter (HTTP)
    ↓                              ↓
  Service          vs         Port In (interface)
    ↓                              ↓
Repository                    Use Case (orquestra)
    ↓                              ↓
  Banco                       Port Out (interface)
                                   ↓
                              Adapter (JPA/REST)
                                   ↓
                                 Banco
```

No modelo hexagonal, o domínio e os use cases não importam nada de Spring, JPA ou HTTP.
Isso garante:

- **Testabilidade** — regras de negócio testadas sem subir contexto Spring
- **Substituibilidade** — trocar PostgreSQL por MongoDB sem tocar no domínio
- **Legibilidade** — a estrutura de pastas conta a história do negócio, não da tecnologia

---

## Decisões Arquiteturais

Esta seção documenta explicitamente cada decisão tomada neste projeto e o motivo.
O objetivo é que qualquer membro do time entenda o *porquê*, não só o *o quê*.

---

### DA-01 — Ports ficam em `application/`, não em `domain/`

**Decisão:** as interfaces de Port In e Port Out vivem em `application/port/`, não em `domain/port/`.

**Contexto:** há duas escolas sobre onde os ports devem ficar:
- Escola A: ports dentro de `domain/` (o domínio define o que precisa)
- Escola B: ports dentro de `application/` (os ports existem para servir os use cases)

**Motivo da escolha:** os ports são contratos definidos *pela aplicação* para permitir que
os use cases funcionem sem depender de infraestrutura. O domínio puro (entidades, value
objects, domain services) não tem consciência de que ports existem. Colocar ports em
`application/` torna mais explícito que eles pertencem à camada de orquestração.

Esta é a convenção adotada pela maioria dos projetos de referência levantados:
reflectoring.io, Medium/@alex9954161, foojay.io.

**Consequência:** `domain/` contém *apenas* modelo puro — sem interfaces de contrato.
Qualquer novo integrante que abrir `domain/` verá só regras de negócio.

---

### DA-02 — Implementações dos Use Cases ficam em `application/usecase/`

**Decisão:** as classes que implementam os ports de entrada (`CriarPedidoService`, etc.)
ficam em `application/usecase/`, não soltas em `application/`.

**Motivo:** sem o subpacote `usecase/`, a pasta `application/` misturaria interfaces
(ports) com implementações (services), dificultando navegação. O subpacote torna explícito
que aquelas classes *são* os casos de uso do sistema — cidadãos de primeira classe.

**Referência:** Tom Hombergs (reflectoring.io) promove use cases como "first-class
citizens" — uma classe por caso de uso, com nome que reflete a ação de negócio.

---

### DA-03 — Domain Services não usam `@Service` do Spring

**Decisão:** `PedidoDomainService` é uma classe Java pura, instanciada manualmente
via `DomainConfig` e exposta como `@Bean`.

**Motivo:** anotar uma classe de domínio com `@Service` introduz uma dependência de
framework no núcleo da aplicação — o oposto do que a arquitetura hexagonal propõe.
A configuração Spring fica na borda (`config/`), não no núcleo (`domain/`).

**Consequência:** `PedidoDomainService` pode ser instanciado e testado sem contexto
Spring, em qualquer teste unitário puro.

---

### DA-04 — `PedidoEntity` separada de `Pedido`

**Decisão:** existe uma classe `PedidoEntity` com anotações JPA e uma classe `Pedido`
de domínio. São objetos distintos, convertidos pelo `PedidoEntityMapper`.

**Motivo:** se a entidade de domínio carregasse anotações `@Entity`, `@Column`, etc.,
ela passaria a depender do Hibernate — um detalhe de infraestrutura. Mudanças no schema
do banco ou na versão do ORM impactariam o domínio. A separação garante que o domínio
evolui independentemente da persistência.

**Custo:** um mapper a mais. O benefício supera o custo em projetos com vida longa.

---

### DA-05 — Um Use Case por operação

**Decisão:** há três use cases separados: `CriarPedidoUseCase`, `ConsultarPedidoUseCase`
e `AtualizarStatusPedidoUseCase`. Não existe um `PedidoService` genérico.

**Motivo:** um serviço com muitos métodos tende a acumular dependências desnecessárias.
`ConsultarPedidoService` não precisa saber que `EnderecoServicePort` existe — e não sabe.
Cada use case tem exatamente o que precisa, nada mais.

**Referência:** Single Responsibility Principle aplicado em nível de use case.

---

### DA-06 — Domain Service valida regras; entidade guarda invariantes de estado

**Decisão:** `PedidoDomainService` valida regras de criação (valor máximo, tamanho mínimo
da descrição). `Pedido` guarda invariantes de transição de estado (só PENDENTE pode ser
confirmado).

**Motivo:** a distinção é conceitual:
- Invariante de estado → pertence à entidade (quem melhor conhece seu próprio estado)
- Regra de criação/negócio → pode envolver múltiplos conceitos e crescer com o tempo;
  pertence ao Domain Service

---

### DA-07 — Fallback silencioso no REST Client

**Decisão:** se a API ViaCEP estiver indisponível, `EnderecoRestAdapter` retorna
`Endereco.vazio()` em vez de lançar exceção.

**Motivo:** o endereço é informação enriquecedora, não bloqueante. Uma instabilidade
na API de CEP não deve impedir a criação de um pedido. O log de erro garante visibilidade
sem afetar a experiência do usuário.

---

### DA-08 — `@Transactional(readOnly = true)` em consultas

**Decisão:** `ConsultarPedidoService` usa `@Transactional(readOnly = true)`.

**Motivo:** otimização que impede o Hibernate de fazer flush desnecessário, reduz
locks no banco e pode habilitar uso de réplicas de leitura se o datasource for configurado.

---

## Estrutura de Pacotes

```
src/main/java/com/empresa/pedidos/
│
├── domain/
│   └── model/
│       ├── Pedido.java
│       ├── Endereco.java
│       ├── StatusPedido.java
│       ├── PedidoDomainService.java
│       └── PedidoNaoEncontradoException.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CriarPedidoUseCase.java
│   │   │   ├── ConsultarPedidoUseCase.java
│   │   │   └── AtualizarStatusPedidoUseCase.java
│   │   └── out/
│   │       ├── PedidoRepositoryPort.java
│   │       └── EnderecoServicePort.java
│   └── usecase/
│       ├── CriarPedidoService.java
│       ├── ConsultarPedidoService.java
│       └── AtualizarStatusPedidoService.java
│
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── PedidoController.java
│   │       ├── PedidoRequest.java
│   │       ├── PedidoResponse.java
│   │       └── GlobalExceptionHandler.java
│   └── out/
│       ├── persistence/
│       │   ├── PedidoJpaAdapter.java
│       │   ├── PedidoJpaRepository.java
│       │   ├── PedidoEntity.java
│       │   └── PedidoEntityMapper.java
│       └── restclient/
│           ├── EnderecoRestAdapter.java
│           └── ViaCepResponse.java
│
├── config/
│   ├── DomainConfig.java
│   └── RestTemplateConfig.java
│
├── shared/
│   ├── ApiError.java
│   └── CepUtil.java
│
└── PedidosApplication.java
```

---

## Responsabilidade de Cada Pacote

### `domain/model/`

**O núcleo do sistema. Não importa nada de Spring, JPA ou qualquer framework externo.**

| Classe | Tipo | Responsabilidade |
|--------|------|-----------------|
| `Pedido` | Entity | Representa um pedido. Guarda o estado e executa transições com suas invariantes (`confirmar`, `cancelar`). Construtor privado — criação apenas via factory methods `criar()` e `reconstituir()`. |
| `Endereco` | Value Object | Endereço imutável. Sem identidade própria — dois endereços são iguais se todos os campos forem iguais. `equals/hashCode` baseado em valor. |
| `StatusPedido` | Enum | Estados possíveis do pedido com descrição legível. Vive no domínio porque é linguagem ubíqua do negócio. |
| `PedidoDomainService` | Domain Service | Regras de negócio que não pertencem a uma única entidade: validação de criação, limite de valor, tamanho mínimo de descrição. Classe Java pura, sem `@Service`. |
| `PedidoNaoEncontradoException` | Domain Exception | Exceção que o domínio lança quando um pedido não é encontrado. Vive no domínio porque é um conceito de negócio ("pedido não existe"), não um erro de infraestrutura. |

**Regra de ouro:** se você precisar importar `org.springframework.*` ou `jakarta.persistence.*` aqui, algo está errado.

---

### `application/port/in/`

**Contratos de entrada — define o que o sistema oferece ao mundo externo.**

Estas interfaces são os *Driving Ports*: qualquer coisa que queira usar o sistema
(um controller HTTP, um job agendado, um consumer de fila) deve passar por aqui.

| Classe | Responsabilidade |
|--------|-----------------|
| `CriarPedidoUseCase` | Contrato para criação de pedido. Contém o `Command` record com os dados de entrada. Quem chama não precisa saber como a criação funciona internamente. |
| `ConsultarPedidoUseCase` | Contrato para leitura de pedidos — busca por ID e listagem. Separado do use case de criação para respeitar a separação de comandos e queries. |
| `AtualizarStatusPedidoUseCase` | Contrato para mudança de status — confirmar e cancelar. Operações de negócio distintas expostas de forma explícita. |

**Por que interfaces e não classes concretas?**
O `PedidoController` depende de `CriarPedidoUseCase` (interface), não de
`CriarPedidoService` (implementação). Se amanhã a implementação mudar completamente,
o controller não muda uma linha.

---

### `application/port/out/`

**Contratos de saída — define o que o sistema precisa do mundo externo.**

Estas interfaces são os *Driven Ports*: o que os use cases precisam que exista
"lá fora" (banco de dados, API externa, fila, etc.).

| Classe | Responsabilidade |
|--------|-----------------|
| `PedidoRepositoryPort` | O domínio precisa salvar e buscar pedidos. Como isso é feito (JPA, JDBC, MongoDB) é irrelevante para o use case. |
| `EnderecoServicePort` | O domínio precisa buscar um endereço por CEP. A implementação pode ser ViaCEP, Correios ou qualquer outra API — o use case não sabe. |

**Regra de inversão de dependência:**
Os use cases *definem* estas interfaces. Os adapters *implementam*.
A dependência flui de fora para dentro, nunca de dentro para fora.

---

### `application/usecase/`

**Implementações dos use cases — orquestram o fluxo sem conter regra de negócio.**

| Classe | Responsabilidade |
|--------|-----------------|
| `CriarPedidoService` | Implementa `CriarPedidoUseCase`. Orquestra: (1) delega validação ao `PedidoDomainService`, (2) busca endereço via `EnderecoServicePort`, (3) solicita criação ao domínio, (4) persiste via `PedidoRepositoryPort`. |
| `ConsultarPedidoService` | Implementa `ConsultarPedidoUseCase`. Delega a busca ao repositório; lança `PedidoNaoEncontradoException` se não encontrado. `@Transactional(readOnly = true)`. |
| `AtualizarStatusPedidoService` | Implementa `AtualizarStatusPedidoUseCase`. Busca o pedido, delega a transição de estado à entidade (`pedido.confirmar()`), persiste o resultado. |

**O que NÃO deve estar aqui:**
Qualquer `if` de negócio ("se o valor for maior que X, rejeitar") pertence ao
`PedidoDomainService`. O use case apenas coordena quem faz o quê — não decide o quê.

---

### `adapter/in/web/`

**Entrada HTTP — traduz requisições HTTP em chamadas aos use cases.**

| Classe | Responsabilidade |
|--------|-----------------|
| `PedidoController` | Recebe requisições HTTP, valida formato com `@Valid`, converte `PedidoRequest` em `Command`, chama o port de entrada, converte o resultado em `PedidoResponse`. Não contém lógica de negócio. |
| `PedidoRequest` | DTO de entrada. Define e valida o formato dos dados recebidos (`@NotBlank`, `@DecimalMin`, `@Pattern`). Não é a entidade de domínio. |
| `PedidoResponse` | DTO de saída. Controla o que é exposto na API. Factory method `de(Pedido)` centraliza a conversão. Permite que a API evolua independente do domínio. |
| `GlobalExceptionHandler` | `@RestControllerAdvice`. Traduz exceções de domínio (`PedidoNaoEncontradoException` → 404) e de validação (`MethodArgumentNotValidException` → 400) em respostas HTTP padronizadas com `ApiError`. |

---

### `adapter/out/persistence/`

**Saída para banco de dados — implementa o port de repositório com JPA.**

| Classe | Responsabilidade |
|--------|-----------------|
| `PedidoJpaAdapter` | Implementa `PedidoRepositoryPort`. Traduz chamadas do domínio em operações JPA: `salvar`, `buscarPorId`, `buscarTodos`. Usa o mapper para converter entre mundos. |
| `PedidoJpaRepository` | Interface Spring Data JPA. Gerenciada pelo framework. Opera sobre `PedidoEntity`, nunca sobre `Pedido` de domínio. |
| `PedidoEntity` | Classe com anotações JPA (`@Entity`, `@Column`, etc.). Representa como o pedido é armazenado no banco. **Completamente separada da entidade de domínio** — mudanças no schema não impactam o domínio. |
| `PedidoEntityMapper` | Converte `Pedido` (domínio) ↔ `PedidoEntity` (JPA). Mantido manual para ser explícito e didático; poderia ser gerado pelo MapStruct. |

---

### `adapter/out/restclient/`

**Saída para API externa — implementa o port de endereço consumindo a ViaCEP.**

| Classe | Responsabilidade |
|--------|-----------------|
| `EnderecoRestAdapter` | Implementa `EnderecoServicePort`. Faz chamada HTTP para a ViaCEP, trata erros com fallback para `Endereco.vazio()`, loga falhas. O use case não sabe que HTTP existe. |
| `ViaCepResponse` | DTO que mapeia exatamente o JSON da API externa. Isolado aqui para que mudanças no contrato da ViaCEP não vazem para o domínio. |

---

### `config/`

**Configurações Spring — a cola entre domínio e infraestrutura.**

| Classe | Responsabilidade |
|--------|-----------------|
| `DomainConfig` | Expõe `PedidoDomainService` como `@Bean`. O Domain Service é Java puro (sem `@Service`) — o Spring só toma conhecimento dele aqui, na borda da aplicação. |
| `RestTemplateConfig` | Configura o `RestTemplate` com timeouts de conexão (3s) e leitura (5s). Evita que APIs externas lentas bloqueiem threads indefinidamente. |

---

### `shared/`

**Utilitários sem estado e sem regra de negócio — usados por múltiplas camadas.**

| Classe | Responsabilidade |
|--------|-----------------|
| `ApiError` | Envelope de erro padronizado. Todas as respostas de erro da API seguem esta estrutura (`status`, `erro`, `mensagem`, `timestamp`, `campos`). |
| `CepUtil` | Utilitário estático para limpeza e formatação de CEP. Sem Spring, sem estado. Pode ser usado por qualquer camada. |

---

## Fluxo de uma Requisição

```
POST /api/v1/pedidos
        │
        ▼
┌─────────────────────┐
│  PedidoController   │  Adapter In — recebe HTTP, valida @Valid
│  (adapter/in/web)   │  converte PedidoRequest → Command
└────────┬────────────┘
         │ chama Port In
         ▼
┌─────────────────────┐
│ CriarPedidoUseCase  │  Port In — interface (contrato)
│ (application/port)  │
└────────┬────────────┘
         │ implementado por
         ▼
┌─────────────────────┐
│ CriarPedidoService  │  Use Case — orquestra o fluxo
│ (application/usecase│  sem conter regra de negócio
└──┬──────────┬───────┘
   │          │
   │ valida   │ busca CEP
   ▼          ▼
┌──────────┐ ┌──────────────────┐
│ Pedido   │ │EnderecoServicePort│  Port Out — interface
│ Domain   │ │(application/port) │
│ Service  │ └────────┬─────────┘
└──────────┘          │ implementado por
   │ cria entidade    ▼
   ▼         ┌──────────────────┐
┌──────────┐ │EnderecoRestAdapter│  Adapter Out — chama ViaCEP
│  Pedido  │ │(adapter/out/rest) │
│ (domain) │ └──────────────────┘
└────┬─────┘
     │ persiste via
     ▼
┌─────────────────────┐
│ PedidoRepositoryPort│  Port Out — interface (contrato)
│ (application/port)  │
└────────┬────────────┘
         │ implementado por
         ▼
┌─────────────────────┐
│  PedidoJpaAdapter   │  Adapter Out — salva no PostgreSQL via JPA
│ (adapter/out/persist│
└─────────────────────┘
```

---

## Stack

| Tecnologia       | Versão  | Uso                                          |
|------------------|---------|----------------------------------------------|
| Java             | 21      | Records, pattern matching, text blocks       |
| Spring Boot      | 3.2.x   | Web, Data JPA, Validation, Actuator          |
| PostgreSQL       | 16      | Banco relacional                             |
| Flyway           | —       | Migrations versionadas (`V1__create_pedido.sql`) |
| RestTemplate     | —       | Chamadas HTTP para API externa (ViaCEP)      |
| MapStruct        | 1.5.5   | Disponível no pom.xml (mapper manual por ora)|
| Lombok           | —       | Disponível no pom.xml                        |
| H2               | —       | Banco em memória nos testes                  |
| WireMock         | 3.4.2   | Mock de APIs externas nos testes             |

---

## Endpoints

| Método  | URL                              | Descrição                                      | Status |
|---------|----------------------------------|------------------------------------------------|--------|
| POST    | `/api/v1/pedidos`                | Cria pedido. Consulta CEP na ViaCEP se fornecido. | 201  |
| GET     | `/api/v1/pedidos`                | Lista todos os pedidos                         | 200    |
| GET     | `/api/v1/pedidos/{id}`           | Busca pedido por UUID                          | 200/404|
| PATCH   | `/api/v1/pedidos/{id}/confirmar` | Confirma pedido (apenas se PENDENTE)           | 200/422|
| PATCH   | `/api/v1/pedidos/{id}/cancelar`  | Cancela pedido (se não estiver cancelado)      | 200/422|

### Exemplo de requisição

```bash
curl -X POST http://localhost:8080/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Monitor 4K",
    "valor": 3500.00,
    "cep": "01310-100"
  }'
```

### Exemplo de resposta

```json
{
  "id": "a1b2c3d4-...",
  "descricao": "Monitor 4K",
  "valor": 3500.00,
  "status": "PENDENTE",
  "statusDescricao": "Aguardando confirmação",
  "endereco": {
    "cep": "01310-100",
    "logradouro": "Avenida Paulista",
    "cidade": "São Paulo"
  },
  "criadoEm": "2024-04-09T10:30:00"
}
```

---

## Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (para o PostgreSQL)

### 1. Subir o banco

```bash
docker run \
  --name pedidos-db \
  -e POSTGRES_DB=pedidos_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### 2. Executar a aplicação

```bash
./mvnw spring-boot:run
```

### 3. Executar os testes

```bash
./mvnw test
```

---

## Referências

As decisões deste projeto foram embasadas nas seguintes fontes:

### Artigos Fundamentais

- **Hexagonal Architecture (Alistair Cockburn — original)**
  https://alistair.cockburn.us/hexagonal-architecture/
  *A fonte primária. Cockburn introduziu o padrão em 2005 com o objetivo de isolar o core da aplicação de qualquer tecnologia externa.*

- **Hexagonal Architecture with Java and Spring (Tom Hombergs — reflectoring.io)**
  https://reflectoring.io/spring-hexagonal/
  *Referência prática mais citada para implementação em Spring Boot. Define use cases como "first-class citizens" e influenciou diretamente a estrutura de `application/usecase/`.*

- **Hexagonal Architecture With Spring Boot (Arho Huttunen)**
  https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/
  *Demonstra o uso de anotação customizada `@UseCase` para evitar `@Service` no domínio — adotado neste projeto via `DomainConfig`.*

### Estrutura de Pacotes

- **Hexagonal Architecture in Spring Boot Microservices (Medium)**
  https://medium.com/@alex9954161/hexagonal-architecture-in-spring-boot-microservices-a-complete-guide-with-folder-structure-be23eb11c739
  *Referência para a estrutura `application/usecase/` + `application/port/in/` + `application/port/out/` — o padrão mais adotado no mercado em 2024/2025.*

- **Hexagonal Architecture Template (Kamil Mazurek)**
  https://kamilmazurek.pl/hexagonal-architecture-template
  *Template público de referência com separação explícita de `adapters`, `ports`, `use cases` e `domain`.*

- **Clean and Modular Java: A Hexagonal Architecture Approach (foojay.io)**
  https://foojay.io/today/clean-and-modular-java-a-hexagonal-architecture-approach/
  *Aborda o pacote `usecase` como local canônico para os application services, sem `@Service` no domínio.*

### Conceitos Relacionados

- **Clean Architecture (Robert C. Martin — Uncle Bob)**
  https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
  *A Clean Architecture e a Hexagonal compartilham o princípio de inversão de dependências. Muitos projetos mesclam as nomenclaturas (Core/Infra vs Domain/Adapters).*

- **Domain-Driven Design Reference (Eric Evans)**
  https://www.domainlanguage.com/ddd/reference/
  *Fundamenta os conceitos de Entity, Value Object e Domain Service usados neste projeto.*

- **Hexagonal Architecture with Spring Boot (Leandro Franchi — Medium)**
  https://leandrofranchi.medium.com/hexagonal-architecture-with-spring-boot-building-truly-scalable-systems-7948472406ed
  *Perspectiva de engenheiro sênior em sistemas financeiros de missão crítica, com uso de `@UseCase` customizado.*

---

*Dúvidas sobre as decisões arquiteturais? Consulte a seção [Decisões Arquiteturais](#decisoes-arquiteturais) ou abra uma discussão no repositório.*
