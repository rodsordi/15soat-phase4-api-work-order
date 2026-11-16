package br.com.fiap.phase4.workorder.adapter.out.metrics;

import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderStatusMetricsPort;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderStatusMetricsAdapter implements WorkOrderStatusMetricsPort {

    private final MeterRegistry meterRegistry;

    @Override
    public void recordStatusDuration(WorkOrderStatus status, Duration duration) {
        if (status != null && duration != null && !duration.isNegative()) {
            meterRegistry.timer("workorder.status.duration", "status", status.name())
                    .record(duration);
            meterRegistry.timer("garage.workorder.status.duration", "status", status.name())
                    .record(duration);
            log.info("Recorded work order status duration metric: status={}, durationMs={}", status.name(), duration.toMillis());
        }
    }
}
