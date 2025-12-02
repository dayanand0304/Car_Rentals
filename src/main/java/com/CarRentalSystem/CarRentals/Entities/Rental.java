package com.CarRentalSystem.CarRentals.Entities;

import com.CarRentalSystem.CarRentals.DTO.BookingStatus;
import jakarta.persistence.*;
import jakarta.persistence.criteria.Fetch;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Rental_List")
@Data
public class Rental {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rentalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carId")
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId")
    private Customer customer;

    private Integer days;

    private Integer totalPrice;

    private Boolean hourly;

    private Integer hours;

    private LocalDateTime startTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status= BookingStatus.PENDING;
}
