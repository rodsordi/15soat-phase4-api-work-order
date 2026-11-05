# Plano de Implementação: Formalização da Máquina de Estados da OS e Observabilidade no Swagger

> **Decisão do Usuário**: Opção 1 - Formalização da Máquina de Estados da OS & Observabilidade (X-Correlation-Id)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
O contrato OpenAPI (`15soat-phase4-api-work-order/application/src/main/resources/contracts/openapi.yaml`) já possui suporte maduro a HATEOAS HAL (RFC 8288), tratamento de erros RFC 7807/9457 e exemplos interativos em combo boxes.

O objetivo desta implementação é consolidar o contrato nos mais altos padrões de governança de APIs de microsserviços e conformidade com OAS 3.0.3:
1. **Formalização da Máquina de Estados da OS**: Documentar explicitamente a matriz canônica de transições e estados terminais do ciclo de vida da Ordem de Serviço na tag `WorkOrder` e no schema `WorkOrderStatus`.
2. **Observabilidade e Rastreabilidade Transacional**: Adicionar o parâmetro de cabeçalho HTTP `X-Correlation-Id` em `components/parameters` e como header de resposta reutilizável em `components/headers`.
3. **Metadados Canônicos de OAS 3.0.3**: Incluir dados de contato técnico, especificação de licença (MIT License) e links para documentação externa (`externalDocs`).

---

## 2. Detalhamento das Alterações

### 2.1. Metadados do Objeto `info` e `externalDocs`
- **Contato**: Nome da equipe, e-mail institucional e URL do repositório.
- **Licença**: MIT License com URL oficial.
- **Documentação Externa**: Nó global `externalDocs` referenciando os documentos arquiteturais e ADRs.

### 2.2. Tag `WorkOrder` e Schema `WorkOrderStatus`
- Mapeamento completo dos 9 status:
  - `RECEIVED`: Triagem inicial / Ordem aberta.
  - `DIAGNOSING`: Análise mecânica e orçamentação.
  - `WAITING_APPROVAL`: Aguardando aprovação do cliente.
  - `APPROVED`: Orçamento aprovado e pagamento pré-autorizado.
  - `EXECUTING`: Manutenção física em andamento.
  - `COMPLETED`: Serviços concluídos e testados.
  - `DELIVERED`: Veículo entregue (Estado Terminal).
  - `CANCELED`: Ordem cancelada (Estado Terminal).
  - `PAYMENT_REJECTED`: Recusa de transação ou pagamento.

### 2.3. Parâmetro e Header de Observabilidade (`X-Correlation-Id`)
- Definição canônica em `components/parameters/CorrelationIdHeaderParam` (UUID v4).
- Definição em `components/headers/CorrelationIdHeader` para inclusão uniforme em respostas HTTP.

---

## 3. Critérios de Aceite e Verificação
1. Validação sintática do arquivo YAML sem quebras ou formatações inválidas.
2. Suíte de testes do projeto (`mvn test`) executada com 100% de sucesso.
3. Zero modificações em código-fonte Java.
4. Nenhuma execução automática de `git commit` ou `git push`.
