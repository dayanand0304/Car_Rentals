package com.CarRentalSystem.CarRentals.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {

    private Integer rentalId;

    private Integer carId;

    private Integer userId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalPrice;

    private BookingStatus status;
}
