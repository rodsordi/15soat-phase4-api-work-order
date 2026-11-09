# Plano de Implementação: Correções Transacionais e Harmonização dos Testes E2E

> **Decisão do Usuário**: Correções no Work Order Service e no Harness E2E
> **Status**: Aprovado para Execução

---

## 1. Visão Geral

Corrigir a demarcação transacional no domínio de `15soat-phase4-api-work-order` para eliminar o `LazyInitializationException` em coleções mapeadas com `FetchType.LAZY` e harmonizar os steps de teste no `15soat-phase4-e2e` para consumir contratos RESTful canônicos (resolvendo identificadores UUID para `PATCH` / `DELETE` e navegando corretamente em estruturas paginadas `PagedModel`).

---

## 2. Escopo de Alterações

### A. `15soat-phase4-api-work-order`
1. `VehicleService.java`:
   - Adicionar anotação `@Transactional` do Spring (`org.springframework.transaction.annotation.Transactional`) na classe e métodos para manter a sessão Hibernate aberta durante a mutação do agregado.
2. `CustomerService.java`:
   - Adicionar `@Transactional` para garantir consistência e atomicidade nas operações de criação, atualização e exclusão.
3. Validação local:
   - Executar `mvn clean test` no projeto `15soat-phase4-api-work-order`.

### B. `15soat-phase4-e2e`
1. `CustomerSteps.java`:
   - Atualizar a asserção de consulta para extrair atributos tanto da raiz quanto de `content[0]`.
   - Na exclusão e atualização de cliente, resolver o identificador `UUID` via consulta prévia antes de chamar `/api/v1/customers/{id}`.
2. `VehicleSteps.java`:
   - Atualizar a asserção de consulta de veículos para extrair campos de `content[0]`.
   - Na exclusão de veículo por identificador canônico, resolver o `UUID` via consulta prévia antes de chamar `/api/v1/vehicles/{id}`.
3. Validação local dos testes BDD.
