package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkOrderUseCase Unit Tests")
class GetWorkOrderUseCaseTest {

    @Mock
    private WorkOrderRepositoryPort repository;

    @InjectMocks
    private GetWorkOrderUseCaseImpl getWorkOrderUseCase;

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should return work order when found by ID")
            void shouldReturnWorkOrderWhenFound() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = WorkOrder.create(
                        new CpfOrCnpj("52998224725"),
                        new LicensePlate("BRA2E19"),
                        "General vehicle inspection"
                );
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = getWorkOrderUseCase.getById(id);

                // Assert
                assertThat(result).isNotNull().isSameAs(workOrder);
                verify(repository).findById(id);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw ResourceNotFoundException when work order does not exist")
            void shouldThrowExceptionWhenNotFound() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(repository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> getWorkOrderUseCase.getById(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining(id.toString());
                verify(repository).findById(id);
            }
        }
    }

    @Nested
    @DisplayName("listByFilters()")
    class ListByFiltersTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should return matching work orders when filters are applied")
            void shouldReturnWorkOrdersWhenFiltersMatch() {
                // Arrange
                WorkOrderStatus status = WorkOrderStatus.RECEIVED;
                String doc = "52998224725";
                String plate = "BRA2E19";
                WorkOrder workOrder = WorkOrder.create(
                        new CpfOrCnpj(doc),
                        new LicensePlate(plate),
                        "Brake pad replacement"
                );
                given(repository.findByFilters(status, doc, plate)).willReturn(List.of(workOrder));

                // Act
                List<WorkOrder> results = getWorkOrderUseCase.listByFilters(status, doc, plate);

                // Assert
                assertThat(results).hasSize(1).containsExactly(workOrder);
                verify(repository).findByFilters(status, doc, plate);
            }

            @Test
            @DisplayName("Should return empty list when no work orders match filters")
            void shouldReturnEmptyListWhenNoMatches() {
                // Arrange
                given(repository.findByFilters(null, null, null)).willReturn(Collections.emptyList());

                // Act
                List<WorkOrder> results = getWorkOrderUseCase.listByFilters(null, null, null);

                // Assert
                assertThat(results).isEmpty();
                verify(repository).findByFilters(null, null, null);
            }
        }
    }
}
