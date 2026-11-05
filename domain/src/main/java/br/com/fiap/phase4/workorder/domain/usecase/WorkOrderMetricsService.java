package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.workorder.domain.model.ServiceAverageTimeMetric;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatusHistory;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderServiceItem;
import br.com.fiap.phase4.workorder.domain.port.in.GetWorkOrderMetricsUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.ServiceCatalogRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkOrderMetricsService implements GetWorkOrderMetricsUseCase {

    private final WorkOrderRepositoryPort workOrderRepository;
    private final ServiceCatalogRepositoryPort serviceCatalogRepository;

    @Override
    public List<ServiceAverageTimeMetric> calculateAndGetAverageExecutionTimes() {
        List<ServiceCatalog> catalogServices = serviceCatalogRepository.findAll();
        List<WorkOrder> allOrders = workOrderRepository.findAll();

        Map<UUID, List<Long>> serviceDurationsMap = new HashMap<>();

        for (WorkOrder order : allOrders) {
            Optional<Long> durationMinutesOpt = calculateExecutionDurationMinutes(order);
            if (durationMinutesOpt.isPresent()) {
                long durationMinutes = durationMinutesOpt.get();
                for (WorkOrderServiceItem item : order.getServices()) {
                    serviceDurationsMap
                            .computeIfAbsent(item.getServiceId(), k -> new ArrayList<>())
                            .add(durationMinutes);
                }
            }
        }

        List<ServiceAverageTimeMetric> metrics = new ArrayList<>();

        for (ServiceCatalog service : catalogServices) {
            List<Long> durations = serviceDurationsMap.get(service.getId());
            long averageMinutes = 0L;
            long totalCompleted = 0L;

            if (durations != null && !durations.isEmpty()) {
                totalCompleted = durations.size();
                double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
                averageMinutes = Math.round(avg);
            }

            service.updateEstimatedMinutes(averageMinutes);
            serviceCatalogRepository.save(service);

            metrics.add(new ServiceAverageTimeMetric(
                    service.getId(),
                    service.getCode(),
                    service.getName(),
                    averageMinutes,
                    totalCompleted
            ));
        }

        return metrics;
    }

    private Optional<Long> calculateExecutionDurationMinutes(WorkOrder order) {
        List<WorkOrderStatusHistory> history = order.getStatusHistory();
        if (history == null || history.isEmpty()) {
            return Optional.empty();
        }

        Optional<Instant> completedAtOpt = history.stream()
                .filter(h -> h.getToStatus() == WorkOrderStatus.COMPLETED || h.getToStatus() == WorkOrderStatus.DELIVERED)
                .map(WorkOrderStatusHistory::getChangedAt)
                .min(Comparator.naturalOrder());

        if (completedAtOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<Instant> startedAtOpt = history.stream()
                .filter(h -> h.getToStatus() == WorkOrderStatus.DIAGNOSING
                        || h.getToStatus() == WorkOrderStatus.EXECUTING
                        || h.getToStatus() == WorkOrderStatus.APPROVED)
                .map(WorkOrderStatusHistory::getChangedAt)
                .min(Comparator.naturalOrder());

        Instant startedAt = startedAtOpt.orElse(order.getCreatedAt());
        Instant completedAt = completedAtOpt.get();

        if (completedAt.isBefore(startedAt)) {
            return Optional.of(0L);
        }

        long minutes = Duration.between(startedAt, completedAt).toMinutes();
        return Optional.of(Math.max(0L, minutes));
    }
}
