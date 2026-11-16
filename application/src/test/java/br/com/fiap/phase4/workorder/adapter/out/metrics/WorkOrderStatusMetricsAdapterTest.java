package br.com.fiap.phase4.workorder.adapter.out.metrics;

import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkOrderStatusMetricsAdapter Unit Tests")
class WorkOrderStatusMetricsAdapterTest {

    private MeterRegistry meterRegistry;
    private WorkOrderStatusMetricsAdapter adapter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        adapter = new WorkOrderStatusMetricsAdapter(meterRegistry);
    }

    @Nested
    @DisplayName("Status Duration Metrics Recording")
    class StatusDurationRecording {

        @Test
        @DisplayName("Should record status duration in both Phase 4 and Phase 3 timer metrics")
        void shouldRecordStatusDurationSuccessfully() {
            Duration duration = Duration.ofSeconds(45);
            adapter.recordStatusDuration(WorkOrderStatus.DIAGNOSING, duration);

            var phase4Timer = meterRegistry.find("workorder.status.duration")
                    .tag("status", "DIAGNOSING")
                    .timer();
            assertThat(phase4Timer).isNotNull();
            assertThat(phase4Timer.count()).isEqualTo(1);
            assertThat(phase4Timer.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(45.0);

            var phase3Timer = meterRegistry.find("garage.workorder.status.duration")
                    .tag("status", "DIAGNOSING")
                    .timer();
            assertThat(phase3Timer).isNotNull();
            assertThat(phase3Timer.count()).isEqualTo(1);
            assertThat(phase3Timer.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(45.0);
        }

        @Test
        @DisplayName("Should silently ignore null status or negative duration")
        void shouldIgnoreInvalidInputs() {
            adapter.recordStatusDuration(null, Duration.ofSeconds(10));
            adapter.recordStatusDuration(WorkOrderStatus.EXECUTING, null);
            adapter.recordStatusDuration(WorkOrderStatus.EXECUTING, Duration.ofSeconds(-5));

            var timer = meterRegistry.find("workorder.status.duration").timer();
            assertThat(timer).isNull();
        }
    }
}
