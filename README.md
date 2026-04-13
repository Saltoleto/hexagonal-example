# saldo-service

Microsserviço que consome mensagens de uma fila **AWS SQS** contendo dados de saldo e os persiste em um banco **MySQL**. Também expõe um endpoint **REST** para consulta dos saldos salvos.

Construído com **Arquitetura Hexagonal** (também conhecida como *Ports & Adapters*), usando **Java 17**, **Spring Boot 3** e **Maven**.

---

## Índice

1. [O que é Arquitetura Hexagonal?](#1-o-que-é-arquitetura-hexagonal)
2. [Estrutura de Pacotes](#2-estrutura-de-pacotes)
3. [Camada `core`](#3-camada-core)
4. [Camada `adapter`](#4-camada-adapter)
5. [Por que alguns Beans precisam ser criados manualmente?](#5-por-que-alguns-beans-precisam-ser-criados-manualmente)
6. [Fluxo completo de uma mensagem SQS](#6-fluxo-completo-de-uma-mensagem-sqs)
7. [Fluxo completo de uma requisição REST](#7-fluxo-completo-de-uma-requisição-rest)
8. [Endpoints disponíveis](#8-endpoints-disponíveis)
9. [Configuração e execução local](#9-configuração-e-execução-local)

---

## 1. O que é Arquitetura Hexagonal?

Imagine que o seu sistema é uma caixa fechada — um **hexágono**. Dentro dessa caixa vive toda a lógica de negócio: as regras, os cálculos, as validações. Essa parte interna **não sabe** se está sendo chamada por uma API REST, por uma fila SQS, por um teste automatizado ou por uma linha de comando. Ela também **não sabe** se os dados estão sendo salvos no MySQL, no MongoDB ou em memória.

Essa separação é o coração da arquitetura hexagonal.

```
         ┌─────────────────────────────────┐
         │                                 │
REST ───▶│  adapter/in  ───▶  CORE         │
SQS  ───▶│  adapter/in  ───▶  (negócio)  ───▶  adapter/out  ───▶  MySQL
         │                                 │
         └─────────────────────────────────┘
```

A comunicação entre o mundo externo e o core acontece através de **portas** (interfaces Java).
As **portas** definem *o que* pode ser feito. Os **adaptadores** definem *como* isso é feito tecnicamente.

### A regra de ouro das dependências

> A seta de dependência aponta sempre para dentro: `adapter → core`. Nunca o inverso.

O `core` nunca importa nada do `adapter`. Isso garante que a lógica de negócio é completamente portável e testável sem precisar subir banco de dados, fila ou servidor HTTP.

---

## 2. Estrutura de Pacotes

```
src/main/java/com/example/saldo/
│
├── SaldoServiceApplication.java              ← Ponto de entrada da aplicação
│
├── core/                                     ← Lógica de negócio pura (sem framework)
│   ├── model/
│   │   ├── Saldo.java                        ← Entidade de domínio
│   │   └── TipoSaldo.java                    ← Enum de domínio
│   ├── port/
│   │   ├── in/
│   │   │   ├── ProcessarSaldoUseCase.java    ← Porta de entrada: processar
│   │   │   └── BuscarSaldoUseCase.java       ← Porta de entrada: buscar
│   │   └── out/
│   │       └── SaldoRepositoryPort.java      ← Porta de saída: persistência
│   └── usecase/
│       ├── ProcessarSaldoUseCaseImpl.java    ← Implementação: processar saldo
│       ├── BuscarSaldoUseCaseImpl.java       ← Implementação: buscar saldo
│       └── SaldoNaoEncontradoException.java  ← Exceção de domínio
│
└── adapter/                                  ← Detalhes técnicos (Spring, SQS, MySQL)
    ├── in/
    │   ├── rest/
    │   │   ├── SaldoController.java          ← Adaptador de entrada: HTTP/REST
    │   │   ├── SaldoResponseDto.java         ← DTO de resposta da API
    │   │   └── GlobalExceptionHandler.java   ← Traduz exceções de domínio → HTTP
    │   └── sqs/
    │       ├── SaldoSqsListener.java         ← Adaptador de entrada: AWS SQS
    │       └── SaldoMensagemDto.java         ← DTO da mensagem SQS
    ├── out/
    │   └── persistence/
    │       ├── SaldoPersistenceAdapter.java  ← Adaptador de saída: MySQL
    │       ├── entity/
    │       │   └── SaldoEntity.java          ← Entidade JPA (detalhe de infra)
    │       ├── mapper/
    │       │   └── SaldoPersistenceMapper.java  ← Converte domínio ↔ JPA
    │       └── repository/
    │           └── SaldoJpaRepository.java   ← Interface Spring Data JPA
    └── config/
        ├── ApplicationConfig.java            ← Registra os Beans do core no Spring
        └── SqsConfig.java                    ← Configuração do cliente AWS SQS
```

---

## 3. Camada `core`

Esta é a camada mais importante do projeto. Ela contém **toda a lógica de negócio** e não possui nenhuma dependência de framework externo como Spring, JPA ou AWS SDK.

> **Regra:** nenhum arquivo dentro de `core` pode ter `import org.springframework.*`.
> A única exceção tolerada é `jakarta.transaction.Transactional`, que expressa uma
> intenção de negócio (atomicidade) e não um detalhe de infraestrutura.

### 3.1 `model`

Os modelos são as **entidades de domínio** — os objetos que representam os conceitos do negócio.

---

#### `Saldo.java`

Representa um registro de saldo recebido. É um **POJO puro** (Plain Old Java Object): sem anotações JPA, sem anotações Spring, sem herança de frameworks.

```java
public class Saldo {
    private Long id;
    private String contaId;
    private BigDecimal valor;
    private String moeda;
    private TipoSaldo tipo;
    private LocalDateTime dataReferencia;
    private LocalDateTime dataProcessamento;

    // Regra de negócio: um saldo só é válido se tiver conta, valor, moeda e tipo
    public boolean isValido() { ... }
}
```

> **Por que não usar `@Entity` aqui?**
> `@Entity` é uma anotação JPA — um detalhe de como os dados são salvos no banco.
> O domínio não deveria saber que existe um banco de dados. Por isso existe a
> `SaldoEntity.java` separada no `adapter`, que é quem carrega as anotações JPA.

---

#### `TipoSaldo.java`

Enum que define os tipos válidos de saldo no domínio do negócio.

```java
public enum TipoSaldo {
    CREDITO, DEBITO, DISPONIVEL, BLOQUEADO
}
```

---

### 3.2 `port/in` — Portas de Entrada

As portas de entrada são **interfaces Java** que definem o que o sistema consegue fazer. Elas são o contrato que os adaptadores de entrada (REST, SQS) devem chamar para acionar a lógica de negócio.

Pense nas portas de entrada como o **cardápio de um restaurante**: ele lista o que você pode pedir, mas não explica como a cozinha vai preparar.

---

#### `ProcessarSaldoUseCase.java`

Define o contrato para processar (salvar) um saldo recebido.

```java
public interface ProcessarSaldoUseCase {
    Saldo processar(Saldo saldo);
}
```

---

#### `BuscarSaldoUseCase.java`

Define o contrato para consultar saldos já salvos.

```java
public interface BuscarSaldoUseCase {
    Saldo buscarPorId(Long id);
    List<Saldo> listarPorContaId(String contaId);
}
```

---

### 3.3 `port/out` — Portas de Saída

As portas de saída são **interfaces Java** que definem o que o sistema *precisa* do mundo externo. Elas são o contrato que os adaptadores de saída (banco de dados, APIs externas) devem implementar.

Pense nas portas de saída como uma **lista de requisitos da cozinha**: "preciso de ingredientes frescos" — não importa de qual fornecedor.

---

#### `SaldoRepositoryPort.java`

Define o que o core precisa em termos de persistência. O core não sabe que existe MySQL — ele só sabe que existe alguém capaz de salvar e buscar saldos.

```java
public interface SaldoRepositoryPort {
    Saldo salvar(Saldo saldo);
    Optional<Saldo> buscarPorId(Long id);
    List<Saldo> listarPorContaId(String contaId);
}
```

---

### 3.4 `usecase`

Os casos de uso são as **implementações das portas de entrada**. Eles orquestram o fluxo de uma operação: chamam as regras do domínio, usam as portas de saída quando necessário e coordenam o resultado.

> **Importante:** os casos de uso são **POJOs puros** — sem `@Service` ou qualquer
> anotação Spring. Eles são registrados no Spring manualmente via `ApplicationConfig`.
> Veja o [capítulo 5](#5-por-que-alguns-beans-precisam-ser-criados-manualmente) para entender o porquê.

---

#### `ProcessarSaldoUseCaseImpl.java`

Implementa `ProcessarSaldoUseCase`. Valida o saldo recebido usando a regra de domínio (`saldo.isValido()`) e delega a persistência à porta de saída.

```java
@Transactional  // jakarta.transaction — não é Spring
public Saldo processar(Saldo saldo) {
    if (!saldo.isValido()) {
        throw new IllegalArgumentException("Saldo inválido...");
    }
    return saldoRepositoryPort.salvar(saldo);
}
```

> **Por que `@Transactional` do Jakarta e não do Spring?**
> `@Transactional` expressa uma regra de negócio: "esta operação é atômica — ou tudo
> acontece, ou nada acontece". Isso é uma intenção de domínio, não um detalhe técnico.
> Usar `jakarta.transaction.Transactional` (ao invés de `org.springframework...`)
> mantém o core independente do Spring. Se a aplicação migrar para Quarkus ou Micronaut,
> esta classe não precisa mudar — ambos os frameworks também honram a anotação Jakarta.

---

#### `BuscarSaldoUseCaseImpl.java`

Implementa `BuscarSaldoUseCase`. Consulta saldos via porta de saída e lança exceção de domínio quando não encontrado.

```java
public Saldo buscarPorId(Long id) {
    return saldoRepositoryPort.buscarPorId(id)
        .orElseThrow(() -> new SaldoNaoEncontradoException("Saldo não encontrado para id=" + id));
}
```

---

#### `SaldoNaoEncontradoException.java`

Exceção de domínio — representa uma situação de negócio (saldo inexistente). Não tem nenhuma anotação HTTP como `@ResponseStatus`. Quem decide que isso vira um `404` é o `GlobalExceptionHandler` no `adapter`.

```java
public class SaldoNaoEncontradoException extends RuntimeException {
    public SaldoNaoEncontradoException(String message) {
        super(message);
    }
}
```

> **Por que não colocar `@ResponseStatus(HttpStatus.NOT_FOUND)` aqui?**
> Porque o domínio não conhece HTTP. Se amanhã esta exceção for lançada num contexto
> de processamento de fila (sem HTTP), a anotação não faria sentido algum.
> A decisão do status HTTP pertence ao adaptador REST.

---

## 4. Camada `adapter`

Esta camada contém todos os **detalhes técnicos** de como o sistema se comunica com o mundo externo. Aqui moram as anotações Spring, JPA, AWS SDK e tudo que o core deliberadamente ignora.

> **Regra de ouro:** um adaptador **nunca contém lógica de negócio**.
> Ele recebe, converte e delega. Nada mais.

### 4.1 `adapter/in/rest` — Adaptador REST

---

#### `SaldoController.java`

Adaptador de entrada HTTP. Recebe requisições REST e delega ao `BuscarSaldoUseCase`.

```java
@RestController
@RequestMapping("/saldos")
public class SaldoController {

    private final BuscarSaldoUseCase buscarSaldoUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<SaldoResponseDto> buscarPorId(@PathVariable Long id) {
        SaldoResponseDto response = SaldoResponseDto.from(buscarSaldoUseCase.buscarPorId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SaldoResponseDto>> listarPorContaId(@RequestParam String contaId) {
        ...
    }
}
```

Repare que o controller conhece apenas a **interface** `BuscarSaldoUseCase` — nunca a
implementação concreta `BuscarSaldoUseCaseImpl`. Isso é injeção de dependência pela porta.

---

#### `SaldoResponseDto.java`

DTO (Data Transfer Object) de resposta da API REST. Existe para **isolar o contrato da API do modelo de domínio**. Se o domínio mudar internamente, o contrato da API pode permanecer estável — e vice-versa.

```java
public class SaldoResponseDto {
    // Campos com @JsonProperty para controlar o nome no JSON
    // Método estático from(Saldo) para converter domínio → DTO
    public static SaldoResponseDto from(Saldo saldo) { ... }
}
```

---

#### `GlobalExceptionHandler.java`

Intercepta exceções lançadas pelo core e as traduz para respostas HTTP apropriadas.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoNaoEncontradoException.class)
    public ResponseEntity<...> handleNaoEncontrado(SaldoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(...);
    }
}
```

| Exceção de domínio | HTTP status |
|---|---|
| `SaldoNaoEncontradoException` | `404 Not Found` |
| `IllegalArgumentException` | `400 Bad Request` |

---

### 4.2 `adapter/in/sqs` — Adaptador SQS

---

#### `SaldoSqsListener.java`

Adaptador de entrada AWS SQS. Escuta a fila, desserializa o JSON, converte para entidade de domínio e invoca `ProcessarSaldoUseCase`.

```java
@Component
public class SaldoSqsListener {

    @SqsListener("${app.sqs.queue-name}")
    public void onMessage(@Payload SaldoMensagemDto mensagem) {
        Saldo saldo = toDomain(mensagem);               // converte DTO → domínio
        processarSaldoUseCase.processar(saldo);         // delega ao core
    }
}
```

---

#### `SaldoMensagemDto.java`

DTO que representa o payload JSON esperado na fila SQS.

```json
{
  "conta_id": "CC-12345",
  "valor": 1500.00,
  "moeda": "BRL",
  "tipo": "CREDITO",
  "data_referencia": "2024-04-01T10:00:00"
}
```

---

### 4.3 `adapter/out/persistence` — Adaptador de Persistência

---

#### `SaldoPersistenceAdapter.java`

Implementa a porta de saída `SaldoRepositoryPort` usando JPA e MySQL. É aqui que o contrato do domínio se conecta à tecnologia real de banco de dados.

```java
@Component
public class SaldoPersistenceAdapter implements SaldoRepositoryPort {

    @Override
    public Saldo salvar(Saldo saldo) {
        SaldoEntity entity = mapper.toEntity(saldo);    // domínio → JPA
        SaldoEntity saved  = jpaRepository.save(entity);
        return mapper.toDomain(saved);                  // JPA → domínio
    }
}
```

Como esta classe tem `@Component` e implementa `SaldoRepositoryPort`, o Spring a
registra automaticamente. Quando alguém pede uma injeção de `SaldoRepositoryPort`,
o Spring entrega esta classe — sem necessidade de `@Bean` manual.

#### Por que usar `implements` e não um `@Bean` manual?

Seria tecnicamente possível remover o `implements` e registrar o Bean manualmente no
`ApplicationConfig`, delegando as chamadas à mão:

```java
// Alternativa — funcionaria, mas não é o que fazemos
@Bean
public SaldoRepositoryPort saldoRepositoryPort(SaldoPersistenceAdapter adapter) {
    return new SaldoRepositoryPort() {
        public Saldo salvar(Saldo saldo) { return adapter.salvar(saldo); }
        public Optional<Saldo> buscarPorId(Long id) { return adapter.buscarPorId(id); }
        public List<Saldo> listarPorContaId(String contaId) { return adapter.listarPorContaId(contaId); }
    };
}
```

Optamos pelo `implements` por dois motivos concretos:

**1. Segurança em tempo de compilação.** Com `implements SaldoRepositoryPort`, o compilador
garante que todos os métodos da interface estão implementados. Se você adicionar um novo
método em `SaldoRepositoryPort` e esquecer de implementar no adapter, o build quebra
imediatamente com erro claro. Sem o `implements`, esse erro só apareceria em runtime —
muito mais difícil de rastrear.

**2. Documentação viva no código.** Ao abrir `SaldoPersistenceAdapter` e ver
`implements SaldoRepositoryPort`, qualquer desenvolvedor entende instantaneamente o papel
desta classe: ela é a implementação concreta de uma porta do domínio. Sem o `implements`,
essa informação estaria escondida dentro do `ApplicationConfig`, longe de quem está
lendo o adapter.

A regra prática do projeto é simples:

> **Adaptadores de saída sempre usam `implements` + `@Component`.**
> `@Bean` manual é reservado para as implementações de casos de uso que vivem no `core`
> e não podem ter anotações Spring.

---

#### `SaldoEntity.java`

Entidade JPA que representa a tabela `saldos` no banco de dados. Existe separada do `Saldo.java` do domínio para que as anotações `@Entity`, `@Table`, `@Column` não poluam o modelo de negócio.

---

#### `SaldoPersistenceMapper.java`

Responsável por converter entre `Saldo` (domínio) e `SaldoEntity` (JPA). É a ponte entre as duas representações — garante que nenhuma das duas precisa conhecer a outra diretamente.

---

#### `SaldoJpaRepository.java`

Interface Spring Data JPA. O Spring gera a implementação automaticamente em tempo de execução.

```java
public interface SaldoJpaRepository extends JpaRepository<SaldoEntity, Long> {
    List<SaldoEntity> findByContaId(String contaId);
}
```

---

### 4.4 `adapter/config` — Configuração

---

#### `ApplicationConfig.java`

Esta é a classe mais estratégica do projeto do ponto de vista arquitetural. É a **cola** entre o core (POJOs sem Spring) e o container de injeção de dependência do Spring. Veja o capítulo 5 para o raciocínio completo.

```java
@Configuration
public class ApplicationConfig {

    @Bean
    public ProcessarSaldoUseCase processarSaldoUseCase(SaldoRepositoryPort saldoRepositoryPort) {
        return new ProcessarSaldoUseCaseImpl(saldoRepositoryPort);
    }

    @Bean
    public BuscarSaldoUseCase buscarSaldoUseCase(SaldoRepositoryPort saldoRepositoryPort) {
        return new BuscarSaldoUseCaseImpl(saldoRepositoryPort);
    }
}
```

---

#### `SqsConfig.java`

Configura o cliente AWS SQS. Suporta `endpoint-override` para uso com **LocalStack** em ambiente local, sem precisar de conta AWS real.

---

## 5. Por que alguns Beans precisam ser criados manualmente?

Esta é uma das dúvidas mais comuns ao trabalhar com arquitetura hexagonal no Spring. A resposta está em entender como o Spring descobre e registra seus Beans.

### Como o Spring registra Beans automaticamente

O Spring percorre os pacotes da aplicação procurando classes com anotações estereótipo:
`@Component`, `@Service`, `@Repository`, `@RestController`. Quando encontra uma, instancia
e registra no contexto — isso é o **component scan**.

### O problema: o core não tem anotações Spring

Por decisão arquitetural, as classes do `core` não podem ter `@Service` ou `@Component`.
Se tivessem, o core passaria a depender do Spring — e toda a portabilidade da lógica de
negócio seria perdida. Se amanhã o projeto migrar para Quarkus, cada `@Service` no core
seria um ponto de mudança desnecessário.

Sem essas anotações, o Spring não enxerga as implementações dos casos de uso durante o component scan.

### A solução: `@Bean` manual no `ApplicationConfig`

O `ApplicationConfig` fica no `adapter` — que *pode* conhecer o Spring. Ele age como
intermediário: instancia os POJOs do core e os registra no contexto Spring manualmente.

```
Spring não enxerga ProcessarSaldoUseCaseImpl (sem @Component)
        ↓
ApplicationConfig (no adapter, conhece Spring) instancia e registra o Bean
        ↓
Spring passa a gerenciar ProcessarSaldoUseCaseImpl como Bean normalmente
        ↓
SaldoSqsListener recebe ProcessarSaldoUseCase por injeção de dependência
```

### Por que os adaptadores de saída não precisam disso?

Porque eles *podem* ter `@Component` — vivem no `adapter`, não no `core`.

```java
@Component  // Spring encontra automaticamente via component scan
public class SaldoPersistenceAdapter implements SaldoRepositoryPort { ... }
```

Quando o Spring encontra `SaldoPersistenceAdapter`, ele a registra como Bean tanto pelo
nome da classe quanto pela interface que ela implementa (`SaldoRepositoryPort`). Então,
quando qualquer classe pedir uma injeção de `SaldoRepositoryPort`, o Spring sabe exatamente
o que entregar.

### Tabela resumo

| Classe | Onde vive | Tem `@Component`? | Precisa de `@Bean` manual? | Motivo |
|---|---|---|---|---|
| `ProcessarSaldoUseCaseImpl` | `core` | ❌ Não | ✅ Sim | POJO puro — core não conhece Spring |
| `BuscarSaldoUseCaseImpl` | `core` | ❌ Não | ✅ Sim | POJO puro — core não conhece Spring |
| `SaldoPersistenceAdapter` | `adapter` | ✅ Sim | ❌ Não | Adapter pode ter `@Component` |
| `SaldoSqsListener` | `adapter` | ✅ Sim | ❌ Não | Adapter pode ter `@Component` |
| `SaldoController` | `adapter` | ✅ Sim (`@RestController`) | ❌ Não | Adapter pode ter `@RestController` |

### Atenção: a regra não é "adapter in precisa, adapter out não precisa"

Pode parecer que adaptadores de entrada sempre precisam de `@Bean` manual e os de saída
não. Mas isso não é uma regra — é uma coincidência do projeto atual.

O que realmente determina a necessidade do `@Bean` manual é **onde vive a classe concreta**:

- Se vive no `adapter` e tem `@Component` → Spring registra automaticamente
- Se vive no `core` e é um POJO puro → precisa de `@Bean` manual

Um contra-exemplo: se você tivesse **duas implementações** de `SaldoRepositoryPort` —
por exemplo `MySQLSaldoAdapter` e `MongoSaldoAdapter` — ambas com `@Component`, o Spring
não saberia qual injetar e lançaria erro. Nesse caso, mesmo sendo adapters de saída,
você precisaria de um `@Bean` manual com qualificação explícita.

---

## 6. Fluxo completo de uma mensagem SQS

```
[Fila SQS]
    │  Mensagem JSON: { "conta_id": "CC-001", "valor": 500.00, ... }
    ▼
[SaldoSqsListener]                          adapter/in/sqs
    │  1. Recebe o payload como SaldoMensagemDto
    │  2. Converte DTO → Saldo (entidade de domínio)
    │  3. Chama processarSaldoUseCase.processar(saldo)
    ▼
[ProcessarSaldoUseCaseImpl]                 core/usecase
    │  4. Valida: saldo.isValido()
    │  5. Se inválido: lança IllegalArgumentException
    │  6. Se válido: chama saldoRepositoryPort.salvar(saldo)
    ▼
[SaldoPersistenceAdapter]                   adapter/out/persistence
    │  7. Converte Saldo → SaldoEntity
    │  8. Chama jpaRepository.save(entity)
    │  9. Converte SaldoEntity salvo → Saldo e retorna
    ▼
[MySQL — tabela saldos]
```

---

## 7. Fluxo completo de uma requisição REST

```
[Cliente HTTP]
    │  GET /saldos/42
    ▼
[SaldoController]                           adapter/in/rest
    │  1. Recebe id=42 via @PathVariable
    │  2. Chama buscarSaldoUseCase.buscarPorId(42)
    ▼
[BuscarSaldoUseCaseImpl]                    core/usecase
    │  3. Chama saldoRepositoryPort.buscarPorId(42)
    ▼
[SaldoPersistenceAdapter]                   adapter/out/persistence
    │  4. Chama jpaRepository.findById(42)
    │  5. Se não encontrado: retorna Optional.empty()
    │  6. Se encontrado: converte SaldoEntity → Saldo e retorna
    ▼
[BuscarSaldoUseCaseImpl]
    │  7. Se Optional.empty(): lança SaldoNaoEncontradoException
    │  8. Se encontrado: retorna Saldo para o controller
    ▼
[SaldoController]
    │  9. Converte Saldo → SaldoResponseDto
    │  10. Retorna ResponseEntity.ok(dto)
    ▼
[Cliente HTTP] ←── 200 OK com JSON

─── Em caso de erro ───────────────────────────────────────

[GlobalExceptionHandler]                    adapter/in/rest
    │  Intercepta SaldoNaoEncontradoException lançada no core
    │  O core não sabe nada de HTTP — apenas lança a exceção semântica
    │  O handler decide que isso vira 404
    ▼
[Cliente HTTP] ←── 404 Not Found com JSON de erro
```

---

## 8. Endpoints disponíveis

### `GET /saldos/{id}`

Busca um saldo pelo ID.

**Exemplo de requisição:**
```
GET /saldos/1
```

**Resposta de sucesso — 200 OK:**
```json
{
  "id": 1,
  "conta_id": "CC-12345",
  "valor": 1500.00,
  "moeda": "BRL",
  "tipo": "CREDITO",
  "data_referencia": "2024-04-01T10:00:00",
  "data_processamento": "2024-04-01T10:05:32"
}
```

**Resposta de erro — 404 Not Found:**
```json
{
  "status": 404,
  "erro": "Não encontrado",
  "mensagem": "Saldo não encontrado para id=1",
  "timestamp": "2024-04-01T10:05:32"
}
```

---

### `GET /saldos?contaId={contaId}`

Lista todos os saldos de uma conta.

**Exemplo de requisição:**
```
GET /saldos?contaId=CC-12345
```

**Resposta de sucesso — 200 OK:**
```json
[
  {
    "id": 1,
    "conta_id": "CC-12345",
    "valor": 1500.00,
    "moeda": "BRL",
    "tipo": "CREDITO",
    "data_referencia": "2024-04-01T10:00:00",
    "data_processamento": "2024-04-01T10:05:32"
  }
]
```

---

## 9. Configuração e execução local

### Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do MySQL |
| `DB_PORT` | `3306` | Porta do MySQL |
| `DB_NAME` | `saldos_db` | Nome do banco |
| `DB_USER` | `root` | Usuário do banco |
| `DB_PASS` | `root` | Senha do banco |
| `AWS_REGION` | `us-east-1` | Região AWS |
| `AWS_ACCESS_KEY_ID` | `local` | Access key AWS |
| `AWS_SECRET_ACCESS_KEY` | `local` | Secret key AWS |
| `AWS_ENDPOINT_OVERRIDE` | _(vazio)_ | Para LocalStack: `http://localhost:4566` |
| `SQS_QUEUE_NAME` | `saldos-queue` | Nome da fila SQS |

### Executando localmente com LocalStack e Docker

```bash
# 1. Sobe o LocalStack (simula AWS localmente)
docker run -d -p 4566:4566 localstack/localstack

# 2. Cria a fila SQS no LocalStack
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name saldos-queue --region us-east-1

# 3. Sobe o MySQL
docker run -d \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=saldos_db \
  mysql:8

# 4. Cria a tabela no banco
mysql -h localhost -u root -proot saldos_db < src/main/resources/schema.sql

# 5. Sobe a aplicação apontando para o LocalStack
export AWS_ENDPOINT_OVERRIDE=http://localhost:4566
mvn spring-boot:run
```

### Build e testes

```bash
mvn clean test       # executa os testes unitários
mvn clean package    # gera o JAR em target/
```

### Enviando uma mensagem de teste para a fila

```bash
aws --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url http://localhost:4566/000000000000/saldos-queue \
  --message-body '{
    "conta_id": "CC-001",
    "valor": 1500.00,
    "moeda": "BRL",
    "tipo": "CREDITO",
    "data_referencia": "2024-04-01T10:00:00"
  }'
```

Após enviar, consulte o saldo salvo via REST:

```bash
# Listar por conta
curl http://localhost:8080/saldos?contaId=CC-001

# Buscar por ID
curl http://localhost:8080/saldos/1
```
