package com.CarRentalSystem.CarRentals.Listeners;

import com.CarRentalSystem.CarRentals.Events.BookingCompletedEvent;
import com.CarRentalSystem.CarRentals.Events.CarDamagedEvent;
import com.CarRentalSystem.CarRentals.Events.RentalOverdueEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RentalEventListener {

    @EventListener
    public void onBookingCompleted(BookingCompletedEvent event) {
        log.info(
                "Booking completed notification: rentalId={}, carId={}, customerId={}, grandTotal={}",
                event.rentalId(),
                event.carId(),
                event.customerId(),
                event.grandTotal()
        );
    }

    @EventListener
    public void onCarDamaged(CarDamagedEvent event) {
        log.info(
                "Repair tracking started: rentalId={}, carId={}, customerId={}, damageFee={}",
                event.rentalId(),
                event.carId(),
                event.customerId(),
                event.damageFee()
        );
    }

    @EventListener
    public void onRentalOverdue(RentalOverdueEvent event) {
        log.info(
                "Overdue rental alert: rentalId={}, carId={}, customerId={}, expectedReturn={}, lateFee={}",
                event.rentalId(),
                event.carId(),
                event.customerId(),
                event.expectedReturnTime(),
                event.lateFee()
        );
    }
}
