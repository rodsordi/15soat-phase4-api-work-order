# Plano de Implementação: Purificação do Enum WorkOrderStatus

> **Decisão do Usuário**: Opção 1 - Purificação do Enum de Domínio (Remoção de Aliases Legados)
> **Status**: Aprovado para Execução

## 1. Contexto & Objetivos
Eliminar o débito técnico e os resíduos de normalização legada presentes em `WorkOrderStatus.java` no módulo `15soat-phase4-api-work-order`:
- Remover as verificações manuais de aliases descontinuados (`WAITING_FOR_APPROVAL`, `RELEASED`, `FINISHED`), garantindo estrita adesão à Linguagem Ubíqua (DDD) formalizada na OpenAPI da Fase 4.
- Simplificar o método `fromString(String value)` para validação de integridade (nulo/vazio) e resolução estrita via `valueOf(value.trim().toUpperCase())`.
- Assegurar que os 10 estados canônicos da Fase 4 permaneçam íntegros: `RECEIVED`, `DIAGNOSING`, `WAITING_APPROVAL`, `APPROVED`, `EXECUTING`, `COMPLETED`, `PAID`, `DELIVERED`, `CANCELED`, `PAYMENT_REJECTED`.

## 2. Detalhamento da Alteração em `WorkOrderStatus.java`
Substituir a implementação com condicionais de aliases legados pelo padrão canônico:
```java
package br.com.fiap.phase4.workorder.domain.model;

public enum WorkOrderStatus {
    RECEIVED,
    DIAGNOSING,
    WAITING_APPROVAL,
    APPROVED,
    EXECUTING,
    COMPLETED,
    PAID,
    DELIVERED,
    CANCELED,
    PAYMENT_REJECTED;

    public static WorkOrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }
        return WorkOrderStatus.valueOf(value.trim().toUpperCase());
    }
}
```

## 3. Validação
- Executar a suíte de testes unitários da API Work Order: `mvn test` em `15soat-phase4-api-work-order`.
- Executar os testes em `15soat-phase4-e2e` (`CucumberTestRunner`) para assegurar conformidade de ponta a ponta.
