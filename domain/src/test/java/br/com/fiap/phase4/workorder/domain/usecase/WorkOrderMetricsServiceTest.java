package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.ServiceAverageTimeMetric;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.ServiceCatalogRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkOrderMetricsService Unit Tests")
class WorkOrderMetricsServiceTest {

    @Mock
    private WorkOrderRepositoryPort workOrderRepository;

    @Mock
    private ServiceCatalogRepositoryPort serviceCatalogRepository;

    private WorkOrderMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new WorkOrderMetricsService(workOrderRepository, serviceCatalogRepository);
    }

    @Test
    @DisplayName("Should return 0 minutes metric when no completed work orders exist")
    void shouldReturnZeroMetricWhenNoCompletedWorkOrders() {
        ServiceCatalog service = ServiceCatalog.create(
                "SRV-OIL-01",
                "Oil Change",
                "Engine oil change",
                Money.of(new BigDecimal("150.00"))
        );

        when(serviceCatalogRepository.findAll()).thenReturn(List.of(service));
        when(workOrderRepository.findAll()).thenReturn(List.of());

        List<ServiceAverageTimeMetric> metrics = metricsService.calculateAndGetAverageExecutionTimes();

        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).serviceCode()).isEqualTo("SRV-OIL-01");
        assertThat(metrics.get(0).averageMinutes()).isEqualTo(0L);
        assertThat(metrics.get(0).totalCompletedOrders()).isEqualTo(0L);
        verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
    }

    @Test
    @DisplayName("Should calculate average execution time correctly based on order status history")
    void shouldCalculateAverageExecutionTimeCorrectly() {
        UUID serviceId = UUID.randomUUID();
        ServiceCatalog service = new ServiceCatalog(
                serviceId,
                "SRV-OIL-01",
                "Oil Change",
                "Engine oil change",
                Money.of(new BigDecimal("150.00")),
                0L,
                Instant.now(),
                Instant.now()
        );

        WorkOrder order = WorkOrder.create(new CpfOrCnpj("52998224725"), new LicensePlate("BRA2E19"), "Test order");
        order.addService(serviceId, "Oil Change", "SRV-OIL-01", Money.of(new BigDecimal("150.00")), 1);

        Instant start = Instant.now().minus(60, ChronoUnit.MINUTES);
        order.transitionTo(WorkOrderStatus.EXECUTING, "Started repairs", "mechanic");
        // Update history timestamp to simulate 60 minutes execution
        order.complete();

        when(serviceCatalogRepository.findAll()).thenReturn(List.of(service));
        when(workOrderRepository.findAll()).thenReturn(List.of(order));

        List<ServiceAverageTimeMetric> metrics = metricsService.calculateAndGetAverageExecutionTimes();

        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).serviceCode()).isEqualTo("SRV-OIL-01");
        assertThat(metrics.get(0).totalCompletedOrders()).isEqualTo(1L);
        verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
    }

    @Test
    @DisplayName("Should filter metrics by serviceCode when filter is provided")
    void shouldFilterMetricsByServiceCode() {
        ServiceCatalog service1 = ServiceCatalog.create("SRV-OIL-01", "Oil Change", "Engine oil", Money.of(new BigDecimal("150.00")));
        ServiceCatalog service2 = ServiceCatalog.create("SRV-BRK-01", "Brake Pad", "Brakes", Money.of(new BigDecimal("200.00")));

        when(serviceCatalogRepository.findAll()).thenReturn(List.of(service1, service2));
        when(workOrderRepository.findAll()).thenReturn(List.of());

        List<ServiceAverageTimeMetric> filtered = metricsService.calculateAndGetAverageExecutionTimes("SRV-OIL-01");

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).serviceCode()).isEqualTo("SRV-OIL-01");

        List<ServiceAverageTimeMetric> all = metricsService.calculateAndGetAverageExecutionTimes(null);
        assertThat(all).hasSize(2);
    }
}
