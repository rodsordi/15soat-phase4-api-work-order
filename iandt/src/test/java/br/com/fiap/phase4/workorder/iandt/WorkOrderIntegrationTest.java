package br.com.fiap.phase4.workorder.iandt;

import br.com.fiap.phase4.commons.integration.setup.KafkaSetup;
import br.com.fiap.phase4.commons.integration.setup.PostgresSetup;
import br.com.fiap.phase4.workorder.WorkOrderApplication;
import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataCustomerRepository;
import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataMaterialRepository;
import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataServiceCatalogRepository;
import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataVehicleRepository;
import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataWorkOrderRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WorkOrderApplication.class)
@ActiveProfiles("test")
public abstract class WorkOrderIntegrationTest implements PostgresSetup, KafkaSetup {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderIntegrationTest.class);

    @LocalServerPort
    protected Integer port;

    @Autowired
    protected SpringDataCustomerRepository customerRepository;

    @Autowired
    protected SpringDataVehicleRepository vehicleRepository;

    @Autowired
    protected SpringDataMaterialRepository materialRepository;

    @Autowired
    protected SpringDataServiceCatalogRepository serviceCatalogRepository;

    @Autowired
    protected SpringDataWorkOrderRepository workOrderRepository;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, Object> consumerFactory;

    @BeforeEach
    void setUpBase() {
        RestAssured.baseURI = String.format("http://localhost:%s", port);
        cleanDatabase();
    }

    protected void cleanDatabase() {
        log.info("Cleaning relational tables before integration test execution");
        try {
            workOrderRepository.deleteAll();
            vehicleRepository.deleteAll();
            customerRepository.deleteAll();
            materialRepository.deleteAll();
            serviceCatalogRepository.deleteAll();
        } catch (Exception ex) {
            log.warn("Error while cleaning relational tables: {}", ex.getMessage());
        }
    }
}
