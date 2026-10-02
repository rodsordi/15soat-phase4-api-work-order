# Microsserviço de Ordens de Serviço (`15soat-phase4-api-work-order`)

Microsserviço responsável pela gestão completa do ciclo de vida das Ordens de Serviço (OS) da oficina mecânica, desenvolvido para o Tech Challenge da Fase 4 da pós-graduação em Software Architecture (SOAT) da FIAP.

## Destaques Arquiteturais

- **Topologia Multi-Módulos (Arquitetura Hexagonal / Ports & Adapters)**:
  - `domain`: Regras puras de negócio, agregados, entidades, *Value Objects*, portas e casos de uso. Isolado e validado rigidamente pela suíte de testes de arquitetura com **ArchUnit**.
  - `application`: Executável Spring Boot 4.0.7, controllers REST, adaptadores de persistência relacional com Spring Data JPA e PostgreSQL, migrations versionadas via **Flyway**, além de produtores e consumidores de eventos Kafka.
- **Design API First**: As interfaces dos controllers (`WorkOrderApi`) são geradas automaticamente em tempo de compilação a partir da especificação `contracts/openapi.yaml` via `openapi-generator-maven-plugin`.
- **Arquitetura Orientada a Eventos Assíncronos**: Contratos de eventos Kafka definidos formalmente em `contracts/asyncapi.yaml`:
  - Emite `WorkOrderApprovedEvent` para iniciar a Saga distribuída de faturamento e produção.
  - Consome `PaymentFailedEvent` para disparar o rollback compensatório seguro da OS.
  - Consome `ExecutionCompletedEvent` para realizar o fechamento e finalização do serviço.
- **Documentação Viva com Swagger UI**: Acesso direto e interativo à documentação da API em `/swagger-ui.html` e `/v3/api-docs`.
- **Padrão Database-per-Service**: Banco de dados relacional dedicado **PostgreSQL** (`work_order_db`) com histórico auditável de migrations Flyway.
- **Testes Realistas com Testcontainers**: Zero dependência de bancos em memória voláteis (H2). Todos os testes de integração sobem instâncias reais de PostgreSQL e Apache Kafka via containers Docker.
- **Suporte a GraalVM Native Image**: Configuração de compilação AOT nativa declarada em Java puro, garantindo subida instantânea e pegada mínima de memória.

## Modelo de Dados & Diagrama Entidade-Relacionamento (MER)

O modelo de dados relacional é persistido no banco PostgreSQL (`work_order_db`) gerenciado via migrations Flyway:

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 800px;">

```mermaid
erDiagram
    %% Nível 1: Cadastro Central (Cliente e Veículo)
    CUSTOMER {
        uuid id PK "Identificador único do Cliente"
        string name "Nome completo do cliente"
        string email UK "E-mail único"
        string document UK "CPF ou CNPJ (Value Object)"
        timestamp created_at "Data de cadastro"
        timestamp updated_at "Última atualização"
        bigint version "Controle de concorrência otimista"
    }

    VEHICLE {
        uuid id PK "Identificador único do Veículo"
        uuid customer_id FK "FK referenciando CUSTOMER(id)"
        string license_plate UK "Placa do Veículo (Value Object)"
        string make "Marca / Fabricante (ex: Toyota)"
        string model "Modelo do veículo (ex: Corolla)"
        integer manufacture_year "Ano de fabricação"
        timestamp created_at "Data de cadastro"
        timestamp updated_at "Última atualização"
        bigint version "Controle de concorrência otimista"
    }

    CUSTOMER ||--o{ VEHICLE : "possui"

    %% Nível 2: Ordem de Serviço
    WORK_ORDER {
        uuid id PK "Identificador único da Ordem de Serviço"
        uuid customer_id FK "FK referenciando CUSTOMER(id)"
        uuid vehicle_id FK "FK referenciando VEHICLE(id)"
        string customer_document "Snapshot do CPF/CNPJ"
        string license_plate "Snapshot da Placa"
        string description "Descrição do problema / solicitação"
        string status "Status (RECEIVED, DIAGNOSING, APPROVED, FINISHED, etc.)"
        numeric total_amount "Valor total consolidado (Serviços + Materiais)"
        timestamp created_at "Data de abertura"
        timestamp updated_at "Última atualização"
        bigint version "Controle de concorrência otimista"
    }

    CUSTOMER ||--o{ WORK_ORDER : "solicita"
    VEHICLE ||--o{ WORK_ORDER : "atendido_em"

    %% Nível 3: Tabelas Associativas de Itens (Camada Intermediária com FKs)
    WORK_ORDER_SERVICE {
        uuid id PK "Chave Primária"
        uuid work_order_id FK "FK referenciando WORK_ORDER(id)"
        uuid service_id FK "FK referenciando SERVICE(id)"
        numeric unit_price "Preço unitário congelado no momento da inclusão"
        integer quantity "Quantidade de execuções do serviço"
        timestamp created_at "Data de inclusão na OS"
    }

    WORK_ORDER_MATERIAL {
        uuid id PK "Chave Primária"
        uuid work_order_id FK "FK referenciando WORK_ORDER(id)"
        uuid material_id FK "FK referenciando MATERIAL(id)"
        numeric unit_price "Preço unitário congelado no momento da inclusão"
        integer quantity "Quantidade de itens consumidos"
        timestamp created_at "Data de inclusão na OS"
    }

    %% Nível 4: Catálogos de Itens (Base)
    SERVICE {
        uuid id PK "Chave Primária"
        string code UK "Código padronizado do serviço (ex: SRV-OIL-01)"
        string name "Nome do serviço"
        string description "Descrição técnica do procedimento"
        numeric price "Preço de tabela da mão de obra"
        bigint estimated_minutes "Tempo estimado em minutos"
        timestamp created_at "Data de cadastro"
        timestamp updated_at "Última atualização"
        bigint version "Controle de concorrência otimista"
    }

    MATERIAL {
        uuid id PK "Chave Primária"
        string sku UK "Código de Estoque / Peça (ex: PART-BRK-01)"
        string name "Nome comercial da peça / insumo"
        string description "Especificação técnica"
        numeric unit_cost "Custo de aquisição"
        numeric unit_price "Preço de venda na OS"
        integer stock_quantity "Quantidade física em estoque"
        integer min_stock "Estoque mínimo para reposição"
        timestamp created_at "Data de cadastro"
        timestamp updated_at "Última atualização"
        bigint version "Controle de concorrência otimista"
    }

    %% Relações Topo -> Meio -> Base (Dagre Top-to-Bottom Flow)
    WORK_ORDER ||--o{ WORK_ORDER_SERVICE : "possui"
    WORK_ORDER ||--o{ WORK_ORDER_MATERIAL : "possui"
    WORK_ORDER_SERVICE }o--|| SERVICE : "referencia_catalogo"
    WORK_ORDER_MATERIAL }o--|| MATERIAL : "referencia_catalogo"
```

</div>
</div>

## Execução Local

### 1. Inicializar Infraestrutura Local (PostgreSQL e Kafka)

```bash
docker compose up -d
```

### 2. Compilar e Executar a Aplicação

```bash
mvn clean package
java -jar application/target/work-order-application-0.0.1-SNAPSHOT.jar
```

Acesse a documentação interativa: `http://localhost:8080/swagger-ui.html`

## 🏛️ Architecture Decision Records (ADRs)

- **[Catálogo de ADRs do Microsserviço](docs/adr/README.md)**: Decisões específicas de Ordem de Serviço (Gestão Mestre de Clientes e Veículos, Modelagem Relacional de Itens com Snapshot de Preços).
- **[Catálogo Global de ADRs (lib-commons)](../15soat-phase4-lib-commons/docs/adr/README.md)**: Padrões transversais compartilhados (Database-per-Service, Saga Kafka, Pragmatic DDD, Tabelas no Singular, Java 25 / Spring Boot 4).
