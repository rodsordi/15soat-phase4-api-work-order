# Plano de Implementação: Migração dos Cenários E2E para o Módulo iandt do api-work-order

> **Decisão do Usuário**: Opção 1 - Migração Abrangente dos Cenários E2E para o iandt
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O objetivo deste plano é transportar integralmente a cobertura funcional, cenários de negócio e regras de integridade referencial especificados no projeto `15soat-phase4-e2e` para o módulo de testes de integração `work-order-iandt`. Dessa forma, o microsserviço `api-work-order` ganha validação autônoma, rápida e determinística com contêineres Docker reais (PostgreSQL e Kafka via Testcontainers), eliminando a dependência do pipeline E2E externo para garantir a qualidade de seus contratos REST e máquina de estados.

---

## 2. Escopo Detalhado da Implementação

### 2.1. Novo Teste: `CatalogControllerIntegrationTest.java`
Localização: `15soat-phase4-api-work-order/iandt/src/test/java/br/com/fiap/phase4/workorder/iandt/CatalogControllerIntegrationTest.java`

* **Material Management (REST API)**:
  1. `shouldCreateAndRetrieveMaterial`: Cadastro via `POST /api/v1/materials`, asserção de status 201 e consulta por ID / listagem (`GET /api/v1/materials`).
  2. `shouldUpdateMaterialPriceViaPatch`: Atualização do preço de venda com `PATCH /api/v1/materials/{id}` e verificação da persistência.
  3. `shouldRejectDuplicateMaterialSku`: Tentativa de cadastrar material com SKU já existente, esperando erro 400 (`RESOURCE_ALREADY_EXISTS`).
* **Service Catalog Management (REST API)**:
  1. `shouldCreateAndRetrieveServiceCatalog`: Cadastro via `POST /api/v1/services`, status 201 e consulta por ID / listagem (`GET /api/v1/services`).
  2. `shouldRejectDuplicateServiceCode`: Tentativa de cadastrar serviço com código já existente, esperando erro 400 (`RESOURCE_ALREADY_EXISTS`).

---

### 2.2. Enriquecimento: `CustomerVehicleControllerIntegrationTest.java`
Localização: `15soat-phase4-api-work-order/iandt/src/test/java/br/com/fiap/phase4/workorder/iandt/CustomerVehicleControllerIntegrationTest.java`

* **Integridade e Ciclo de Vida de Cliente e Veículo**:
  1. `shouldUpdateCustomerViaPatch`: Atualização parcial de nome/email com `PATCH /api/v1/customers/{id}` (Status 200).
  2. `shouldDeleteVehicleSafely`: Exclusão de veículo sem ordens de serviço ativas com `DELETE /api/v1/vehicles/{id}` (Status 204).
  3. `shouldDeleteCustomerWithoutVehiclesSafely`: Exclusão de cliente sem vínculos com `DELETE /api/v1/customers/{id}` (Status 204).
  4. `shouldRejectCustomerDeletionWhenVehiclesExist`: Rejeição ao tentar excluir cliente com veículos associados, esperando status 422 (`CUSTOMER_HAS_VEHICLES`).
  5. `shouldRejectVehicleCreationForNonExistingCustomer`: Rejeição ao vincular veículo a CPF inexistente, esperando erro 400 (`CUSTOMER_NOT_FOUND`).
  6. `shouldRejectDuplicateCustomerDocument`: Rejeição ao cadastrar cliente com CPF já registrado, esperando erro 400 (`CUSTOMER_ALREADY_EXISTS`).
  7. `shouldRejectDuplicateVehicleLicensePlate`: Rejeição ao cadastrar veículo com placa já registrada, esperando erro 400 (`VEHICLE_ALREADY_EXISTS`).

---

### 2.3. Enriquecimento: `WorkOrderControllerIntegrationTest.java`
Localização: `15soat-phase4-api-work-order/iandt/src/test/java/br/com/fiap/phase4/workorder/iandt/WorkOrderControllerIntegrationTest.java`

* **Ciclo de Vida Completo das 8 Fases**:
  1. `shouldFollowFullWorkOrderLifecycleToDelivered`:
     * `RECEIVED -> DIAGNOSING -> WAITING_APPROVAL -> APPROVED -> EXECUTING -> COMPLETED -> PAID -> DELIVERED`.
  2. `shouldCancelWorkOrderUponCustomerBudgetRejection`:
     * `RECEIVED -> DIAGNOSING -> WAITING_APPROVAL -> CANCELED` (Status 200).
  3. `shouldRejectDeliveryBeforePayment`:
     * Tentativa de transição `COMPLETED -> DELIVERED` sem confirmação de pagamento prévio (`PAID`), esperando erro 400 (`INVALID_STATUS_TRANSITION`).
  4. `shouldRejectCancellationOfPaidWorkOrder`:
     * Tentativa de transição `PAID -> CANCELED`, esperando erro 400 (`INVALID_STATUS_TRANSITION`).

---

## 3. Critérios de Sucesso e Validação

1. **Compilação**: `mvn clean test-compile` bem-sucedido no reactor completo.
2. **Degradação Suave**: `mvn test` executa testes unitários ignorando `iandt` por padrão.
3. **Execução Integrada com Testcontainers**: `mvn test -DintegrationTests=true` executa 100% dos testes de integração passando verde contra contêineres reais de PostgreSQL e Kafka.
4. **Governança Git**: Nenhuma alteração é commitada ou enviada ao repositório remoto sem prévia e expressa autorização do usuário.
