package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.Entities.Rental;
import com.CarRentalSystem.CarRentals.Services.CarService;
import com.CarRentalSystem.CarRentals.Services.CustomerService;
import com.CarRentalSystem.CarRentals.Services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CarService carService;

    //GET ALL RENTALS
    @GetMapping("/get-all")
    public ResponseEntity<List<Rental>> getAllRentals(){
        List<Rental> rentals=rentalService.getAllRentals();
        return ResponseEntity.ok(rentals);
    }

    //GET RENTAL BY ID
    @GetMapping("/get/rentalId/{rentalId}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Integer rentalId){
        Optional<Rental> rental=rentalService.getRentalById(rentalId);
        return rental.map(ResponseEntity::ok).
                orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    //GET RENTALS BY CUSTOMER
    @GetMapping("/get/customerId/{customerId}")
    public ResponseEntity<List<Rental>> getRentalByCustomer(@PathVariable Integer customerId){
        List<Rental> rentals=rentalService.getRentalsByCustomerId(customerId);
        return ResponseEntity.ok(rentals);
    }

    //RENT A CAR IN DAY BASES
    @PostMapping("/rent-car")
    public ResponseEntity<Rental> rentACar(@RequestParam Integer carId,
                                           @RequestParam Integer customerId,
                                           @RequestParam Integer days){
        Rental rental=rentalService.rentACar(carId,customerId,days);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }


    //RENT A CAR BY HOURLY BASES
    @PostMapping("/rent-car-hourly")
    public ResponseEntity<Rental> rentACarHourly(@RequestParam Integer carId,
                                           @RequestParam Integer customerId,
                                           @RequestParam Integer hours){
        Rental rental=rentalService.rentACarInHourly(carId,customerId,hours);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    //RETURN A CAR
    @PutMapping("/return-car/{rentalId}")
    public ResponseEntity<Rental> returnCar(@PathVariable Integer rentalId){
        Rental rental=rentalService.returnACar(rentalId);
        return ResponseEntity.ok(rental);
    }

    //GET OVER DUES RENTALS
    @GetMapping("/get-overDues")
    public ResponseEntity<List<Rental>> getOverDues(){
        List<Rental> rentals=rentalService.getAllOverdueRentals();
        return ResponseEntity.ok(rentals);
    }

    //CANCEL RENTAL
    @DeleteMapping("/cancel-rental/{rentalId}")
    public ResponseEntity<String> cancelRental(@PathVariable Integer rentalId){
        String message=rentalService.cancelCar(rentalId);
        return ResponseEntity.ok(message);
    }
}
