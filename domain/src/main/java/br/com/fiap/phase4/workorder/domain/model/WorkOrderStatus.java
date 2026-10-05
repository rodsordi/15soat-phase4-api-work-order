package br.com.fiap.phase4.workorder.domain.model;

public enum WorkOrderStatus {
    RECEIVED,
    DIAGNOSING,
    WAITING_APPROVAL,
    APPROVED,
    EXECUTING,
    COMPLETED,
    DELIVERED,
    CANCELED,
    PAYMENT_REJECTED;

    public static WorkOrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }
        String normalized = value.trim().toUpperCase();
        if ("WAITING_FOR_APPROVAL".equals(normalized)) {
            return WAITING_APPROVAL;
        }
        if ("RELEASED".equals(normalized) || "FINISHED".equals(normalized)) {
            return DELIVERED;
        }
        return WorkOrderStatus.valueOf(normalized);
    }
}
