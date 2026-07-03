package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.CustomExceptions.Rentals.DamagedFeeNullException;
import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Entities.Rental;
import com.CarRentalSystem.CarRentals.Enums.RentalType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PricingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");
    private static final BigDecimal LATE_FINE_PER_HOUR = BigDecimal.valueOf(50);

    public BigDecimal calculateBasePrice(Car car, RentalType rentalType, int duration) {
        return switch (rentalType) {
            case DAILY -> BigDecimal.valueOf(car.getCarRentPerDay())
                    .multiply(BigDecimal.valueOf(duration));
            case HOURLY -> {
                BigDecimal hourlyRate = BigDecimal.valueOf(car.getCarRentPerDay())
                        .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
                yield hourlyRate.multiply(BigDecimal.valueOf(duration));
            }
        };
    }

    public BigDecimal calculateTax(BigDecimal taxableAmount) {
        return taxableAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDiscount(BigDecimal basePrice, RentalType rentalType, int duration) {
        BigDecimal discount = BigDecimal.ZERO;

        if (rentalType == RentalType.DAILY) {
            if (duration > 20) {
                discount = basePrice.multiply(new BigDecimal("0.20"));
            } else if (duration > 10) {
                discount = basePrice.multiply(new BigDecimal("0.15"));
            } else if (duration > 7) {
                discount = basePrice.multiply(new BigDecimal("0.10"));
            }
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public BookingPricing calculateBookingPricing(Car car, RentalType rentalType, int duration) {
        BigDecimal totalPrice = calculateBasePrice(car, rentalType, duration);
        BigDecimal tax = calculateTax(totalPrice);
        BigDecimal discount = calculateDiscount(totalPrice, rentalType, duration);
        BigDecimal grandTotal = totalPrice.add(tax).subtract(discount);

        return new BookingPricing(totalPrice, tax, discount, grandTotal);
    }

    public ReturnPricing calculateReturnPricing(
            Rental rental,
            Boolean damaged,
            BigDecimal damagedFee,
            LocalDateTime returnTime
    ) {
        BigDecimal totalPrice = rental.getTotalPrice();
        BigDecimal lateFee = calculateLateFee(rental, returnTime);
        BigDecimal damageCost = calculateDamageCost(damaged, damagedFee);

        BigDecimal subTotal = totalPrice.add(lateFee).add(damageCost);
        BigDecimal tax = calculateTax(subTotal);
        BigDecimal discount = calculateDiscount(totalPrice, rental.getRentalType(), rental.getDuration());
        BigDecimal grandTotal = subTotal.add(tax).subtract(discount);

        return new ReturnPricing(lateFee, damageCost, tax, discount, grandTotal, lateFee.signum() > 0);
    }

    public BigDecimal calculateLateFee(Rental rental, LocalDateTime returnTime) {
        LocalDateTime expected = rental.getExpectedReturnTime();
        if (returnTime == null || expected == null || !returnTime.isAfter(expected)) {
            return BigDecimal.ZERO;
        }

        long extraHours = Math.max(1, ChronoUnit.HOURS.between(expected, returnTime));
        BigDecimal rentPerHour = BigDecimal.valueOf(rental.getCar().getCarRentPerDay())
                .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);

        return rentPerHour.add(LATE_FINE_PER_HOUR).multiply(BigDecimal.valueOf(extraHours));
    }

    public BigDecimal calculateDamageCost(Boolean damaged, BigDecimal damagedFee) {
        if (!Boolean.TRUE.equals(damaged)) {
            return BigDecimal.ZERO;
        }
        if (damagedFee == null) {
            throw new DamagedFeeNullException();
        }
        return damagedFee;
    }

    public record BookingPricing(
            BigDecimal totalPrice,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal grandTotal
    ) {}

    public record ReturnPricing(
            BigDecimal lateFee,
            BigDecimal damageCost,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal grandTotal,
            boolean overdue
    ) {}
}
