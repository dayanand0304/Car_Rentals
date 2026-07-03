package com.CarRentalSystem.CarRentals.Events;

public record BookingCompletedEvent(
        Integer rentalId,
        Integer carId,
        Integer customerId,
        java.math.BigDecimal grandTotal
) {}
