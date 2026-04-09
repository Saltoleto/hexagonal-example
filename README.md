# Pedidos — Demo Arquitetura Hexagonal

Projeto demonstrativo de **Arquitetura Hexagonal (Ports & Adapters)** com Java 21 e Spring Boot 3.

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Records, sealed classes, pattern matching |
| Spring Boot 3.2 | Web, Data JPA, Validation, Actuator |
| PostgreSQL | Banco relacional |
| Flyway | Migrations versionadas |
| RestTemplate | Chamadas HTTP para API externa (ViaCEP) |
| MapStruct + Lombok | Disponíveis no pom.xml |
| H2 + WireMock | Testes |

## Estrutura

```
src/main/java/com/empresa/pedidos/
│
├── domain/                        ← NÚCLEO — zero dependências externas
│   ├── model/
│   │   ├── Pedido.java            Entity com comportamento
│   │   ├── Endereco.java          Value Object imutável
│   │   ├── StatusPedido.java      Enum de domínio
│   │   └── PedidoNaoEncontradoException.java
│   └── port/
│       ├── in/                    ← Driving ports (o que o exterior pode pedir)
│       │   ├── CriarPedidoUseCase.java
│       │   ├── ConsultarPedidoUseCase.java
│       │   └── AtualizarStatusPedidoUseCase.java
│       └── out/                   ← Driven ports (o que o domínio precisa de fora)
│           ├── PedidoRepositoryPort.java
│           └── EnderecoServicePort.java
│
├── application/                   ← Orquestração, @Service, @Transactional
│   ├── CriarPedidoService.java
│   ├── ConsultarPedidoService.java
│   └── AtualizarStatusPedidoService.java
│
├── adapter/
│   ├── in/
│   │   └── web/                   ← Entrada HTTP
│   │       ├── PedidoController.java
│   │       ├── PedidoRequest.java  DTO de entrada
│   │       ├── PedidoResponse.java DTO de saída
│   │       └── GlobalExceptionHandler.java
│   └── out/
│       ├── persistence/           ← Banco de dados (JPA)
│       │   ├── PedidoJpaAdapter.java    implementa PedidoRepositoryPort
│       │   ├── PedidoJpaRepository.java Spring Data
│       │   ├── PedidoEntity.java        @Entity separada do domínio
│       │   └── PedidoEntityMapper.java  Entity <-> Domain
│       └── restclient/            ← API externa (ViaCEP)
│           ├── EnderecoRestAdapter.java  implementa EnderecoServicePort
│           └── ViaCepResponse.java      DTO da API externa
│
├── config/
│   └── RestTemplateConfig.java    ← Bean com timeout configurado
│
├── shared/
│   ├── ApiError.java              ← Envelope de erro padronizado
│   └── CepUtil.java               ← Utilitário sem estado
│
└── PedidosApplication.java
```

## Endpoints

| Método | URL | Descrição |
|---|---|---|
| POST | `/api/v1/pedidos` | Cria pedido (consulta CEP na ViaCEP) |
| GET | `/api/v1/pedidos` | Lista todos os pedidos |
| GET | `/api/v1/pedidos/{id}` | Busca pedido por ID |
| PATCH | `/api/v1/pedidos/{id}/confirmar` | Confirma pedido |
| PATCH | `/api/v1/pedidos/{id}/cancelar` | Cancela pedido |

## Exemplo de requisição

```bash
curl -X POST http://localhost:8080/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Monitor 4K",
    "valor": 3500.00,
    "cep": "01310-100"
  }'
```

## Como executar

1. Suba o PostgreSQL:
```bash
docker run -e POSTGRES_DB=pedidos_db -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16
```

2. Execute:
```bash
./mvnw spring-boot:run
```

## Regras da arquitetura

- **Domain** não importa nada de Spring, JPA ou HTTP
- `PedidoEntity` ≠ `Pedido` — entidade JPA é detalhe de infraestrutura
- Adapters conhecem Ports; Ports não conhecem Adapters
- Um UseCase por operação (sem serviços com 15 métodos)
- Exceções de domínio vivem no domain; são traduzidas para HTTP no `GlobalExceptionHandler`
