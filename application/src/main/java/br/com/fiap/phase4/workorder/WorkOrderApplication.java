package br.com.fiap.phase4.workorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"br.com.fiap.phase4"})
@EnableJpaAuditing
@EnableKafka
@EnableScheduling
public class WorkOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkOrderApplication.class, args);
    }
}
