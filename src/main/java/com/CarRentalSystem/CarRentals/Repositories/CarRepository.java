package com.CarRentalSystem.CarRentals.Repositories;

import com.CarRentalSystem.CarRentals.Entities.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car,Integer> {
    List<Car> findByCarBrand(String carBrand);
    List<Car> findByCarBrandAndCarModel(String carBrand, String carModel);
    List<Car> findByAvailable(boolean available);
    Boolean existsByCarIdAndAvailable(Integer carId, boolean available);       // returns true/false
}
