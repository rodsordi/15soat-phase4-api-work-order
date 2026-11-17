package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.commons.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkOrder Items & Status History Unit Tests")
class WorkOrderItemTest {

    private WorkOrder createWorkOrder() {
        return WorkOrder.create(
                new CpfOrCnpj("52998224725"),
                new LicensePlate("BRA2E19"),
                "Periodic maintenance"
        );
    }

    @Nested
    @DisplayName("WorkOrderServiceItem Tests")
    class ServiceItemTests {

        @Test
        @DisplayName("Should create service item and compute subtotal correctly")
        void shouldCreateServiceItemAndComputeSubtotal() {
            WorkOrder wo = createWorkOrder();
            UUID serviceId = UUID.randomUUID();

            WorkOrderServiceItem item = WorkOrderServiceItem.of(
                    wo, serviceId, "Oil Change", "SRV-01", Money.of(new BigDecimal("120.00")), 2
            );

            assertThat(item.getId()).isNotNull();
            assertThat(item.getWorkOrder()).isSameAs(wo);
            assertThat(item.getServiceId()).isEqualTo(serviceId);
            assertThat(item.getItemName()).isEqualTo("Oil Change");
            assertThat(item.getItemCode()).isEqualTo("SRV-01");
            assertThat(item.getUnitPrice()).isEqualTo(Money.of(new BigDecimal("120.00")));
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getSubtotal()).isEqualTo(Money.of(new BigDecimal("240.00")));
        }

        @Test
        @DisplayName("Should create service item using 4-parameter overload")
        void shouldCreateWithFourParams() {
            WorkOrder wo = createWorkOrder();
            UUID serviceId = UUID.randomUUID();

            WorkOrderServiceItem item = WorkOrderServiceItem.of(
                    wo, serviceId, Money.of(new BigDecimal("80.00")), 1
            );

            assertThat(item.getServiceId()).isEqualTo(serviceId);
            assertThat(item.getSubtotal()).isEqualTo(Money.of(new BigDecimal("80.00")));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is zero or negative")
        void shouldThrowWhenQuantityIsInvalid() {
            WorkOrder wo = createWorkOrder();
            UUID serviceId = UUID.randomUUID();

            assertThatThrownBy(() -> WorkOrderServiceItem.of(wo, serviceId, Money.of(new BigDecimal("80.00")), 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than zero");
        }
    }

    @Nested
    @DisplayName("WorkOrderMaterialItem Tests")
    class MaterialItemTests {

        @Test
        @DisplayName("Should create material item and compute subtotal correctly")
        void shouldCreateMaterialItemAndComputeSubtotal() {
            WorkOrder wo = createWorkOrder();
            UUID materialId = UUID.randomUUID();

            WorkOrderMaterialItem item = WorkOrderMaterialItem.of(
                    wo, materialId, "Brake Pads", "MAT-01", Money.of(new BigDecimal("50.00")), 3
            );

            assertThat(item.getId()).isNotNull();
            assertThat(item.getWorkOrder()).isSameAs(wo);
            assertThat(item.getMaterialId()).isEqualTo(materialId);
            assertThat(item.getItemName()).isEqualTo("Brake Pads");
            assertThat(item.getItemSku()).isEqualTo("MAT-01");
            assertThat(item.getUnitPrice()).isEqualTo(Money.of(new BigDecimal("50.00")));
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getSubtotal()).isEqualTo(Money.of(new BigDecimal("150.00")));
        }

        @Test
        @DisplayName("Should create material item using 4-parameter overload")
        void shouldCreateWithFourParams() {
            WorkOrder wo = createWorkOrder();
            UUID materialId = UUID.randomUUID();

            WorkOrderMaterialItem item = WorkOrderMaterialItem.of(
                    wo, materialId, Money.of(new BigDecimal("30.00")), 2
            );

            assertThat(item.getMaterialId()).isEqualTo(materialId);
            assertThat(item.getSubtotal()).isEqualTo(Money.of(new BigDecimal("60.00")));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is zero or negative")
        void shouldThrowWhenQuantityIsInvalid() {
            WorkOrder wo = createWorkOrder();
            UUID materialId = UUID.randomUUID();

            assertThatThrownBy(() -> WorkOrderMaterialItem.of(wo, materialId, Money.of(new BigDecimal("30.00")), -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than zero");
        }
    }

    @Nested
    @DisplayName("WorkOrderStatusHistory Tests")
    class StatusHistoryTests {

        @Test
        @DisplayName("Should record status history transition correctly")
        void shouldRecordStatusHistory() {
            WorkOrder wo = createWorkOrder();

            WorkOrderStatusHistory history = WorkOrderStatusHistory.record(
                    wo, WorkOrderStatus.RECEIVED, WorkOrderStatus.DIAGNOSING,
                    "Diagnosis started", "mechanic-1"
            );

            assertThat(history.getId()).isNotNull();
            assertThat(history.getWorkOrder()).isSameAs(wo);
            assertThat(history.getFromStatus()).isEqualTo(WorkOrderStatus.RECEIVED);
            assertThat(history.getToStatus()).isEqualTo(WorkOrderStatus.DIAGNOSING);
            assertThat(history.getReason()).isEqualTo("Diagnosis started");
            assertThat(history.getChangedBy()).isEqualTo("mechanic-1");
            assertThat(history.getChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw NullPointerException when workOrder or toStatus is null")
        void shouldThrowWhenRequiredFieldsAreNull() {
            WorkOrder wo = createWorkOrder();

            assertThatThrownBy(() -> new WorkOrderStatusHistory(null, null, null, WorkOrderStatus.DIAGNOSING, null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("WorkOrder is required");

            assertThatThrownBy(() -> new WorkOrderStatusHistory(null, wo, null, null, null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Target status is required");
        }
    }
}
