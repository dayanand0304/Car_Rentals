package com.CarRentalSystem.CarRentals.Events;

import java.math.BigDecimal;

public record CarDamagedEvent(
        Integer rentalId,
        Integer carId,
        Integer customerId,
        BigDecimal damageFee
) {}
