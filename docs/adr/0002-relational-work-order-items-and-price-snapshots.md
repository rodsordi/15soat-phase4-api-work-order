# ADR 0002: Modelagem Relacional de Itens de Ordem de Serviço com Snapshot Imutável de Preços

* **Status**: Aceito (Accepted)
* **Data**: 2026-10-02
* **Escopo**: Específico do Microsserviço `15soat-phase4-api-work-order`
* **Autores**: FIAP SOAT Tech Challenge Team
* **Decisores Técnicos**: Especialistas em Engenharia de Banco e Arquitetura de Software

---

## 1. Contexto e Declaração do Problema

Uma Ordem de Serviço de oficina mecânica é composta por múltiplos serviços de mão de obra (`service`) e peças/materiais consumidos (`material`).

Sem tabelas associativas dedicadas para os itens da OS:
1. **Volatilidade de Preços de Tabela**: Se o preço da troca de óleo subir no catálogo de R$ 100 para R$ 150 no mês seguinte, ordens de serviço históricas fechadas anteriormente poderiam ter seus valores alterados indevidamente caso dependessem diretamente do preço atual da tabela `service`.
2. **Ausência de Registro de Quantidades**: Não haveria onde registrar a quantidade de peças aplicadas (ex: 4 litros de óleo, 2 pastilhas de freio) ou o número de horas/serviços executados.
3. **Falta de Integridade Referencial (FKs)**: Relações N:M diretas entre tabelas sem entidades intermediárias ferem as normas de integridade do modelo relacional no PostgreSQL.

---

## 2. Decisão Arquitetural

Decidimos criar duas tabelas associativas explícitas com integridade referencial rígida e snapshot imutável de valores monetários:

1. **Tabela `work_order_service`**:
   - `id UUID PRIMARY KEY`.
   - `work_order_id UUID NOT NULL FK -> work_order(id)` com `ON DELETE CASCADE`.
   - `service_id UUID NOT NULL FK -> service(id)` com `ON DELETE RESTRICT`.
   - `unit_price NUMERIC(12,2) NOT NULL` (*Snapshot imutável do valor da mão de obra no momento da inclusão na OS*).
   - `quantity INTEGER NOT NULL DEFAULT 1`.
   - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`.

2. **Tabela `work_order_material`**:
   - `id UUID PRIMARY KEY`.
   - `work_order_id UUID NOT NULL FK -> work_order(id)` com `ON DELETE CASCADE`.
   - `material_id UUID NOT NULL FK -> material(id)` com `ON DELETE RESTRICT`.
   - `unit_price NUMERIC(12,2) NOT NULL` (*Snapshot imutável do valor de venda da peça no momento da inclusão*).
   - `quantity INTEGER NOT NULL DEFAULT 1`.
   - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`.

3. **Cálculo Determinístico do Total**:
   - O campo `total_amount` da tabela `work_order` é calculado de forma atômica no aggregate root:
     $$\text{total\_amount} = \sum (\text{unit\_price} \times \text{quantity})_{\text{serviços}} + \sum (\text{unit\_price} \times \text{quantity})_{\text{materiais}}$$

---

## 3. Consequências

### Positivas:
* **Imutabilidade Histórica**: Alterações nos preços de catálogo nunca retroagem sobre ordens de serviço existentes.
* **Integridade Referencial Estrita**: Não é permitido excluir um serviço ou peça do catálogo se houver ordens de serviço que o referenciem (`ON DELETE RESTRICT`).
* **Auditoria Financeira**: Transparência contábil de cada centavo cotado e cobrado na OS.

### Negativas / Trade-offs:
* **Cascade Delete Restrito à OS**: Ao deletar uma OS de teste, seus itens associados são deletados em cascata (`CASCADE`), mantendo os catálogos intactos.
