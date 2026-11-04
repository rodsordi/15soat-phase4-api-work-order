# Plano de Implementação: Especialização dos Textos de Descrição de Erros no Swagger UI

> **Decisão do Usuário**: Opção 3 - Customizar Textos de Descrição (description) de Erro por Operação mantendo Schema Canônico
> **Status**: Concluído e Validado

---

## 1. Contexto e Objetivo
Anteriormente, todos os endpoints do contrato OpenAPI utilizavam referências globais para respostas de erro (`$ref: '#/components/responses/BadRequest'`, `NotFound`, `Conflict`, `UnprocessableEntity`), fazendo com que o Swagger UI exibisse textos genéricos e repetitivos (ex: *"Recurso Não Encontrado (Not Found)"*, *"Requisição Inválida (Bad Request)"*) em todas as rotas.

A implementação contextualizou os textos explicativos (`description`) de erro para cada operação específica de negócio da oficina (Work Orders, Customers, Vehicles, Services, Materials), mantendo a conformidade com o schema canônico RFC 7807/9457 `ProblemDetail`.

---

## 2. Escopo das Alterações Concluídas

1. **Work Orders (`/api/v1/work-orders`)**:
   - `POST`: 400 (Violação de esquema ou atributos obrigatórios ausentes na abertura da OS), 409 (Conflito de agendamento ou ordem de serviço concorrente já em andamento para o veículo), 422 (Inconsistência de negócio: cliente ou veículo inexistente, serviço ou peça inativo no catálogo).
   - `GET /api/v1/work-orders`: 400 (Parâmetros de consulta inválidos: filtro por status, documento, placa ou paginação incorretos).
   - `GET /api/v1/work-orders/{id}`: 400 (Identificador da Ordem de Serviço fornecido em formato UUID inválido), 404 (Ordem de Serviço não encontrada para o identificador UUID informado).
   - `PATCH /api/v1/work-orders/{id}`: 400 (Identificador UUID inválido ou payload de transição malformado), 404 (Ordem de Serviço não encontrada), 409 (Conflito de concorrência ou transição incompatível com o estado atual da OS), 422 (Transição de status inválida no ciclo de vida da Ordem de Serviço).

2. **Customers (`/api/v1/customers`)**:
   - `POST`: 400 (Violação de atributos do cliente: nome, e-mail malformado ou documento fora do padrão), 409 (Conflito cadastral: já existe um cliente cadastrado com o CPF ou CNPJ informado), 422 (Documento informado possui dígitos verificadores inválidos perante algoritmo de validação fiscal).
   - `GET /api/v1/customers`: 400 (Parâmetros de consulta inválidos: filtro por documento fora do padrão ou parâmetros de paginação incorretos).
   - `GET /api/v1/customers/{id}`: 400 (Identificador do cliente fornecido em formato UUID inválido), 404 (Cliente não encontrado para o identificador UUID informado).
   - `PATCH /api/v1/customers/{id}`: 400 (Identificador UUID inválido ou payload de atualização com campos inválidos), 404 (Cliente não encontrado), 409 (Conflito com dados cadastrais já existentes: ex: e-mail já utilizado), 422 (Inconsistência de validação nos dados do cliente fornecidos).
   - `DELETE /api/v1/customers/{id}`: 400 (Identificador UUID do cliente fornecido em formato inválido), 404 (Cliente não encontrado para exclusão), 409 (Conflito de integridade relacional: o cliente possui ordens de serviço ou veículos ativos vinculados), 422 (Não é permitido excluir cliente com histórico ativo ou pendências financeiras na oficina).

3. **Vehicles (`/api/v1/vehicles`)**:
   - `POST`: 400 (Violação de atributos do veículo: placa em padrão inválido, ano fora do intervalo aceitável ou campos obrigatórios ausentes), 409 (Conflito cadastral: já existe um veículo cadastrado com a placa informada), 422 (Inconsistência de domínio: o cliente proprietário associado não existe ou encontra-se inativo).
   - `GET /api/v1/vehicles`: 400 (Parâmetros de consulta inválidos: filtro por placa, documento do proprietário ou parâmetros de paginação incorretos).
   - `GET /api/v1/vehicles/{id}`: 400 (Identificador do veículo fornecido em formato UUID inválido), 404 (Veículo não encontrado para o identificador UUID informado).
   - `PATCH /api/v1/vehicles/{id}`: 400 (Identificador UUID inválido ou payload de atualização com campos inválidos), 404 (Veículo não encontrado), 409 (Conflito com dados já existentes: placa informada já pertence a outro veículo), 422 (Inconsistência de validação nos dados do veículo fornecidos).
   - `DELETE /api/v1/vehicles/{id}`: 400 (Identificador UUID do veículo fornecido em formato inválido), 404 (Veículo não encontrado para exclusão), 409 (Conflito de integridade relacional: o veículo possui ordens de serviço ativas vinculadas e não pode ser excluído), 422 (Não é permitido excluir veículo vinculado a serviços em execução na oficina).

4. **Service Catalog (`/api/v1/services`)**:
   - `POST`: 400 (Violação de atributos do serviço: código padronizado, nome, descrição ou preço inválidos), 409 (Conflito cadastral: já existe um serviço registrado com o código informado), 422 (Inconsistência de domínio na definição do serviço: preço ou tempo estimado inválido).
   - `GET /api/v1/services`: 400 (Parâmetros de consulta inválidos: filtro por código do serviço ou paginação incorreta).
   - `GET /api/v1/services/metrics`: 400 (Parâmetro de filtro de serviço inválido para consulta de métricas).
   - `GET /api/v1/services/{id}`: 400 (Identificador do serviço fornecido em formato UUID inválido), 404 (Serviço não encontrado no catálogo para o identificador UUID informado).
   - `PATCH /api/v1/services/{id}`: 400 (Identificador UUID inválido ou payload de atualização com campos inválidos), 404 (Serviço não encontrado), 409 (Conflito com dados já existentes no catálogo), 422 (Inconsistência de validação nos valores de preço ou tempo estimado atualizados).
   - `DELETE /api/v1/services/{id}`: 400 (Identificador UUID do serviço fornecido em formato inválido), 404 (Serviço não encontrado no catálogo para exclusão), 409 (Conflito de integridade relacional: o serviço está vinculado a ordens de serviço ativas ou histórico de manutenção), 422 (Não é permitido remover serviço com histórico ativo de execuções na oficina).

5. **Materials (`/api/v1/materials`)**:
   - `POST`: 400 (Violação de atributos do material: SKU, nome, preços ou quantidade inicial inválidos), 409 (Conflito cadastral: já existe uma peça/material cadastrado com o SKU informado), 422 (Inconsistência de domínio: preço de venda inferior ao custo unitário ou quantidade negativa).
   - `GET /api/v1/materials`: 400 (Parâmetros de consulta inválidos: filtro por SKU ou parâmetros de paginação incorretos).
   - `GET /api/v1/materials/{id}`: 400 (Identificador do material fornecido em formato UUID inválido), 404 (Material não encontrado no estoque para o identificador UUID informado).
   - `PATCH /api/v1/materials/{id}`: 400 (Identificador UUID inválido ou payload de atualização com campos inválidos), 404 (Material não encontrado), 409 (Conflito com dados já existentes no catálogo de materiais), 422 (Inconsistência de validação nos dados de preço ou estoque do material).
   - `DELETE /api/v1/materials/{id}`: 400 (Identificador UUID do material fornecido em formato inválido), 404 (Material não encontrado no estoque para exclusão), 409 (Conflito de integridade relacional: o material possui reservas ativas ou alocações em ordens de serviço pendentes), 422 (Não é permitido remover material com movimentações ou reservas pendentes no estoque).

Erros de infraestrutura/segurança (`401 Unauthorized`, `403 Forbidden`, `500 InternalServerError`) permanecem referenciando os componentes padronizados globais.

---

## 3. Validação e Qualidade
- **Contrato OpenAPI**: Arquivo [`openapi.yaml`](../application/src/main/resources/contracts/openapi.yaml) atualizado sem erros sintáticos.
- **Testes Automatizados**: Suíte executada via `mvn clean test` com **43 testes passando** e **BUILD SUCCESS**.
- **Regras Arquiteturais**: Nenhuma linha de código Java foi alterada; foco restrito ao contrato e à documentação.
