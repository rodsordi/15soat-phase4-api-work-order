package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.web.advice.CommonsExceptionAdvice;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateWorkOrderRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.UpdateWorkOrderStatusRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.port.in.CreateWorkOrderUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetWorkOrderUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateWorkOrderStatusUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkOrder Controller Web Layer Unit Tests")
class WorkOrderControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateWorkOrderUseCase createWorkOrderUseCase;

    @Mock
    private GetWorkOrderUseCase getWorkOrderUseCase;

    @Mock
    private UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase;

    @InjectMocks
    private WorkOrderController workOrderController;

    private Customer testCustomer;
    private Vehicle testVehicle;
    private WorkOrder testWorkOrder;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(workOrderController)
                .setControllerAdvice(new CommonsExceptionAdvice())
                .setValidator(validator)
                .build();

        testCustomer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "52998224725");
        testVehicle = Vehicle.create(testCustomer, "BRA2E19", "Toyota", "Corolla", 2023);
        testWorkOrder = WorkOrder.create(testCustomer, testVehicle, "General inspection and maintenance");
    }

    @Nested
    @DisplayName("Create Work Order Endpoints")
    class CreateWorkOrderTests {

        @Test
        @DisplayName("Should create work order successfully and return 201 Created")
        void shouldCreateWorkOrderSuccessfully() throws Exception {
            CreateWorkOrderRequest request = new CreateWorkOrderRequest("52998224725", "BRA2E19", "General inspection and maintenance");

            when(createWorkOrderUseCase.create(any(), any(), any())).thenReturn(testWorkOrder);

            mockMvc.perform(post("/api/v1/work-orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(testWorkOrder.getId().toString()))
                    .andExpect(jsonPath("$.customerDocument").value("52998224725"))
                    .andExpect(jsonPath("$.licensePlate").value("BRA2E19"))
                    .andExpect(jsonPath("$.status").value("RECEIVED"));
        }
    }

    @Nested
    @DisplayName("Get and Update Work Order Endpoints")
    class GetAndUpdateWorkOrderTests {

        @Test
        @DisplayName("Should get work order by ID and return 200 OK")
        void shouldGetWorkOrderById() throws Exception {
            UUID id = testWorkOrder.getId();
            when(getWorkOrderUseCase.getById(id)).thenReturn(testWorkOrder);

            mockMvc.perform(get("/api/v1/work-orders/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.customerDocument").value("52998224725"))
                    .andExpect(jsonPath("$.licensePlate").value("BRA2E19"));
        }

        @Test
        @DisplayName("Should update status to APPROVED and return 200 OK")
        void shouldUpdateWorkOrderStatus() throws Exception {
            UUID id = testWorkOrder.getId();
            UpdateWorkOrderStatusRequest request = new UpdateWorkOrderStatusRequest(WorkOrderStatus.APPROVED);
            request.setReason("Budget approved by customer");

            when(updateWorkOrderStatusUseCase.updateStatus(eq(id), any(), eq("Budget approved by customer"), any()))
                    .thenAnswer(inv -> {
                        testWorkOrder.startDiagnosis();
                        testWorkOrder.finishDiagnosisAndSendBudget();
                        testWorkOrder.approve();
                        return testWorkOrder;
                    });

            mockMvc.perform(patch("/api/v1/work-orders/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }
    }
}
