package br.com.fiap.phase4.workorder.domain.model;

import java.util.UUID;

public record ServiceAverageTimeMetric(
        UUID serviceId,
        String serviceCode,
        String serviceName,
        Long averageMinutes,
        Long totalCompletedOrders
) {
}
