package com.CarRentalSystem.CarRentals.integration;

import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Enums.FuelType;
import com.CarRentalSystem.CarRentals.Enums.Role;
import com.CarRentalSystem.CarRentals.Enums.SeatType;
import com.CarRentalSystem.CarRentals.Repositories.CarRepository;
import com.CarRentalSystem.CarRentals.Repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class SoftDeleteIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        carRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void findByActiveFalse_returnsOnlyInactiveCars() {
        carRepository.save(activeCar("AP01AB1111", true));
        carRepository.save(activeCar("AP01AB2222", false));

        List<Car> inactiveCars = carRepository.findByActive(false, PageRequest.of(0, 10)).getContent();

        assertThat(inactiveCars).hasSize(1);
        assertThat(inactiveCars.get(0).getRegistrationNumber()).isEqualTo("AP01AB2222");
        assertThat(inactiveCars.get(0).getDeletedAt()).isNotNull();
    }

    @Test
    void findByCarIdAndActiveTrue_excludesInactiveCar() {
        Car inactiveCar = activeCar("AP01AB3333", false);
        inactiveCar = carRepository.save(inactiveCar);

        assertThat(carRepository.findByCarIdAndActiveTrue(inactiveCar.getCarId())).isEmpty();
    }

    @Test
    void findByAvailableTrueAndActiveTrue_excludesInactiveAvailableCars() {
        Car inactiveAvailable = activeCar("AP01AB4444", false);
        inactiveAvailable.setAvailable(true);
        carRepository.save(inactiveAvailable);
        carRepository.save(activeCar("AP01AB5555", true));

        List<Car> availableActiveCars = carRepository
                .findByAvailableTrueAndActiveTrue(PageRequest.of(0, 10))
                .getContent();

        assertThat(availableActiveCars).extracting(Car::getRegistrationNumber)
                .containsExactly("AP01AB5555");
    }

    @Test
    void findByActiveFalse_returnsOnlyInactiveCustomers() {
        customerRepository.save(activeCustomer("active@example.com", true));
        customerRepository.save(activeCustomer("inactive@example.com", false));

        List<Customer> inactiveCustomers = customerRepository
                .findByActive(false, PageRequest.of(0, 10))
                .getContent();

        assertThat(inactiveCustomers).hasSize(1);
        assertThat(inactiveCustomers.get(0).getCustomerEmail()).isEqualTo("inactive@example.com");
    }

    private Car activeCar(String registrationNumber, boolean active) {
        Car car = new Car();
        car.setCarBrand("Toyota");
        car.setCarModel("Innova");
        car.setRegistrationNumber(registrationNumber);
        car.setFuelType(FuelType.PETROL);
        car.setSeats(SeatType.FIVE);
        car.setCarRentPerDay(1500);
        car.setAvailable(true);
        car.setActive(active);
        if (!active) {
            car.setDeletedAt(LocalDateTime.now());
            car.setAvailable(false);
        }
        return car;
    }

    private Customer activeCustomer(String email, boolean active) {
        Customer customer = new Customer();
        customer.setCustomerName("Test User");
        customer.setCustomerEmail(email);
        customer.setPassword("encoded-password");
        customer.setRole(Role.CUSTOMER);
        customer.setActive(active);
        if (!active) {
            customer.setDeletedAt(LocalDateTime.now());
        }
        return customer;
    }
}
