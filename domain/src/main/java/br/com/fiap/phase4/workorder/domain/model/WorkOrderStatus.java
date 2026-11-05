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
