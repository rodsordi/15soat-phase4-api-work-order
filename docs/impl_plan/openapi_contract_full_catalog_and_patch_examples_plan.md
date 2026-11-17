# Plano de Implementação: Enriquecimento Completo dos Cadastros Restantes com Combo Boxes no Swagger

> **Decisão do Usuário**: Opção 1 - Enriquecimento Completo dos Cadastros Restantes com Combo Boxes (examples map)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
O contrato OpenAPI (`15soat-phase4-api-work-order/application/src/main/resources/contracts/openapi.yaml`) já possui suporte a múltiplos cenários interativos via mapa `examples` no Swagger UI para `POST /work-orders`, `PATCH /work-orders/{id}`, `POST /customers` e `POST /vehicles`.

Este plano visa aplicar a mesma experiência interativa aos formulários restantes da API, eliminando assimetrias e proporcionando combo boxes interativos para:
1. `POST /api/v1/services`: Casos de uso de serviços rápidos/preventivos e manutenções complexas/corretivas.
2. `POST /api/v1/materials`: Casos de uso de peças de reposição discretas/unitárias e insumos fluidos/fracionados.
3. `PATCH /api/v1/customers/{id}`: Atualização de e-mail vs. atualização cadastral de nome/razão social.
4. `PATCH /api/v1/vehicles/{id}`: Atualização de modelo/marca vs. atualização de ano de fabricação/modelo.
5. `PATCH /api/v1/services/{id}`: Reajuste do valor de mão de obra vs. atualização de escopo descritivo e tempo estimado.
6. `PATCH /api/v1/materials/{id}`: Reajuste de margem/preços de custo e venda vs. ajuste de estoque mínimo de segurança.

---

## 2. Detalhamento dos Exemplos e Payloads

### 2.1. `POST /api/v1/services`
- **`preventiveQuickService`**:
  - `code`: `'SRV-OIL-01'`
  - `name`: `'Troca de Óleo e Filtro'`
  - `description`: `'Substituição completa do lubrificante de motor e elemento filtrante'`
  - `price`: `150.00`
- **`correctiveComplexService`**:
  - `code`: `'SRV-CLU-02'`
  - `name`: `'Substituição do Kit de Embreagem'`
  - `description`: `'Substituição de platô, disco, colar de desengate e alinhamento'`
  - `price`: `680.00`

### 2.2. `POST /api/v1/materials`
- **`discretePartItem`**:
  - `sku`: `'PART-BRK-01'`
  - `name`: `'Pastilha de Freio Dianteira'`
  - `description`: `'Jogo de pastilhas cerâmicas para eixo dianteiro com sensor de desgaste'`
  - `unitCost`: `80.00`
  - `unitPrice`: `140.00`
  - `initialStock`: `20`
  - `minStock`: `5`
- **`fluidConsumableItem`**:
  - `sku`: `'LUB-SYN-5W30'`
  - `name`: `'Óleo Motor Sintético 5W30 (Litro)'`
  - `description`: `'Lubrificante 100% sintético de alta performance API SP e ILSAC GF-6'`
  - `unitCost`: `32.50`
  - `unitPrice`: `65.00`
  - `initialStock`: `100`
  - `minStock`: `25`

### 2.3. `PATCH /api/v1/customers/{id}`
- **`updateEmailOnly`**:
  - `email`: `'carlos.silva.novo@example.com'`
- **`updateNameOnly`**:
  - `name`: `'Carlos Eduardo da Silva'`
- **`updateFullContact`**:
  - `name`: `'Carlos Eduardo da Silva'`
  - `email`: `'carlos.silva.novo@example.com'`

### 2.4. `PATCH /api/v1/vehicles/{id}`
- **`updateModelOnly`**:
  - `model`: `'Corolla Altis Premium Hybrid'`
- **`updateYearOnly`**:
  - `manufactureYear`: `2024`
  - `year`: `2024`
- **`updateMakeAndModel`**:
  - `make`: `'Toyota'`
  - `model`: `'Corolla Cross XRE'`
  - `manufactureYear`: `2024`

### 2.5. `PATCH /api/v1/services/{id}`
- **`priceAdjustmentOnly`**:
  - `price`: `180.00`
- **`timeAndDescriptionUpdate`**:
  - `name`: `'Troca de Óleo e Filtro com Higienização'`
  - `description`: `'Substituição de óleo, troca de filtro e higienização do sistema'`
  - `estimatedMinutes`: `60`
- **`fullServiceUpdate`**:
  - `name`: `'Troca de Óleo e Filtro de Alta Performance'`
  - `description`: `'Substituição com óleo sintético 5W30 e filtro de alta vazão'`
  - `price`: `180.00`
  - `estimatedMinutes`: `60`

### 2.6. `PATCH /api/v1/materials/{id}`
- **`priceAdjustmentOnly`**:
  - `unitCost`: `85.00`
  - `unitPrice`: `155.00`
- **`stockThresholdUpdate`**:
  - `minStock`: `10`
- **`fullMaterialUpdate`**:
  - `name`: `'Pastilha de Freio Cerâmica Premium'`
  - `description`: `'Pastilhas cerâmicas para alto rendimento e frenagem estável'`
  - `unitCost`: `85.00`
  - `unitPrice`: `155.00`
  - `minStock`: `10`

---

## 3. Critérios de Aceite e Verificação
1. Sintaxe YAML rigorosamente válida (sem dois-pontos desprotegidos em textos).
2. Validação via suíte de testes do projeto (`mvn test`) para garantir integridade do contrato (100% de sucesso).
3. Não alteração de código-fonte Java.
4. Nenhuma execução automática de commit ou push no repositório Git.
