package com.CarRentalSystem.CarRentals.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class BookingRequest {

    @NotNull
    private Integer carId;

    @NotNull
    private Integer userId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
