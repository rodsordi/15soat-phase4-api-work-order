# Catálogo de ADRs - `15soat-phase4-api-work-order`

Este diretório contém os registros formais de decisões de arquitetura (**ADRs**) específicos do microsserviço de Gestão de Ordens de Serviço:

---

## 📚 Decisões Específicas do Microsserviço

| ADR | Título | Status | Data |
| :---: | :--- | :---: | :---: |
| **[ADR 0001](0001-customer-and-vehicle-master-data-lifecycle.md)** | Gestão Mestre de Clientes, Veículos e Ciclo de Vida Reutilizável | **Aceito** | 2026-10-02 |
| **[ADR 0002](0002-relational-work-order-items-and-price-snapshots.md)** | Modelagem Relacional de Itens de Ordem de Serviço com Snapshot Imutável de Preços | **Aceito** | 2026-10-02 |
| **[ADR 0003](0003-customer-summary-vehicles-composition.md)** | Composição Pragmática de Veículos em Resumo de Clientes para Usabilidade Operacional de Balcão | **Aceito** | 2026-10-08 |

---

> [!NOTE]
> Para decisões arquiteturais transversais que afetam todos os serviços da Fase 4 (Database-per-Service, Saga Kafka, Fusão JPA no Domínio, Java 25 / Spring Boot 4), consulte o **[Catálogo Global de ADRs no lib-commons](../../15soat-phase4-lib-commons/docs/adr/README.md)**.
