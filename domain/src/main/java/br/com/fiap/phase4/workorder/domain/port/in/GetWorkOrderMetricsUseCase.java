package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.ServiceAverageTimeMetric;

import java.util.List;

public interface GetWorkOrderMetricsUseCase {
    List<ServiceAverageTimeMetric> calculateAndGetAverageExecutionTimes();

    default List<ServiceAverageTimeMetric> calculateAndGetAverageExecutionTimes(String serviceCode) {
        var metrics = calculateAndGetAverageExecutionTimes();
        if (serviceCode == null || serviceCode.isBlank()) {
            return metrics;
        }
        return metrics.stream()
                .filter(m -> m.serviceCode() != null && m.serviceCode().equalsIgnoreCase(serviceCode.trim()))
                .toList();
    }
}
