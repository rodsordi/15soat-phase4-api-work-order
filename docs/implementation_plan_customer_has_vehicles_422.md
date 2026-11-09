# Plano de Implementação: Tratamento de Status HTTP 422 para Violações Semânticas de Negócio

> **Decisão do Usuário**: Opção 1 - Harmonização Completa (Alinhamento Estrito ao Diagrama e RFC 7807 / RFC 4918)
> **Status**: Aprovado para Execução

## Contexto e Objetivo
O diagrama de sequência documentado em `customer_vehicle_management.md` (Bloco 8) e a especificação BDD em `customer_vehicle_management.feature` (Cenário 13) definem que a tentativa de exclusão de um cliente que ainda possui veículos vinculados deve retornar `HTTP 422 Unprocessable Entity` com Problem Details (RFC 7807) e código de erro `CUSTOMER_HAS_VEHICLES`.

Atualmente, o handler comum captura `DomainException` e retorna `400 Bad Request`. Criaremos um `@RestControllerAdvice` local no microserviço `15soat-phase4-api-work-order` (com `@Order(Ordered.HIGHEST_PRECEDENCE)`) para interceptar erros semânticos de integridade referencial (`CUSTOMER_HAS_VEHICLES` e `CUSTOMER_HAS_ACTIVE_WORK_ORDERS`) e emitir resposta com `HttpStatus.UNPROCESSABLE_ENTITY` (422), preservando o ProblemDetail da RFC 7807.

## Escopo de Alterações

### 1. `WorkOrderExceptionHandler.java` (em `adapter/in/web`)
Criar o controller advice para lidar com regras semânticas de exclusão:
```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class WorkOrderExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        if ("CUSTOMER_HAS_VEHICLES".equals(ex.getErrorCode()) || "CUSTOMER_HAS_ACTIVE_WORK_ORDERS".equals(ex.getErrorCode())) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
            problemDetail.setTitle("Business Rule Violation");
            problemDetail.setProperty("errorCode", ex.getErrorCode());
            problemDetail.setProperty("timestamp", Instant.now());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problemDetail);
        }
        // Deixar para os demais handlers tratarem outras exceções de domínio
        throw ex;
    }
}
```

### 2. Validação e Testes
1. Executar `mvn test` no módulo `work-order-application`.
2. Garantir 100% dos testes unitários passando.
3. Submeter via commit/push e validar pipeline CI/CD no GitHub Actions.
