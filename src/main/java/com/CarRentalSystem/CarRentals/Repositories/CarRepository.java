package com.CarRentalSystem.CarRentals.Repositories;

import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Enums.FuelType;
import com.CarRentalSystem.CarRentals.Enums.SeatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;


import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car,Integer> {

    Page<Car> findByCarBrandContainingIgnoreCase(String carBrand,Pageable pageable);
    Page<Car> findByCarBrandContainingIgnoreCaseAndCarModelContainingIgnoreCase(String carBrand, String carModel,Pageable pageable);
    Page<Car> findByCarBrandContainingIgnoreCaseAndCarModelContainingIgnoreCaseAndAvailable(
            String carBrand,
            String carModel,
            Boolean available,
            Pageable pageable);
    Page<Car> findByFuelType(FuelType fuelType,Pageable pageable);
    Page<Car> findBySeats(SeatType seats,Pageable pageable);
    Page<Car> findByAvailableTrue(Pageable pageable);
    Page<Car> findByAvailable(Boolean available, Pageable pageable);
    Page<Car> findByCarBrandContainingIgnoreCaseAndAvailableTrue(String carBrand,Pageable pageable);
    Page<Car> findByCarBrandContainingIgnoreCaseAndAvailable(String carBrand, Boolean available, Pageable pageable);
    Page<Car> findByCarRentPerDayBetween(Integer min,Integer max,Pageable pageable);

    Optional<Car> findByRegistrationNumber(String number);
    Optional<Car> findByRegistrationNumberIgnoreCase(String number);
    Page<Car> findByRegistrationNumberEndingWith(String number,Pageable pageable);
    boolean existsByRegistrationNumber(String number);

    Optional<Car> findByCarIdAndActiveTrue(Integer carId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.carId = :carId")
    Optional<Car> findByIdForUpdate(@Param("carId") Integer carId);

    Page<Car> findAllByActiveTrue(Pageable pageable);
    Page<Car> findByActive(Boolean active,Pageable pageable);
}
