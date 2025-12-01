package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Entities.Rental;
import com.CarRentalSystem.CarRentals.Repositories.CarRepository;
import com.CarRentalSystem.CarRentals.Repositories.CustomerRepository;
import com.CarRentalSystem.CarRentals.Repositories.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalService {


    private final RentalRepository rentalRepository;


    private final CarRepository carRepository;


    private final CustomerRepository customerRepository;


    //GET ALL RENTALS
    public List<Rental> getAllRentals(){
        log.info("Fetching All Rentals");
        return rentalRepository.findAll();
    }

    //GET RENTAL BY ID
    public Optional<Rental> getRentalById(Integer rentalId){
        log.info("Fetching All Rentals By Rental Id:{}",rentalId);
        return rentalRepository.findById(rentalId);
    }

    //GET RENTALS BY CUSTOMER
    public List<Rental> getRentalsByCustomerId(Integer customerId){
        log.info("Fetching All Rentals By Customer Id:{}",customerId);
        return rentalRepository.findByCustomerCustomerId(customerId);
    }

    //RENT A CAR
    @Transactional
    public Rental rentACar(Integer carId,Integer customerId,Integer days) throws RuntimeException{

        log.info("Renting Process of Day Bases Started for customerId:{} and carId:{} for {} days",
                customerId,carId,days);

        if(days==null || days<=0){
            log.warn("Invalid Days Provided:{}",days);
            throw new RuntimeException("Days Should be Greater than Zero");
        }

        log.info("Fetching Customer With Id:{}",customerId);
        Customer customer=customerRepository.findById(customerId)
                .orElseThrow(()-> {
                    log.error("Customer With Id {} Not Found",customerId);
                    return new RuntimeException("Customer With Id "+customerId+" Not Found");
                });

        log.info("Fetching Car With Id:{}",carId);
        Car car=carRepository.findById(carId)
                .orElseThrow(()->{
                    log.error("Car With Id:{} Not Found",carId);
                    return new RuntimeException("Car With Id "+carId+" Not Found");
                });

        if(!car.getAvailable()){
            log.warn("Car With Id:{} is Not Available",carId);
            throw new RuntimeException("Car With Id:"+carId+" is Not Available");
        }

        Rental rental=new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setDays(days);

        LocalDateTime start=LocalDateTime.now();
        rental.setStartTime(start);

        LocalDateTime expected=start.plusDays(days).plusHours(1);
        rental.setExpectedReturnTime(expected);


        Integer totalPrice=car.getCarRentPerDay()*days;
        rental.setTotalPrice(totalPrice);

        rental.setHourly(false);
        rental.setHours(null);

        car.setAvailable(false);
        carRepository.save(car);

        log.info("Car With Id:{} Marked as Unavailable and Saved",carId);

        Rental saved=rentalRepository.save(rental);

        log.info("Rental Created With RentalId:{}",saved.getRentalId());
        return rentalRepository.save(rental);
    }

    //RENT A CAR IN HOURLY BASES
    @Transactional
    public Rental rentACarInHourly(Integer carId,Integer customerId,Integer hours) throws RuntimeException{

        log.info("Renting Process for Hourly Bases Started for customerId:{} and CarId:{} for {} Hours",
                customerId,carId,hours);

        if(hours==null || hours<=0){
            log.warn("Invalid Hours Provided:{}",hours);
            throw new RuntimeException("Hours should be greater than Zero");
        }

        log.info("Fetching Customer With Id:{}",customerId);
        Customer customer=customerRepository.findById(customerId)
                .orElseThrow(()-> {
                    log.error("Customer With Id:{} Not Found",customerId);
                            return new RuntimeException("Customer With Id "+customerId+" Not Found");
                        });

        log.info("Fetching Car With Id:{}",carId);
        Car car=carRepository.findById(carId)
                .orElseThrow(()->{
                    log.error("Car With Id:{} Not Found",carId);
                    return new RuntimeException("Car With Id "+carId+" Not Found");
                });

        if(!car.getAvailable()){
            log.warn("Car With Id:{} is Not Available",carId);
            throw new RuntimeException("Car With Id:"+carId+" is Not Available");
        }

        Rental rental=new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setHours(hours);
        rental.setDays(null);

        LocalDateTime start=LocalDateTime.now();
        rental.setStartTime(start);

        LocalDateTime expected=start.plusHours(hours).plusHours(1);
        rental.setExpectedReturnTime(expected);

        Integer rentPerDay=car.getCarRentPerDay();
        Integer hourlyRent=rentPerDay/24;
        Integer totalPrice=hourlyRent*hours;
        rental.setTotalPrice(totalPrice);

        rental.setHourly(true);

        car.setAvailable(false);
        carRepository.save(car);

        log.info("Car With Id:{} Marked as Unavailable and Saved",carId);

        Rental saved=rentalRepository.save(rental);

        log.info("Rental Created With Id:{}",saved.getRentalId());
        return saved;
    }

    //RETURN A CAR
    @Transactional
    public Rental returnACar(Integer rentalId){
        log.info("Fetching Rental With Rental Id:{}",rentalId);
        Rental rental=rentalRepository.findById(rentalId)
                .orElseThrow(()->{
                    log.error("Rental With Id:{} Not Found",rentalId);
                    return new RuntimeException("Rental With Id "+rentalId+" Not Found");
                });

        Car car=rental.getCar();

        LocalDateTime now=LocalDateTime.now();
        rental.setActualReturnTime(now);

        LocalDateTime expected=rental.getExpectedReturnTime();
        Integer totalPrice=rental.getTotalPrice();

        if(now.isAfter(expected)){
            long extraHours= ChronoUnit.HOURS.between(expected,now);

            Integer rentPerDay=car.getCarRentPerDay();
            Integer rentPerHour= rentPerDay/24;

            Integer fineHour=50;

            int extraCost= (int)(extraHours * (rentPerHour + fineHour));
            totalPrice+=extraCost;

            log.info("Late return for rentalId:{} extraHours:{} extraCost:{}", rentalId, extraHours, extraCost);
        }
        rental.setTotalPrice(totalPrice);

        car.setAvailable(true);
        carRepository.save(car);

        log.info("Car With Id:{} Marked as Available and Saved",car.getCarId());

        Rental saved=rentalRepository.save(rental);
        log.info("Rental with id:{} updated on return", saved.getRentalId());
        return saved;
    }

    //GET OVER DUES RENTALS
    public List<Rental> getAllOverdueRentals(){
        LocalDateTime now=LocalDateTime.now();

        return rentalRepository.findAll()
                .stream()
                .filter(rental -> rental.getActualReturnTime()==null)
                .filter(rental -> rental.getExpectedReturnTime()!=null
                        && rental.getExpectedReturnTime().isBefore(now))
                .toList();
    }

    //CANCEL A CAR
    @Transactional
    public String cancelCar(Integer rentalId){
        log.info("Fetching Rental With Rental Id:{}",rentalId);
        Rental rental=rentalRepository.findById(rentalId)
                .orElseThrow(()->{
                    log.error("Rental With Id:{} Not Found",rentalId);
                    return new RuntimeException("Rental Id With "+rentalId+" Not Found");
                });

        if(rental.getActualReturnTime()!=null){
            log.warn("Request To Cancel After Return for RentalId:{}",rentalId);
            throw new RuntimeException("Cannot Cancel A Rental That it has Already Returned");
        }

        Car car=rental.getCar();
        car.setAvailable(true);
        carRepository.save(car);

        log.info("Car With Id:{} Marked as Available and Saved",car.getCarId());

        rentalRepository.delete(rental);
        log.info("Car With Id:{} Cancelled and Deleted",rentalId);
        return "Rental Cancelled";
    }
}
