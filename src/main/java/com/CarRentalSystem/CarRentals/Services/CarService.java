package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Repositories.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class CarService {


    private final CarRepository carRepository;

    //GET CAR DETAILS
    public List<Car> getAllCars(){
        log.info("Fetching All Cars");
        return carRepository.findAll();
    }

    //GET AVAILABLE CARS
    public List<Car> getAvailableCars(){
        log.info("Fetching Available Cars");
        return carRepository.findByAvailable(true);
    }

    //GET CAR DETAILS BY BRAND
    public List<Car> carListByBrand(String carBrand){
        log.info("Fetching All Cars By BrandName:{}",carBrand);
        return carRepository.findByCarBrand(carBrand);
    }

    //GET CAR DETAILS BY CAR BRAND AND MODEL
    public List<Car> carListByBrandAndModel(String carBrand,String carModel){
        log.info("Fetching All Cars By BrandName:{} and CarModel:{}",carBrand,carModel);
        return carRepository.findByCarBrandAndCarModel(carBrand,carModel);
    }

    //GET CAR DETAILS BY ID
    public Optional<Car> carListById(Integer carId) {
        log.info("Fetching All Cars By carId:{}",carId);
        return carRepository.findById(carId);
    }

    //CHECK IF CAR IS AVAILABLE OR NOT
    public boolean isAvailable(Integer carId,boolean available){
        log.debug("checking Availability of CarId:{} isAvailabel:{}",carId,available);
        return carRepository.existsByCarIdAndAvailable(carId,available);
    }

    //ADD CAR
    public Car addCar(Car car){
        log.info("Adding New Car Of Brand:{} and Model:{}", car.getCarBrand(),car.getCarModel());
        Car saved=carRepository.save(car);
        log.info("Added New Car with id:{}",saved.getCarId());
        return saved;
    }

    //DELETE CAR
    public String deleteCar(Integer carId){
        log.info("Attempting to delete car with id: {}", carId);
        if(carRepository.existsById(carId)){
            carRepository.deleteById(carId);
            log.info("Car With Id:{} is Deleted",carId);
            return "Car Deleted";
        }
        log.warn("Car With Id:{} Not",carId);
        return "Car Not Found";
    }

    //EDIT CAR DETAILS
    public Car updateCarDetails(Integer carId,Car updatedDetails){
        log.info("Updating car with id: {}", carId);
        return carRepository.findById(carId)
                .map(existingDetails->{
                    if(updatedDetails.getCarBrand()!=null){
                        existingDetails.setCarBrand(updatedDetails.getCarBrand());
                    }
                    if(updatedDetails.getCarModel()!=null){
                        existingDetails.setCarModel(updatedDetails.getCarModel());
                    }
                    if(updatedDetails.getCarRentPerDay()!=null){
                        existingDetails.setCarRentPerDay(updatedDetails.getCarRentPerDay());
                    }
                    existingDetails.setAvailable(updatedDetails.getAvailable());

                    Car saved=carRepository.save(existingDetails);
                    log.info("Car with id:{} updated successfully", saved.getCarId());
                    return saved;

                })
                .orElse(null);
    }
}
