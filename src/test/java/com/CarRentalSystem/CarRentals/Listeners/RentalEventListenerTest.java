package com.CarRentalSystem.CarRentals.Listeners;

import com.CarRentalSystem.CarRentals.Events.BookingCompletedEvent;
import com.CarRentalSystem.CarRentals.Events.CarDamagedEvent;
import com.CarRentalSystem.CarRentals.Events.RentalOverdueEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
class RentalEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void publishRentalEvents_doesNotFailContext() {
        eventPublisher.publishEvent(new BookingCompletedEvent(1, 2, 3, new BigDecimal("1500.00")));
        eventPublisher.publishEvent(new CarDamagedEvent(1, 2, 3, new BigDecimal("500.00")));
        eventPublisher.publishEvent(new RentalOverdueEvent(
                1, 2, 3, LocalDateTime.now().minusHours(2), new BigDecimal("250.00")
        ));
    }
}
