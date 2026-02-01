package com.roomreservationservice.model;

import com.roomreservationservice.enums.PaymentMode;
import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.enums.RoomSegment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "reservations")
@Getter
@Setter
@RequiredArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    int roomNumber;

    @Column(nullable = false)
    String customerName;

    @Column(nullable = false)
    LocalDate startDate;

    @Column(nullable = false)
    LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RoomSegment roomSegment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMode paymentMode;


    @Enumerated(EnumType.STRING)
    ReservationStatus status;


    String paymentReference;

    long totalAmount;

    @Version
    private Long version;

}

