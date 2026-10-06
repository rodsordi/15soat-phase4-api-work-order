package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.web.advice.CommonsExceptionAdvice;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateCustomerRequest;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.port.in.CreateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetCustomerUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Controller Web Layer Validation Tests")
class CustomerControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateCustomerUseCase createCustomerUseCase;

    @Mock
    private GetCustomerUseCase getCustomerUseCase;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new CommonsExceptionAdvice())
                .setValidator(validator)
                .build();
    }

    @Nested
    @DisplayName("Bean Validation (@Valid) Constraint Tests")
    class BeanValidationConstraintTests {

        @Test
        @DisplayName("Should return 400 Bad Request with fieldErrors when email is invalid")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            CreateCustomerRequest request = new CreateCustomerRequest("52998224725", "Rodrigo Sordi", "invalid-email-format");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.fieldErrors.email").exists());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when required document is null")
        void shouldReturn400WhenDocumentIsNull() throws Exception {
            CreateCustomerRequest request = new CreateCustomerRequest(null, "Rodrigo Sordi", "rodrigo@fiap.com.br");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.fieldErrors.document").exists());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when required name is null")
        void shouldReturn400WhenNameIsNull() throws Exception {
            CreateCustomerRequest request = new CreateCustomerRequest("52998224725", null, "rodrigo@fiap.com.br");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.fieldErrors.name").exists());
        }

        @Test
        @DisplayName("Should return 201 Created when request payload satisfies all constraints")
        void shouldReturn201WhenPayloadIsValid() throws Exception {
            CreateCustomerRequest request = new CreateCustomerRequest("52998224725", "Rodrigo Sordi", "rodrigo@fiap.com.br");
            Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "52998224725");

            when(createCustomerUseCase.createCustomer(any(), any(), any())).thenReturn(customer);

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.document").value("52998224725"))
                    .andExpect(jsonPath("$.name").value("Rodrigo Sordi"))
                    .andExpect(jsonPath("$.email").value("rodrigo@fiap.com.br"));
        }
    }
}
