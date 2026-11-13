package br.com.fiap.phase4.workorder.iandt;

import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.commons.kafka.event.EventEnvelope;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateWorkOrderStatusUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("Work Order Kafka Saga Events Integration Tests")
class WorkOrderEventsKafkaIntegrationTest extends WorkOrderIntegrationTest {

    @Autowired
    private WorkOrderRepositoryPort workOrderRepository;

    @Autowired
    private CustomerRepositoryPort customerRepository;

    @Autowired
    private VehicleRepositoryPort vehicleRepository;

    @Autowired
    private UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    private Consumer<String, Object> createTestConsumer(String topic, String groupId) {
        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        Consumer<String, Object> consumer = new DefaultKafkaConsumerFactory<String, Object>(props).createConsumer();
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    private WorkOrder createPersistedWorkOrder(WorkOrderStatus initialStatus) {
        String doc = "98765432100";
        String plate = "WKO" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        if (!plate.matches("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$")) {
            plate = "WKO1A23";
        }

        Customer customer = customerRepository.findByDocument(doc).orElseGet(() ->
                customerRepository.save(new Customer(UUID.randomUUID(), "Kafka WO Owner", "kafka.owner@test.com", doc, Instant.now(), Instant.now()))
        );

        final String finalPlate = plate;
        Vehicle vehicle = vehicleRepository.findByLicensePlate(plate).orElseGet(() ->
                vehicleRepository.save(new Vehicle(UUID.randomUUID(), customer, finalPlate, "Toyota", "Yaris", 2022, Instant.now(), Instant.now()))
        );

        WorkOrder workOrder = new WorkOrder(
                UUID.randomUUID(),
                customer,
                vehicle,
                customer.getDocument(),
                vehicle.getLicensePlate(),
                "Kafka Integration Test Work Order",
                initialStatus,
                BigDecimal.valueOf(150.00),
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now()
        );

        return workOrderRepository.save(workOrder);
    }

    @Nested
    @DisplayName("Saga Outbound Events")
    class OutboundEvents {

        @Test
        @DisplayName("Should publish WorkOrderApprovedEvent to work-order-events when work order is approved")
        void shouldPublishWorkOrderApprovedEvent() {
            // Setup consumer for work-order-events
            Consumer<String, Object> consumer = createTestConsumer("work-order-events", "work-order-test-group-" + UUID.randomUUID());
            consumer.poll(Duration.ofMillis(500)); // warmup partition assignment

            WorkOrder workOrder = createPersistedWorkOrder(WorkOrderStatus.WAITING_APPROVAL);
            String traceId = UUID.randomUUID().toString();

            // Trigger approval which dispatches the outbound event
            updateWorkOrderStatusUseCase.updateStatus(workOrder.getId(), WorkOrderStatus.APPROVED, "Customer approved budget", traceId);

            try {
                await().atMost(10, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(300)).untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.key() != null && record.key().equals(workOrder.getId().toString())) {
                            found = true;
                            assertThat(record.value()).isNotNull();
                            break;
                        }
                    }
                    assertThat(found).isTrue();
                });
            } finally {
                consumer.close();
            }
        }
    }

    @Nested
    @DisplayName("Saga Inbound Events")
    class InboundEvents {

        @Test
        @DisplayName("Should update Work Order to COMPLETED upon receiving ExecutionCompletedEvent from execution-events")
        void shouldHandleExecutionCompletedEvent() {
            WorkOrder workOrder = createPersistedWorkOrder(WorkOrderStatus.APPROVED);

            Map<String, Object> payload = Map.of(
                    "workOrderId", workOrder.getId().toString()
            );

            EventEnvelope<Map<String, Object>> envelope = EventEnvelope.of(
                    "ExecutionCompletedEvent",
                    "15soat-phase4-api-exec",
                    UUID.randomUUID().toString(),
                    payload
            );

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    "execution-events",
                    workOrder.getId().toString(),
                    envelope
            );

            kafkaTemplate.send(record);

            await().atMost(10, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(300)).untilAsserted(() -> {
                WorkOrder updated = workOrderRepository.findById(workOrder.getId()).orElse(null);
                assertThat(updated).isNotNull();
                assertThat(updated.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
            });
        }

        @Test
        @DisplayName("Should update Work Order to PAYMENT_REJECTED upon receiving PaymentFailedEvent from payment-events")
        void shouldHandlePaymentFailedEvent() {
            WorkOrder workOrder = createPersistedWorkOrder(WorkOrderStatus.APPROVED);

            Map<String, Object> payload = Map.of(
                    "workOrderId", workOrder.getId().toString(),
                    "reason", "Insufficient balance on credit card"
            );

            EventEnvelope<Map<String, Object>> envelope = EventEnvelope.of(
                    "PaymentFailedEvent",
                    "15soat-phase4-api-billing",
                    UUID.randomUUID().toString(),
                    payload
            );

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    "payment-events",
                    workOrder.getId().toString(),
                    envelope
            );

            kafkaTemplate.send(record);

            await().atMost(10, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(300)).untilAsserted(() -> {
                WorkOrder updated = workOrderRepository.findById(workOrder.getId()).orElse(null);
                assertThat(updated).isNotNull();
                assertThat(updated.getStatus()).isEqualTo(WorkOrderStatus.PAYMENT_REJECTED);
            });
        }
    }
}
