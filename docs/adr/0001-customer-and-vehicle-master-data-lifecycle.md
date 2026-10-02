# ADR 0001: Gestão Mestre de Clientes, Veículos e Ciclo de Vida Reutilizável

* **Status**: Aceito (Accepted)
* **Data**: 2026-10-02
* **Escopo**: Específico do Microsserviço `15soat-phase4-api-work-order`
* **Autores**: FIAP SOAT Tech Challenge Team
* **Decisores Técnicos**: Especialistas em Modelagem de Domínio e Arquitetura de Software

---

## 1. Contexto e Declaração do Problema

Inicialmente na Fase 4, a tabela `work_order` foi modelada recebendo apenas `customer_document` e `license_plate` como dados textuais avulsos (*Value Objects* desacoplados).

Essa simplificação inicial gerou três problemas críticos para a operação da oficina mecânica:
1. **Perda de Dados Técnicos**: A oficina não possuía onde registrar marca (`make`), modelo (`model`) e ano de fabricação (`manufacture_year`) do carro.
2. **Ausência de Histórico do Veículo**: Quando o mesmo carro retornava para manutenções futuras, a oficina não conseguia consultar o histórico de visitas daquele veículo por placa única.
3. **Inconsistência Cadastral**: Sem a tabela mestre de clientes e veículos, não havia garantia de que uma placa informada pertencia de fato àquele cliente.

---

## 2. Decisão Arquitetural

Decidimos restaurar formalmente as entidades e tabelas relacionais `customer` e `vehicle` no banco de dados `work_order_db`:

1. **Tabela `customer`**:
   - Chave Primária: `id UUID`.
   - Dados Cadastrais: `name VARCHAR(255)`, `email VARCHAR(255) UNIQUE`, `document VARCHAR(14) UNIQUE` (CPF/CNPJ).
   - Relacionamento: 1 Cliente para N Veículos (`1:N`).

2. **Tabela `vehicle`**:
   - Chave Primária: `id UUID`.
   - Dados Técnicos: `make VARCHAR(100)`, `model VARCHAR(100)`, `manufacture_year INTEGER`, `license_plate VARCHAR(10) UNIQUE`.
   - Chave Estrangeira: `customer_id UUID FK -> customer(id)`.

3. **Tabela `work_order`**:
   - Chaves Estrangeiras: `customer_id UUID FK -> customer(id)` e `vehicle_id UUID FK -> vehicle(id)`.
   - Colunas de Snapshot: `customer_document` e `license_plate` preservadas para consultas rápidas e emissão de eventos Kafka.

4. **Ciclo de Vida Reutilizável**:
   - O cliente e o veículo são cadastrados **uma única vez** (via `/api/v1/customers` e `/api/v1/vehicles`).
   - Ao abrir uma nova Ordem de Serviço (`POST /api/v1/work-orders`), o atendente apenas referencia o veículo já existente. Todos os dados técnicos (marca, modelo, ano) e histórico de manutenções permanecem preservados e vinculados à placa.

---

## 3. Consequências

### Positivas:
* **Ficha Médica do Veículo**: A oficina ganha rastreabilidade completa de todas as manutenções já executadas em um determinado carro.
* **Integridade Referencial com Chaves Estrangeiras**: Constraints no banco impedem a exclusão acidental de clientes que possuam veículos ou ordens de serviço ativas.
* **Paridade Total com a Fase 3 e Testes E2E**: Alinhamento estrito com os cenários de teste automatizados em `customer_vehicle_management.feature`.

### Negativas / Trade-offs:
* **Dependência Cadastral**: A abertura de uma OS para um veículo novo exige que cliente e veículo existam no banco, ou que sejam resolvidos no momento da criação.
