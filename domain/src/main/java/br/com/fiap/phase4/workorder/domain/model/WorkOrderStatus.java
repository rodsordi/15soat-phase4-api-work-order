package br.com.fiap.phase4.workorder.domain.model;

public enum WorkOrderStatus {
    RECEIVED,
    DIAGNOSING,
    WAITING_APPROVAL,
    APPROVED,
    EXECUTING,
    COMPLETED,
    CANCELED,
    PAYMENT_REJECTED
}
