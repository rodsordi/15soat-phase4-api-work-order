# Plano de Implementação: Módulo de Testes de Integração (iandt) no api-work-order

> **Decisão do Usuário**: Opção 1 - Criação do Submódulo Maven Dedicado iandt (work-order-iandt) no 15soat-phase4-api-work-order
> **Status**: Aprovado para Execução

---

## 1. Objetivo
Implementar o submódulo Maven físico `work-order-iandt` (`iandt`) no microsserviço `15soat-phase4-api-work-order`, reutilizando as fixtures de Testcontainers do `mod-integration-test` (`PostgresSetup` e `KafkaSetup`) do `15soat-phase4-lib-commons`, garantindo o isolamento de testes unitários rápidos e cobertura dos cenários críticos de ponta a ponta migrados do E2E.

---

## 2. Escopo Técnico da Implementação

### 2.1. Configuração Maven do Parent (`15soat-phase4-api-work-order/pom.xml`)
- Adicionar o submódulo `<module>iandt</module>` à lista de `<modules>`.
- Adicionar `work-order-iandt` ao bloco `<dependencyManagement>`.
- Garantir a presença de `mod-integration-test` no `<dependencyManagement>` com `<scope>test</scope>`.

### 2.2. Criação do Módulo `15soat-phase4-api-work-order/iandt`
- Criar `iandt/pom.xml`:
  - `artifactId`: `work-order-iandt`
  - Dependências: `work-order-application`, `work-order-domain`, `mod-integration-test`, `spring-boot-starter-test`, `rest-assured`, `awaitility`.
  - Configurar perfil Maven `integrationTests` com `<skipTests>true</skipTests>` por padrão (Graceful Degradation conforme ADR 0006).

### 2.3. Arquivo de Configuração de Teste (`iandt/src/test/resources/application-test.yml`)
- Configurar contexto Spring de teste:
  - `spring.jpa.hibernate.ddl-auto: validate`
  - `spring.jpa.open-in-view: false`
  - `spring.flyway.enabled: true`
  - `spring.flyway.locations: classpath:db/migration`

### 2.4. Classe Base de Teste de Integração (`WorkOrderIntegrationTest.java`)
- Pacote: `br.com.fiap.phase4.workorder.iandt`
- Anotações:
  - `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WorkOrderApplication.class)`
  - `@ActiveProfiles("test")`
- Herança de fixtures:
  - `implements PostgresSetup, KafkaSetup`
- Injeções de infraestrutura:
  - `@LocalServerPort private Integer port;`
  - Repositórios Spring Data JPA para limpeza e verificações de banco.
  - `KafkaTemplate<String, Object> kafkaTemplate;`
  - `ConsumerFactory<String, Object> consumerFactory;`
- Ciclo de vida `@BeforeEach`:
  - Configuração do `RestAssured.baseURI`.
  - Limpeza determinística de tabelas relacionais respeitando integridade de FKs.

### 2.5. Suítes de Testes de Integração
1. **`WorkOrderPersistenceIntegrationTest.java`**:
   - Execução das migrations Flyway (`V1` a `V4`).
   - Validação de constraints, integridade de FKs e persistência de clientes, veículos, catálogos e itens da OS.
2. **`CustomerVehicleControllerIntegrationTest.java`**:
   - Criação e busca de clientes (validações de CPF, duplicidade, RFC 7807).
   - Cadastro e busca de veículos por placa.
3. **`WorkOrderControllerIntegrationTest.java`**:
   - Abertura de OS (`POST /api/v1/work-orders`).
   - Adição de serviços e peças de manutenção à OS.
   - Cálculo automático do valor total.
   - Transições de status da OS (`RECEIVED` -> `APPROVED` -> `IN_PROGRESS` -> `COMPLETED`).
4. **`WorkOrderEventsKafkaIntegrationTest.java`**:
   - Publicação assíncrona de `WorkOrderApprovedEvent` no tópico `work-order-events` (Saga Outbound).
   - Consumo de `ExecutionCompletedEvent` no tópico `execution-events` finalizando a OS (Saga Inbound).

---

## 3. Validação e Quality Gate
- Compilação dos módulos (`mvn test-compile`).
- Execução da suíte completa de testes de unidade sem dependência externa.
- Execução dos testes integrados com Testcontainers via `mvn test -DintegrationTests=true`.
