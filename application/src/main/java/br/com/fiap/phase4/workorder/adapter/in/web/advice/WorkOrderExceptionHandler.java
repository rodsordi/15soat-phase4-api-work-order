package br.com.fiap.phase4.workorder.adapter.in.web.advice;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkOrderExceptionHandler {

    private static final Set<String> UNPROCESSABLE_ENTITY_CODES = Set.of(
            "CUSTOMER_HAS_VEHICLES",
            "CUSTOMER_HAS_ACTIVE_WORK_ORDERS"
    );

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        HttpStatus status = UNPROCESSABLE_ENTITY_CODES.contains(ex.getErrorCode())
                ? HttpStatus.UNPROCESSABLE_ENTITY
                : HttpStatus.BAD_REQUEST;

        log.warn("Domain exception handled [{}]: {} with status {}", ex.getErrorCode(), ex.getMessage(), status);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setType(URI.create("https://api.fiap.com.br/errors/domain-error"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        String traceId = MDC.get("traceId");
        if (traceId != null) {
            problemDetail.setProperty("traceId", traceId);
        }

        return ResponseEntity.status(status).body(problemDetail);
    }
}
