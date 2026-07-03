package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.CustomExceptions.Rentals.DamagedFeeNullException;
import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Entities.Rental;
import com.CarRentalSystem.CarRentals.Enums.BookingStatus;
import com.CarRentalSystem.CarRentals.Enums.FuelType;
import com.CarRentalSystem.CarRentals.Enums.RentalType;
import com.CarRentalSystem.CarRentals.Enums.Role;
import com.CarRentalSystem.CarRentals.Enums.SeatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingServiceTest {

    private PricingService pricingService;
    private Car car;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        car = new Car();
        car.setCarId(1);
        car.setCarBrand("Toyota");
        car.setCarModel("Innova");
        car.setRegistrationNumber("AP01AB1234");
        car.setFuelType(FuelType.PETROL);
        car.setSeats(SeatType.FIVE);
        car.setCarRentPerDay(2400);
        car.setAvailable(true);
        car.setActive(true);
    }

    @Test
    void calculateBasePrice_dailyRental_multipliesRentPerDayByDuration() {
        BigDecimal price = pricingService.calculateBasePrice(car, RentalType.DAILY, 3);

        assertThat(price).isEqualByComparingTo("7200");
    }

    @Test
    void calculateBasePrice_hourlyRental_dividesDailyRateByTwentyFour() {
        BigDecimal price = pricingService.calculateBasePrice(car, RentalType.HOURLY, 6);

        assertThat(price).isEqualByComparingTo("600.00");
    }

    @Test
    void calculateTax_appliesEighteenPercentRate() {
        BigDecimal tax = pricingService.calculateTax(new BigDecimal("1000"));

        assertThat(tax).isEqualByComparingTo("180.00");
    }

    @Test
    void calculateDiscount_dailyRentalOverSevenDays_appliesTenPercent() {
        BigDecimal basePrice = new BigDecimal("10000");
        BigDecimal discount = pricingService.calculateDiscount(basePrice, RentalType.DAILY, 8);

        assertThat(discount).isEqualByComparingTo("1000.00");
    }

    @Test
    void calculateDiscount_hourlyRental_hasNoDiscount() {
        BigDecimal discount = pricingService.calculateDiscount(new BigDecimal("500"), RentalType.HOURLY, 24);

        assertThat(discount).isEqualByComparingTo("0.00");
    }

    @Test
    void calculateBookingPricing_combinesBaseTaxAndDiscount() {
        PricingService.BookingPricing pricing = pricingService.calculateBookingPricing(car, RentalType.DAILY, 1);

        assertThat(pricing.totalPrice()).isEqualByComparingTo("2400");
        assertThat(pricing.taxAmount()).isEqualByComparingTo("432.00");
        assertThat(pricing.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(pricing.grandTotal()).isEqualByComparingTo("2832.00");
    }

    @Test
    void calculateLateFee_onTimeReturn_sameDayAsPickup_hasZeroLateFee() {
        Rental rental = buildRental(RentalType.HOURLY, 4);
        LocalDateTime returnTime = rental.getExpectedReturnTime();

        BigDecimal lateFee = pricingService.calculateLateFee(rental, returnTime);

        assertThat(lateFee).isEqualByComparingTo("0");
    }

    @Test
    void calculateLateFee_overdueReturn_chargesAtLeastOneHour() {
        Rental rental = buildRental(RentalType.HOURLY, 2);
        LocalDateTime returnTime = rental.getExpectedReturnTime().plusHours(3);

        BigDecimal lateFee = pricingService.calculateLateFee(rental, returnTime);

        assertThat(lateFee).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void calculateDamageCost_whenDamagedWithoutFee_throwsException() {
        assertThatThrownBy(() -> pricingService.calculateDamageCost(true, null))
                .isInstanceOf(DamagedFeeNullException.class);
    }

    @Test
    void calculateReturnPricing_damageAndLateReturn_combinesBothCharges() {
        Rental rental = buildRental(RentalType.DAILY, 1);
        rental.setTotalPrice(new BigDecimal("2400"));
        rental.setTaxAmount(new BigDecimal("432.00"));
        rental.setDiscountAmount(BigDecimal.ZERO);
        rental.setGrandTotal(new BigDecimal("2832.00"));

        LocalDateTime returnTime = rental.getExpectedReturnTime().plusHours(5);
        BigDecimal damageFee = new BigDecimal("1500");

        PricingService.ReturnPricing pricing = pricingService.calculateReturnPricing(
                rental, true, damageFee, returnTime
        );

        assertThat(pricing.damageCost()).isEqualByComparingTo("1500");
        assertThat(pricing.lateFee()).isGreaterThan(BigDecimal.ZERO);
        assertThat(pricing.grandTotal())
                .isEqualByComparingTo(
                        rental.getTotalPrice()
                                .add(pricing.lateFee())
                                .add(damageFee)
                                .add(pricing.taxAmount())
                                .subtract(pricing.discountAmount())
                );
        assertThat(pricing.overdue()).isTrue();
    }

    private Rental buildRental(RentalType rentalType, int duration) {
        Customer customer = new Customer();
        customer.setCustomerId(10);
        customer.setCustomerName("Test User");
        customer.setCustomerEmail("test@example.com");
        customer.setPassword("secret");
        customer.setRole(Role.CUSTOMER);
        customer.setActive(true);

        Rental rental = new Rental();
        rental.setRentalId(100);
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setRentalType(rentalType);
        rental.setDuration(duration);
        rental.setStatus(BookingStatus.CONFIRMED);
        rental.setStartTime(LocalDateTime.of(2026, 7, 1, 10, 0));
        rental.setExpectedReturnTime(
                rentalType == RentalType.DAILY
                        ? rental.getStartTime().plusDays(duration)
                        : rental.getStartTime().plusHours(duration)
        );
        rental.setTotalPrice(pricingService.calculateBasePrice(car, rentalType, duration));
        rental.setTaxAmount(pricingService.calculateTax(rental.getTotalPrice()));
        rental.setDiscountAmount(BigDecimal.ZERO);
        rental.setGrandTotal(rental.getTotalPrice().add(rental.getTaxAmount()));
        return rental;
    }
}
