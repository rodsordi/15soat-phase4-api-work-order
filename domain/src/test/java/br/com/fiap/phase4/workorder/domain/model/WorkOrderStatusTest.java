package br.com.fiap.phase4.workorder.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkOrderStatus Enum Domain Unit Tests")
class WorkOrderStatusTest {

    @Nested
    @DisplayName("Canonical Status Resolution Tests")
    class CanonicalStatusTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "RECEIVED", "DIAGNOSING", "WAITING_APPROVAL", "APPROVED",
                "EXECUTING", "COMPLETED", "PAID", "DELIVERED", "CANCELED", "PAYMENT_REJECTED"
        })
        @DisplayName("Should resolve exact canonical enum values")
        void shouldResolveCanonicalStatuses(String statusName) {
            WorkOrderStatus resolved = WorkOrderStatus.fromString(statusName);
            assertThat(resolved).isNotNull();
            assertThat(resolved.name()).isEqualTo(statusName);
        }

        @Test
        @DisplayName("Should trim and ignore case for canonical values")
        void shouldTrimAndIgnoreCase() {
            assertThat(WorkOrderStatus.fromString("  waiting_approval  "))
                    .isEqualTo(WorkOrderStatus.WAITING_APPROVAL);
            assertThat(WorkOrderStatus.fromString("paid"))
                    .isEqualTo(WorkOrderStatus.PAID);
        }
    }

    @Nested
    @DisplayName("Invalid Status Rejection Tests")
    class InvalidStatusTests {

        @Test
        @DisplayName("Should reject null or blank status strings")
        void shouldRejectNullOrBlank() {
            assertThatThrownBy(() -> WorkOrderStatus.fromString(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Status cannot be null or blank");

            assertThatThrownBy(() -> WorkOrderStatus.fromString("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Status cannot be null or blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"WAITING_FOR_APPROVAL", "RELEASED", "FINISHED", "UNKNOWN_STATUS", "INVALID"})
        @DisplayName("Should reject legacy aliases and unknown strings")
        void shouldRejectLegacyAndUnknownStatuses(String invalidStatus) {
            assertThatThrownBy(() -> WorkOrderStatus.fromString(invalidStatus))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
