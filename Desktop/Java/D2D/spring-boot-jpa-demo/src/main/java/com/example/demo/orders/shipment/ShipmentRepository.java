package com.example.demo.orders.shipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
        Optional<Shipment> findByTrackingNumber(String trackingNumber);

        @Query("SELECT s FROM Shipment s " +
                        "JOIN FETCH s.sender " +
                        "JOIN FETCH s.receiver " +
                        "JOIN FETCH s.packageDetails")
        List<Shipment> findAllWithDetails();

        @Query("SELECT s FROM Shipment s " +
                        "JOIN FETCH s.sender " +
                        "JOIN FETCH s.receiver " +
                        "JOIN FETCH s.packageDetails " +
                        "WHERE s.userId = :userId")
        List<Shipment> findAllByUserIdWithDetails(@Param("userId") String userId);

        @Query("SELECT s FROM Shipment s " +
                        "JOIN FETCH s.sender " +
                        "JOIN FETCH s.receiver " +
                        "JOIN FETCH s.packageDetails " +
                        "WHERE s.trackingNumber = :trackingNumber")
        Optional<Shipment> findByTrackingNumberWithDetails(@Param("trackingNumber") String trackingNumber);

        @Query("SELECT s FROM Shipment s " +
                        "JOIN FETCH s.sender " +
                        "JOIN FETCH s.receiver " +
                        "JOIN FETCH s.packageDetails " +
                        "WHERE s.status = :status")
        List<Shipment> findByStatusWithDetails(@Param("status") Shipment.ShipmentStatus status);

        long countByStatus(Shipment.ShipmentStatus status);

        @Query("SELECT s FROM Shipment s " +
                        "JOIN FETCH s.sender " +
                        "JOIN FETCH s.receiver " +
                        "JOIN FETCH s.packageDetails " +
                        "WHERE s.status IN :statuses")
        List<Shipment> findByStatusInWithDetails(@Param("statuses") List<Shipment.ShipmentStatus> statuses);

        @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Shipment s WHERE s.status = :status")
        java.math.BigDecimal sumTotalAmountByStatus(@Param("status") Shipment.ShipmentStatus status);

        @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'DELIVERED' AND s.updatedAt >= :startDate AND s.updatedAt < :endDate")
        long countDeliveredShipmentsBetweenDates(@Param("startDate") java.sql.Timestamp startDate, @Param("endDate") java.sql.Timestamp endDate);
}
