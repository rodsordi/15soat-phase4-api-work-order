package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    @Query("SELECT w FROM WorkOrder w WHERE " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:customerDocument IS NULL OR w.customerDocument = :customerDocument) AND " +
           "(:licensePlate IS NULL OR w.licensePlate = :licensePlate) " +
           "ORDER BY w.createdAt DESC")
    List<WorkOrder> findByFilters(
            @Param("status") WorkOrderStatus status,
            @Param("customerDocument") String customerDocument,
            @Param("licensePlate") String licensePlate
    );
}
