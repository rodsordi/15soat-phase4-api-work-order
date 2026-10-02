package br.com.fiap.phase4.workorder.adapter.out.kafka;

import br.com.fiap.phase4.commons.kafka.event.EventEnvelope;
import br.com.fiap.phase4.commons.kafka.tracing.KafkaTraceContextUtils;
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
        Map<String, Object> payload = Map.of(
                "workOrderId", workOrder.getId().toString(),
                "customerDocument", workOrder.getCustomerDocument().value(),
                "licensePlate", workOrder.getLicensePlate().value(),
                "totalAmount", workOrder.getTotalAmount().amount(),
                "status", workOrder.getStatus().name()
        );

        var envelope = EventEnvelope.of(
                "WorkOrderApprovedEvent",
                "api-work-order",
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
                "api-work-order",
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
}
