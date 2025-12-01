package com.CarRentalSystem.CarRentals.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Rental_List")
@Data
public class Rental {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rentalId;

    @ManyToOne
    @JoinColumn(name = "carId")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "customerId")
    private Customer customer;

    private Integer days;

    private Integer totalPrice;

    private Boolean hourly;

    private Integer hours;

    private LocalDateTime startTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
}
