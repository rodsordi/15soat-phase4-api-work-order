package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;

import java.time.Duration;

public interface WorkOrderStatusMetricsPort {

    void recordStatusDuration(WorkOrderStatus status, Duration duration);
}
