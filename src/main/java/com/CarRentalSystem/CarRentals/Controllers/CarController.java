package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Services.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarController {


    private final CarService carService;

    //GET CAR DETAILS
    @GetMapping("/get-all")
    public ResponseEntity<List<Car>> getAllCars(){
        List<Car> cars=carService.getAllCars();
        return ResponseEntity.ok(cars);
    }

    //GET AVAILABLE CARS
    @GetMapping("/get-available-cars")
    public ResponseEntity<List<Car>> getAvailableCars(){
        List<Car> cars=carService.getAvailableCars();
        return ResponseEntity.ok(cars);
    }

    //GET CAR DETAILS BY ID
    @GetMapping("/get-car/{carId}")
    public ResponseEntity<Car> getCarById(@PathVariable Integer carId){
        Optional<Car> car=carService.carListById(carId);
        return car.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    //GET CAR DETAILS BY CAR BRAND AND MODEL OR WITH ONLY CAR BRAND
    @GetMapping("/search")
    public ResponseEntity<List<Car>> getCarsByBrand(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model) {

        if (brand != null && model != null) {
            return ResponseEntity.ok(carService.carListByBrandAndModel(brand,model));
        } else if (brand != null) {
            return ResponseEntity.ok(carService.carListByBrand(brand));
        }else {
            return ResponseEntity.badRequest().build(); // or return all cars
        }
    }

    //CHECK IF CAR IS AVAILABLE OR NOT
    @GetMapping("/{carId}/available")
    public ResponseEntity<Boolean> isAvailable(@PathVariable Integer carId){
        Boolean car=carService.isAvailable(carId,true);
        return ResponseEntity.ok(car);
    }


    //ADD CAR
    @PostMapping("/add-car")
    public ResponseEntity<Car> addCar(@RequestBody Car car){
        Car newCar=carService.addCar(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCar);
    }

    //DELETE CAR
    @DeleteMapping("/delete-car/{carId}")
    public ResponseEntity<String> deleteCar(@PathVariable Integer carId){
        String message=carService.deleteCar(carId);
        return ResponseEntity.ok(message);
    }

    //EDIT CAR DETAILS
    @PutMapping("/update-car/{carId}")
    public ResponseEntity<Car> updateCarDetails(@PathVariable Integer carId,
                                                @RequestBody Car update){
        Car car=carService.updateCarDetails(carId,update);
        if(car!=null){
            return ResponseEntity.ok(car);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
