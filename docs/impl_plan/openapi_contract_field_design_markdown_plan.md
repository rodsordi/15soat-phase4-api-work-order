# Plano de Implementação: Design System dos Campos com Markdown no Swagger

> **Decisão do Usuário**: Opção 1 - Design System Integral de Campos em Markdown em Todos os Schemas
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
A auditoria técnica identificou que 97% das 224 propriedades de schemas do contrato OpenAPI (`15soat-phase4-api-work-order/application/src/main/resources/contracts/openapi.yaml`) utilizavam descrições em texto simples, e 22 propriedades com referências hipermídia ou `$ref` não possuíam descrições explicativas inline.

Este plano tem como objetivo:
1. Padronizar o design visual de todas as propriedades de schemas (`components/schemas`) e parâmetros (`components/parameters`) utilizando CommonMark (Markdown).
2. Adotar convenções visuais consistentes:
   - **Monoespaçamento (backticks)** para códigos, regex, UUIDs, enums e máscaras (ex: `` `BRA2E19` ``, `` `UUID v4` ``, `` `SRV-OIL-01` ``).
   - **Negrito e marcadores estruturados** para tipos contábeis e precisão (ex: `**Monetário (BigDecimal)**`).
   - **Especificação explícita de restrições** (ex: `**Obrigatório**`, `**Mínimo: 0.01**`, `**Formato: apenas dígitos numéricos**`).
3. Preencher todas as 22 propriedades identificadas com ausência de descrição inline (como atributos com `$ref`, nós de coleções paginadas `content` e referências de hipermídia `_links` e `PageLinks`).

---

## 2. Padrões de Design Aplicados por Categoria de Campo

### 2.1. Identificadores e Chaves Primárias
- Formato: Identificador único universal no padrão `UUID v4` (RFC 4122).
- Notação: `` `UUID v4` ``.

### 2.2. Documentos e Registros (CPF / CNPJ)
- `document` / `customerDocument`: Detalhamento dos padrões para Pessoa Física (11 dígitos) e Pessoa Jurídica (14 dígitos), com restrição estrita de caracteres numéricos.

### 2.3. Placas de Veículos
- `licensePlate`: Especificação dos formatos aceitos com exemplos:
  - Padrão Mercosul: `` `BRA2E19` `` (3 letras, 1 dígito, 1 letra, 2 dígitos).
  - Padrão Tradicional: `` `ABC-1234` `` ou `` `ABC1234` `` (3 letras, 4 dígitos).

### 2.4. Campos Monetários e Contábeis
- `hourlyRate`, `price`, `unitCost`, `unitPrice`, `subtotal`, `totalAmount`, etc.:
  - Indicação expressa de tipo `BigDecimal`.
  - Precisão de 2 casas decimais e limite `minimum: 0.01` (ou `0.0` para custos/subtotais).

### 2.5. Campos Temporais
- `createdAt`, `updatedAt`, `timestamp`: Indicação expressa de padrão `ISO 8601 UTC` (`` `YYYY-MM-DDThh:mm:ssZ` ``).

### 2.6. Navegação Hipermídia e Paginação HAL
- Nós `_links`, `Link`, `PageLinks`, `first`, `prev`, `next`, `last`: Documentação de conformidade com RFC 8288 e RFC 6570.

---

## 3. Critérios de Aceite e Verificação
1. Sintaxe YAML rigorosamente válida, sem quebra de parsing no SnakeYAML/IntelliJ.
2. 100% das propriedades de schemas com descrições semânticas ricas e formatadas.
3. Suíte de testes do projeto (`mvn test`) com 100% de sucesso (43 testes passando).
4. Zero modificações em código-fonte Java.
5. Nenhuma execução automática de commit ou push no repositório Git.
