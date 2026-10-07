package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.ServiceAverageTimeMetric;

import java.util.List;

public interface GetWorkOrderMetricsUseCase {
    List<ServiceAverageTimeMetric> calculateAndGetAverageExecutionTimes();
}
