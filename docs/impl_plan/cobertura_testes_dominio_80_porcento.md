# Plano de Implementação: Desativação do JaCoCo no Application e Cobertura >= 80% no Módulo Domain

> **Decisão do Usuário**: Opção 1 - Desativação Declarativa nos application/pom.xml & Suíte TDD @Nested >= 80% no domain
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo

Conforme as diretrizes de Clean Architecture e a solicitação do usuário:
1. **Camada Application**: A medição de cobertura de testes pelo JaCoCo é desativada em todas as APIs (`api-work-order`, `api-billing`, `api-exec`) para eliminar o ruído estatístico decorrente de DTOs e código gerado por OpenAPI/AsyncAPI.
2. **Camada Domain**: O núcleo de regras de negócio, use cases, entidades e máquinas de estado do módulo `work-order-domain` deve possuir cobertura de testes unitários superior a **80%**, com enforcement automatizado via meta `jacoco:check` no Maven.

---

## 2. Alterações Técnicas

### 2.1 Desativação de Cobertura no Application de Todas as APIs
- Adicionar `<properties><jacoco.skip>true</jacoco.skip></properties>` em:
  - `15soat-phase4-api-work-order/application/pom.xml`
  - `15soat-phase4-api-billing/application/pom.xml`
  - `15soat-phase4-api-exec/application/pom.xml`

### 2.2 Configuração da Trava de Qualidade JaCoCo no `work-order-domain`
- No arquivo `15soat-phase4-api-work-order/domain/pom.xml`:
  - Configurar execução do `jacoco-maven-plugin` com a meta `check`.
  - Definir regra com limites mínimos de `0.80` (80%) para `INSTRUCTION` e `LINE`.

### 2.3 Implementação de Testes Unitários de Domínio em JUnit 5 (Jupiter)
Seguindo rigorosamente o padrão canônico da `test-skill`:
- Estrutura hierárquica em 3 níveis com `@Nested` e `@DisplayName`:
  - Nível 1: Classe sob teste (`@DisplayName("NomeComponente Tests")`)
  - Nível 2: Método sob teste (`@Nested @DisplayName("methodUnderTest()")`)
  - Nível 3: Categoria de cenário (`@Nested @DisplayName("Success Scenarios")`, `@Nested @DisplayName("Failure & Validation Scenarios")`)
- Asserções fluentes com AssertJ (`assertThat`, `assertThatThrownBy`) e simulações com BDDMockito.
- Testes parametrizados com `@ParameterizedTest` e `@CsvSource` para limites e validações.
- Classes a implementar/expandir em `domain/src/test/java`:
  - `GetWorkOrderUseCaseTest` (novo): busca por ID (sucesso e `WorkOrderNotFoundException`), filtros por status e listagem paginada;
  - `CustomerServiceTest`: expansão cobrindo criação, atualização, busca por ID, busca por documento e validações;
  - `VehicleServiceTest`: expansão cobrindo criação, atualização, busca por placa, busca por ID e validações;
  - `MaterialServiceTest`: controle de estoque, atualização de preços, ativação/desativação;
  - `ServiceCatalogServiceTest`: catálogo, estimativas de tempo e preços, ativação/desativação;
  - `UpdateWorkOrderStatusUseCaseTest`: todas as transições da máquina de estados do `WorkOrderStatus` e cenários de erro/inconsistência;
  - `WorkOrderDomainTest`, `MaterialTest`, `ServiceCatalogTest`, `CustomerTest`, `VehicleTest`: cobertura de regras de negócio, invariantes e métodos auxiliares.

---

## 3. Roteiro de Execução

1. Criar branch `feature/domain-coverage-80pct` em `15soat-phase4-api-work-order`.
2. Adicionar `<jacoco.skip>true</jacoco.skip>` nos módulos `application/pom.xml` de `api-work-order`, `api-billing` e `api-exec`.
3. Configurar a meta `check` do JaCoCo no `domain/pom.xml` de `api-work-order`.
4. Escrever as suítes de testes unitários no módulo `domain` de `api-work-order`.
5. Executar `mvn clean test jacoco:report jacoco:check` e validar que a cobertura atinge $\ge 80\%$ (visando $\ge 85\%$).
6. Submeter o código ao auditor `Code Reviewer Sentinel` (Diretriz Global #7).
7. Solicitar permissão explícita ao usuário para commit e push (Diretriz Global #1).
8. Abrir PR, realizar merge e validar a pipeline no GitHub Actions.
