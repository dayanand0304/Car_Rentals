package com.CarRentalSystem.CarRentals.Events;

import java.time.LocalDateTime;

public record RentalOverdueEvent(
        Integer rentalId,
        Integer carId,
        Integer customerId,
        LocalDateTime expectedReturnTime,
        java.math.BigDecimal lateFee
) {}
