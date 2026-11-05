# Plano de Implementação: Configuração de Múltiplos Exemplos (Combo Box) no Swagger UI via OpenAPI 3.0

> **Decisão do Usuário**: Opção 1 - Múltiplos Exemplos com Combo Box Interativo (examples map) no OpenAPI 3.0
> **Status**: Concluído e Validado

---

## 1. Contexto e Objetivo
Permitir que desenvolvedores, QA e integradores selecionem facilmente cenários reais pré-configurados no Swagger UI através do componente nativo de Combo Box (dropdown interativo). Essa experiência é viabilizada pelo mapa `examples` do OpenAPI 3.0 em `content.application/json` dos `requestBody`.

Como o `openapi-generator-maven-plugin` utiliza primariamente as definições de `schema` para a geração de DTOs e interfaces, a inclusão do nó `examples` atua estritamente na camada de documentação interativa e contratos de consumo, sem alterar ou quebrar código Java existente.

---

## 2. Endpoints e Cenários Configurados

### 2.1. `POST /api/v1/work-orders` (Abertura de Ordem de Serviço)
- **`simpleDiagnosis`**: Abertura simples / Diagnóstico inicial (sem itens ou serviços pré-alocados para triagem inicial).
- **`preventiveService`**: Revisão preventiva periódica (com serviço pré-cadastrado no catálogo sem requisição imediata de peças).
- **`fullCorrectiveMaintenance`**: Manutenção corretiva completa (mão de obra de catálogo + peças/materiais de estoque).

### 2.2. `PATCH /api/v1/work-orders/{id}` (Transição de Ciclo de Vida da OS)
- **`approveOrder`**: Aprovação de orçamento (`status: APPROVED`).
- **`startExecution`**: Início da execução no box de oficina (`status: EXECUTING`).
- **`completeOrder`**: Conclusão dos serviços e testes de qualidade (`status: COMPLETED`).
- **`cancelOrder`**: Cancelamento da ordem com justificativa (`status: CANCELED`).

### 2.3. `POST /api/v1/customers` (Cadastro de Clientes)
- **`individualCustomer`**: Pessoa Física (CPF de 11 dígitos numéricos).
- **`corporateCustomer`**: Pessoa Jurídica / Frotista (CNPJ de 14 dígitos numéricos).

### 2.4. `POST /api/v1/vehicles` (Cadastro de Veículos)
- **`mercosulPlate`**: Placa no padrão Mercosul (`BRA2E19`).
- **`traditionalPlate`**: Placa no padrão tradicional cinza (`ABC-1234`).

---

## 3. Validação e Qualidade
- **Contrato OpenAPI**: `application/src/main/resources/contracts/openapi.yaml` atualizado e sintaticamente válido.
- **Suíte de Testes**: Execução completa com `mvn clean test`:
  - `work-order-domain`: 43 testes executados com 0 falhas, 0 erros e 0 pulados.
  - `work-order-application`: Compilação e empacotamento com 100% de sucesso.
  - **Status da Build**: `BUILD SUCCESS`.
