package br.com.fiap.phase4.workorder.adapter.in.kafka;

import br.com.fiap.phase4.commons.kafka.event.EventEnvelope;
import br.com.fiap.phase4.commons.kafka.tracing.KafkaTraceContextUtils;
import br.com.fiap.phase4.workorder.adapter.in.kafka.api.IConsumeExecutionCompletedConsumerService;
import br.com.fiap.phase4.workorder.adapter.in.kafka.api.IConsumePaymentFailedConsumerService;
import br.com.fiap.phase4.workorder.adapter.in.kafka.dto.ExecutionCompletedPayload;
import br.com.fiap.phase4.workorder.adapter.in.kafka.dto.ExecutionFailedPayload;
import br.com.fiap.phase4.workorder.adapter.in.kafka.dto.PaymentFailedPayload;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateWorkOrderStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderKafkaConsumer implements IConsumeExecutionCompletedConsumerService, IConsumePaymentFailedConsumerService {

    private final UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase;

    @Override
    public void consumeExecutionCompleted(ExecutionCompletedPayload payload, ExecutionCompletedPayloadHeaders headers) {
        if (payload != null && payload.getWorkOrderId() != null) {
            log.info("Saga Completion: Finalizing Work Order {} via consumer contract", payload.getWorkOrderId());
            updateWorkOrderStatusUseCase.updateStatus(payload.getWorkOrderId(), WorkOrderStatus.COMPLETED, "Repair completed", null);
        }
    }

    @Override
    public void consumeExecutionCompleted(ExecutionFailedPayload payload, ExecutionFailedPayloadHeaders headers) {
        if (payload != null && payload.getWorkOrderId() != null) {
            log.warn("Saga Rollback: Canceling Work Order {} via consumer contract", payload.getWorkOrderId());
            updateWorkOrderStatusUseCase.updateStatus(payload.getWorkOrderId(), WorkOrderStatus.CANCELED, payload.getReason(), null);
        }
    }

    @Override
    public void consumePaymentFailed(PaymentFailedPayload payload, PaymentFailedPayloadHeaders headers) {
        if (payload != null && payload.getWorkOrderId() != null) {
            log.warn("Saga Rollback Triggered: Rejecting Work Order {} via consumer contract", payload.getWorkOrderId());
            updateWorkOrderStatusUseCase.updateStatus(payload.getWorkOrderId(), WorkOrderStatus.PAYMENT_REJECTED, payload.getReason(), null);
        }
    }

    @Override
    public void defaultHandler(Object payload, Map<String, Object> headers) {
        log.warn("Unhandled message payload: {}", payload != null ? payload.getClass().getName() : null);
    }

    @KafkaListener(topics = "payment-events", groupId = "work-order-payment-group")
    public void consumePaymentEvents(ConsumerRecord<String, EventEnvelope<Map<String, Object>>> record) {
        String traceId = KafkaTraceContextUtils.extractTraceId(record.headers());
        EventEnvelope<Map<String, Object>> envelope = record.value();

        if (envelope == null || envelope.metadata() == null) {
            return;
        }

        String eventType = envelope.metadata().eventType();
        log.info("Received event on payment-events: {} [traceId: {}]", eventType, traceId);

        if ("PaymentFailedEvent".equalsIgnoreCase(eventType)) {
            UUID workOrderId = UUID.fromString(envelope.payload().get("workOrderId").toString());
            String reason = String.valueOf(envelope.payload().getOrDefault("reason", "Payment rejected by gateway"));
            log.warn("Saga Rollback Triggered: Rejecting Work Order {} due to payment failure", workOrderId);
            PaymentFailedPayload payload = new PaymentFailedPayload()
                    .withWorkOrderId(workOrderId)
                    .withReason(reason);
            consumePaymentFailed(payload, null);
        }
    }

    @KafkaListener(topics = "execution-events", groupId = "work-order-execution-group")
    public void consumeExecutionEvents(ConsumerRecord<String, EventEnvelope<Map<String, Object>>> record) {
        String traceId = KafkaTraceContextUtils.extractTraceId(record.headers());
        EventEnvelope<Map<String, Object>> envelope = record.value();

        if (envelope == null || envelope.metadata() == null) {
            return;
        }

        String eventType = envelope.metadata().eventType();
        log.info("Received event on execution-events: {} [traceId: {}]", eventType, traceId);

        if ("ExecutionCompletedEvent".equalsIgnoreCase(eventType)) {
            UUID workOrderId = UUID.fromString(envelope.payload().get("workOrderId").toString());
            log.info("Saga Completion: Finalizing Work Order {} after successful repair", workOrderId);
            ExecutionCompletedPayload payload = new ExecutionCompletedPayload().withWorkOrderId(workOrderId);
            consumeExecutionCompleted(payload, (ExecutionCompletedPayloadHeaders) null);
        } else if ("ExecutionFailedEvent".equalsIgnoreCase(eventType)) {
            UUID workOrderId = UUID.fromString(envelope.payload().get("workOrderId").toString());
            String reason = String.valueOf(envelope.payload().getOrDefault("reason", "Repair execution aborted"));
            log.warn("Saga Rollback: Canceling Work Order {} due to execution failure", workOrderId);
            ExecutionFailedPayload payload = new ExecutionFailedPayload()
                    .withWorkOrderId(workOrderId)
                    .withReason(reason);
            consumeExecutionCompleted(payload, (ExecutionFailedPayloadHeaders) null);
        }
    }
}
