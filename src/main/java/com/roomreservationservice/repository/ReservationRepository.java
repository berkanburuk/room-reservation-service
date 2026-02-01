package com.roomreservationservice.repository;

import com.roomreservationservice.enums.PaymentMode;
import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.model.Reservation;
import com.roomreservationservice.repository.projection.ReservationSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("SELECT ReservationSummary" +
            "(r.id, r.endDate, r.status) " +
            "FROM Reservation r " +
            "WHERE r.startDate >= :targetDate " +
            "AND r.paymentMode = :paymentMode " +
            "AND r.status = :status")
    List<ReservationSummary> findPendingBankTransfers(
            @Param("targetDate") LocalDate targetDate,
            @Param("paymentMode") PaymentMode paymentMode,
            @Param("status") ReservationStatus status
    );


    @Modifying
    @Query("UPDATE Reservation r SET r.status = :status " +
            "WHERE r.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") ReservationStatus status);

    @Query("""
                SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
                FROM Reservation r
                WHERE r.roomNumber = :roomNumber
                  AND r.status != 'CANCELLED'
                  AND r.startDate < :endDate
                  AND r.endDate > :startDate
            """)
    boolean existsByRoomNumberAndDateRange(
            @Param("roomNumber") int roomNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
