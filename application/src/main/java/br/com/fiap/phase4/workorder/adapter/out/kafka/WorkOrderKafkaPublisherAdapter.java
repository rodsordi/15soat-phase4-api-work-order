package br.com.fiap.phase4.workorder.adapter.out.kafka;

import br.com.fiap.phase4.commons.kafka.event.EventEnvelope;
import br.com.fiap.phase4.commons.kafka.tracing.KafkaTraceContextUtils;
import br.com.fiap.phase4.workorder.adapter.in.kafka.dto.WorkOrderApprovedPayload;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderKafkaPublisherAdapter implements WorkOrderEventPublisherPort {

    public static final String WORK_ORDER_EVENTS_TOPIC = "work-order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishWorkOrderApproved(WorkOrder workOrder, String traceId) {
        WorkOrderApprovedPayload payload = new WorkOrderApprovedPayload()
                .withWorkOrderId(workOrder.getId())
                .withCustomerDocument(workOrder.getCustomerDocument().value())
                .withTotalAmount(workOrder.getTotalAmount().amount().doubleValue());

        var envelope = EventEnvelope.of(
                "WorkOrderApprovedEvent",
                "15soat-phase4-api-work-order",
                traceId,
                payload
        );

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                WORK_ORDER_EVENTS_TOPIC,
                workOrder.getId().toString(),
                envelope
        );

        KafkaTraceContextUtils.injectTraceContext(record.headers(), traceId, null);
        kafkaTemplate.send(record);
        log.info("Published WorkOrderApprovedEvent for OS ID: {} on topic: {}", workOrder.getId(), WORK_ORDER_EVENTS_TOPIC);
    }

    @Override
    public void publishWorkOrderCompleted(WorkOrder workOrder, String traceId) {
        Map<String, Object> payload = Map.of(
                "workOrderId", workOrder.getId().toString(),
                "status", workOrder.getStatus().name()
        );

        var envelope = EventEnvelope.of(
                "WorkOrderCompletedEvent",
                "15soat-phase4-api-work-order",
                traceId,
                payload
        );

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                WORK_ORDER_EVENTS_TOPIC,
                workOrder.getId().toString(),
                envelope
        );

        KafkaTraceContextUtils.injectTraceContext(record.headers(), traceId, null);
        kafkaTemplate.send(record);
        log.info("Published WorkOrderCompletedEvent for OS ID: {} on topic: {}", workOrder.getId(), WORK_ORDER_EVENTS_TOPIC);
    }

    @Override
    public void publishWorkOrderEstimateRequested(WorkOrder workOrder, String traceId) {
        java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();
        if (workOrder.getServices() != null) {
            for (var s : workOrder.getServices()) {
                items.add(Map.of(
                        "itemType", "SERVICE",
                        "itemCodeOrSku", s.getItemCode() != null ? s.getItemCode() : "SRV",
                        "name", s.getItemName() != null ? s.getItemName() : "Service",
                        "unitPrice", s.getUnitPrice() != null ? s.getUnitPrice().amount().doubleValue() : 0.0,
                        "quantity", s.getQuantity() != null ? s.getQuantity() : 1
                ));
            }
        }
        if (workOrder.getMaterials() != null) {
            for (var m : workOrder.getMaterials()) {
                items.add(Map.of(
                        "itemType", "MATERIAL",
                        "itemCodeOrSku", m.getItemSku() != null ? m.getItemSku() : "MAT",
                        "name", m.getItemName() != null ? m.getItemName() : "Material",
                        "unitPrice", m.getUnitPrice() != null ? m.getUnitPrice().amount().doubleValue() : 0.0,
                        "quantity", m.getQuantity() != null ? m.getQuantity() : 1
                ));
            }
        }

        Map<String, Object> payload = Map.of(
                "workOrderId", workOrder.getId().toString(),
                "customerDocument", workOrder.getCustomerDocument() != null ? workOrder.getCustomerDocument().value() : "",
                "totalAmount", workOrder.getTotalAmount() != null ? workOrder.getTotalAmount().amount().doubleValue() : 0.0,
                "items", items
        );

        var envelope = EventEnvelope.of(
                "WorkOrderEstimateRequestedEvent",
                "15soat-phase4-api-work-order",
                traceId,
                payload
        );

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                WORK_ORDER_EVENTS_TOPIC,
                workOrder.getId().toString(),
                envelope
        );

        KafkaTraceContextUtils.injectTraceContext(record.headers(), traceId, null);
        kafkaTemplate.send(record);
        log.info("Published WorkOrderEstimateRequestedEvent for OS ID: {} on topic: {}", workOrder.getId(), WORK_ORDER_EVENTS_TOPIC);
    }
}
