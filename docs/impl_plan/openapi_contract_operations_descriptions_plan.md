# Plano de Implementação: Enriquecimento das Operações com Descrições Detalhadas no Swagger

> **Decisão do Usuário**: Opção 1 - Enriquecimento Integral das 25 Operações com description Detalhada no Swagger UI & Ajuste de Parâmetros
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
A auditoria técnica automatizada no contrato OpenAPI (`15soat-phase4-api-work-order/application/src/main/resources/contracts/openapi.yaml`) identificou que todas as 25 operações HTTP possuíam apenas o atributo `summary`, deixando a área de conteúdo explicativo (`description`) vazia quando os endpoints são expandidos no Swagger UI.

Este plano tem como objetivo:
1. Inserir o campo `description` detalhado e profissional em todas as 25 operações da API (`WorkOrder`, `Customer`, `Vehicle`, `ServiceCatalog` e `Material`).
2. Documentar em cada rota seu propósito funcional, regras de validação, restrições de integridade referencial (especialmente em `DELETE` e `PATCH`), suporte ao cabeçalho de rastreabilidade `X-Correlation-Id` e navegação hipermídia HATEOAS HAL (`_links`).
3. Adicionar o atributo `example: 'RECEIVED'` no parâmetro de consulta `status` em `GET /api/v1/work-orders`.

---

## 2. Escopo das Operações a Enriquecer

### 2.1. WorkOrder (4 operações)
- `POST /api/v1/work-orders`: Abertura de OS com triagem, vinculação de cliente/veículo e orçamentação preliminar de serviços/peças.
- `GET /api/v1/work-orders`: Consulta paginada com múltiplos filtros combinados (status, documento, placa, período) e ordenação dinâmica.
- `GET /api/v1/work-orders/{id}`: Consulta detalhada por UUID com sub-recursos de mão de obra e peças e links de autodescoberta HAL.
- `PATCH /api/v1/work-orders/{id}`: Transição de status no ciclo de vida (aprovação, início de execução, conclusão, cancelamento) com justificativa.

### 2.2. Customer (5 operações)
- `POST /api/v1/customers`: Cadastro de cliente (PF/PJ) com validação de unicidade de documento e formato de e-mail.
- `GET /api/v1/customers`: Listagem paginada com filtros por documento ou nome e ordenação.
- `GET /api/v1/customers/{id}`: Consulta completa de cliente com veículos associados embutidos e auditoria.
- `PATCH /api/v1/customers/{id}`: Atualização parcial de dados cadastrais (nome, e-mail).
- `DELETE /api/v1/customers/{id}`: Remoção com salvaguarda de integridade referencial (bloqueio se houver OS em aberto).

### 2.3. Vehicle (5 operações)
- `POST /api/v1/vehicles`: Cadastro de veículo com validação de placa (Mercosul/antiga) e vínculo com cliente proprietário.
- `GET /api/v1/vehicles`: Consulta paginada de veículos com filtros por placa e proprietário.
- `GET /api/v1/vehicles/{id}`: Consulta detalhada de veículo com dados resumidos do proprietário e auditoria.
- `PATCH /api/v1/vehicles/{id}`: Atualização parcial de dados do veículo (modelo, ano de fabricação/modelo).
- `DELETE /api/v1/vehicles/{id}`: Remoção com salvaguarda de integridade (bloqueio se houver OS ativa).

### 2.4. ServiceCatalog (6 operações)
- `POST /api/v1/services`: Cadastro de serviço no catálogo com código único padronizado e preço base de mão de obra.
- `GET /api/v1/services`: Listagem paginada de serviços com filtro por código.
- `GET /api/v1/services/metrics`: Apuração de métricas consolidadas de tempo médio real de execução.
- `GET /api/v1/services/{id}`: Consulta de item do catálogo por UUID.
- `PATCH /api/v1/services/{id}`: Atualização de preço, tempo estimado ou escopo descritivo.
- `DELETE /api/v1/services/{id}`: Remoção de serviço com bloqueio caso alocado em ordens ativas.

### 2.5. Material (5 operações)
- `POST /api/v1/materials`: Cadastro de peça ou insumo com SKU único, custos, preço de venda e estoques mínimo e inicial.
- `GET /api/v1/materials`: Listagem paginada de materiais com filtro por SKU.
- `GET /api/v1/materials/{id}`: Consulta de peça/material com posição de estoque e precificação.
- `PATCH /api/v1/materials/{id}`: Atualização de custos, preços de venda ou ponto de ressuprimento.
- `DELETE /api/v1/materials/{id}`: Remoção de item do catálogo com bloqueio se possuir estoque positivo ou ordens ativas.

---

## 3. Critérios de Aceite e Verificação
1. Sintaxe YAML rigorosamente válida (com strings escapadas/delimitadas).
2. Validação automatizada via `mvn test` com 100% dos testes passando.
3. Não alteração de código Java.
4. Nenhuma execução automática de commit ou push no repositório Git.
