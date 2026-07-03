package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.CustomExceptions.Cars.CarNotAvailableException;
import com.CarRentalSystem.CarRentals.CustomExceptions.Cars.CarNotFoundException;
import com.CarRentalSystem.CarRentals.CustomExceptions.Cars.CarNotActiveException;
import com.CarRentalSystem.CarRentals.CustomExceptions.Customers.CustomerInActiveException;
import com.CarRentalSystem.CarRentals.CustomExceptions.Customers.CustomerNotFoundException;
import com.CarRentalSystem.CarRentals.CustomExceptions.Rentals.*;
import com.CarRentalSystem.CarRentals.DTO.PageMapper;
import com.CarRentalSystem.CarRentals.DTO.RentalMapper;
import com.CarRentalSystem.CarRentals.DTO.Response.PageResponse;
import com.CarRentalSystem.CarRentals.DTO.Response.RentalResponse;
import com.CarRentalSystem.CarRentals.Enums.BookingStatus;
import com.CarRentalSystem.CarRentals.Enums.RentalType;
import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Entities.Rental;
import com.CarRentalSystem.CarRentals.Events.BookingCompletedEvent;
import com.CarRentalSystem.CarRentals.Events.CarDamagedEvent;
import com.CarRentalSystem.CarRentals.Events.RentalOverdueEvent;
import com.CarRentalSystem.CarRentals.Repositories.CarRepository;
import com.CarRentalSystem.CarRentals.Repositories.CustomerRepository;
import com.CarRentalSystem.CarRentals.Repositories.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalService {


    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final CarCacheService carCacheService;


    //1.GET ALL RENTALS
    public PageResponse<RentalResponse> getAllRentals(Pageable pageable){
        log.info("Fetching All Rentals");
        Page<Rental> page=rentalRepository.findAll(pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //2.GET RENTAL BY ID
    public RentalResponse getRentalByRentalId(Integer rentalId){
        log.info("Fetching All Rentals By Rental Id:{}",rentalId);
        Rental rental=rentalRepository.findByRentalId(rentalId)
                .orElseThrow(()->new RentalNotFoundException(rentalId));

        return RentalMapper.response(rental);
    }

    //3.GET RENTALS BY CUSTOMER ID
    public PageResponse<RentalResponse> getRentalsByCustomerId(Integer customerId,Pageable pageable){
        log.info("Fetching All Rentals By Customer Id:{}",customerId);
        Page<Rental> page=rentalRepository.findByCustomerCustomerId(customerId,pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //4.GET RENTALS BY CAR ID
    public PageResponse<RentalResponse> getRentalByCarId(Integer carId,Pageable pageable){
        log.info("Fetching All Rentals By Car Id:{}",carId);
        Page<Rental> page=rentalRepository.findByCarCarId(carId,pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //5.GET RENTALS BY STATUS
    public PageResponse<RentalResponse> getRentalByStatus(BookingStatus status,Pageable pageable){
        log.info("Fetching All Rentals By status:{}",status);
        Page<Rental> page=rentalRepository.findByStatus(status,pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //GET RENTALS BY RENTAL TYPE
    public PageResponse<RentalResponse> getRentalsByRentalType(RentalType rentalType,Pageable pageable){
        Page<Rental> page=rentalRepository.findByRentalType(rentalType,pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //GET ALL DAMAGED RENTALS
    public PageResponse<RentalResponse> getRentalsOfDamaged(Pageable pageable){
        Page<Rental> page=rentalRepository.findByDamagedTrue(pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //GET OVER DUES RENTALS
    public PageResponse<RentalResponse> getAllOverdueRentals(Pageable pageable){
        LocalDateTime now=LocalDateTime.now();
        Page<Rental> page=rentalRepository.findByActualReturnTimeIsNullAndExpectedReturnTimeBefore(now,pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //6.FIND BY CUSTOMER ID AND STATUS
    public PageResponse<RentalResponse> getRentalByCustomerIdAndStatus(Integer customerId,BookingStatus status,Pageable pageable){
        log.info("Fetching All Rentals By Customer Id:{} With {}",customerId,status);
        Page<Rental> page=rentalRepository.findByCustomerCustomerIdAndStatus(customerId,status,pageable);

        return PageMapper.toPageResponse(page,RentalMapper::response);
    }

    //RENT A CAR
    @Transactional
    public RentalResponse rentACar(Integer carId,
                           Integer customerId,
                           RentalType rentalType,
                           Integer duration,
                           String requesterEmail,
                           boolean admin){

        log.info("Renting Process of {} Bases Started for customerId:{} and carId:{}",
                rentalType,customerId,carId);

        if(duration ==null || duration <=0){
            log.warn("Invalid Duration Provided:{}", duration);
            throw new DurationException(duration);
        }

        Customer customer = resolveBookingCustomer(customerId, requesterEmail, admin);

        if(!customer.isActive()){
            throw new CustomerInActiveException();
        }

        log.info("Fetching Car With Id:{}",carId);
        Car car=carRepository.findByIdForUpdate(carId)
                .orElseThrow(()->{
                    log.error("Car With Id:{} Not Found",carId);
                    return new CarNotFoundException(carId);
                });

        if(!car.getActive()){
            throw new CarNotActiveException(carId);
        }

        if(!car.getAvailable()){
            log.warn("Car With Id:{} is Not Available",carId);
            throw new CarNotAvailableException(carId);
        }

        if (rentalType == null) {
            throw new RentalTypeException(rentalType);
        }

        Rental rental=new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setRentalType(rentalType);
        rental.setDuration(duration);


        rental.setStartTime(LocalDateTime.now());
        rental.setStatus(BookingStatus.CONFIRMED);

        if(rentalType==RentalType.DAILY){
            rental.setExpectedReturnTime(rental.getStartTime().plusDays(duration));
        }else{
            rental.setExpectedReturnTime(rental.getStartTime().plusHours(duration));
        }

        PricingService.BookingPricing pricing = pricingService.calculateBookingPricing(car, rentalType, duration);

        rental.setTotalPrice(pricing.totalPrice());
        rental.setTaxAmount(pricing.taxAmount());
        rental.setDiscountAmount(pricing.discountAmount());
        rental.setGrandTotal(pricing.grandTotal());

        car.setAvailable(false);
        carRepository.save(car);
        carCacheService.evictAvailableCarsCache();

        Rental saved=rentalRepository.save(rental);
        log.info("Rental Created With RentalId:{}",saved.getRentalId());
        return RentalMapper.response(saved);
    }

    //8.RETURN A CAR
    @Transactional
    public RentalResponse returnACar(Integer rentalId,
                                     Boolean damaged,
                                     BigDecimal damagedFee,
                                     String requesterEmail,
                                     boolean admin){
        log.info("Fetching Rental With Rental Id:{}",rentalId);
        Rental rental=rentalRepository.findById(rentalId)
                .orElseThrow(()->{
                    log.error("Rental With Id:{} Not Found",rentalId);
                    return new RentalNotFoundException(rentalId);
                });

        validateRentalAccess(rental, requesterEmail, admin);

        if (rental.getStatus() == BookingStatus.CANCELLED) {
            throw new RentalAlreadyCancelledException();
        }

        if (rental.getActualReturnTime() != null) {
            throw new RentalAlreadyReturnedException();
        }
        Car car=rental.getCar();
        LocalDateTime returnTime = LocalDateTime.now();

        applyReturnPricing(rental, damaged, damagedFee, returnTime);

        carRepository.save(car);
        carCacheService.evictAvailableCarsCache();
        Rental saved=rentalRepository.save(rental);
        publishReturnEvents(saved, damaged);
        log.info("Rental with id:{} updated on return", saved.getRentalId());
        return RentalMapper.response(saved);
    }

    private void applyReturnPricing(Rental rental, Boolean damaged, BigDecimal damagedFee, LocalDateTime returnTime) {
        PricingService.ReturnPricing pricing = pricingService.calculateReturnPricing(
                rental, damaged, damagedFee, returnTime
        );

        Car car = rental.getCar();

        if (Boolean.TRUE.equals(damaged)) {
            rental.setDamaged(true);
            rental.setStatus(BookingStatus.COMPLETED_WITH_DAMAGED);
            car.setAvailable(false);
        } else {
            rental.setStatus(BookingStatus.COMPLETED);
            car.setAvailable(true);
        }

        rental.setActualReturnTime(returnTime);
        rental.setTaxAmount(pricing.taxAmount());
        rental.setDiscountAmount(pricing.discountAmount());
        rental.setDamagedFee(pricing.damageCost());
        rental.setLateFee(pricing.lateFee());
        rental.setGrandTotal(pricing.grandTotal());
    }

    private void publishReturnEvents(Rental rental, Boolean damaged) {
        if (Boolean.TRUE.equals(damaged)) {
            eventPublisher.publishEvent(new CarDamagedEvent(
                    rental.getRentalId(),
                    rental.getCar().getCarId(),
                    rental.getCustomer().getCustomerId(),
                    rental.getDamagedFee()
            ));
        } else {
            eventPublisher.publishEvent(new BookingCompletedEvent(
                    rental.getRentalId(),
                    rental.getCar().getCarId(),
                    rental.getCustomer().getCustomerId(),
                    rental.getGrandTotal()
            ));
        }

        if (rental.getLateFee() != null && rental.getLateFee().signum() > 0) {
            eventPublisher.publishEvent(new RentalOverdueEvent(
                    rental.getRentalId(),
                    rental.getCar().getCarId(),
                    rental.getCustomer().getCustomerId(),
                    rental.getExpectedReturnTime(),
                    rental.getLateFee()
            ));
        }
    }

    //9.CANCEL A CAR
    @Transactional
    public void cancelCar(Integer rentalId, String requesterEmail, boolean admin){
        log.info("Fetching Rental With Rental Id:{}",rentalId);
        Rental rental=rentalRepository.findById(rentalId)
                .orElseThrow(()->{
                    log.error("Rental With Id:{} Not Found",rentalId);
                    return new RentalNotFoundException(rentalId);
                });

        validateRentalAccess(rental, requesterEmail, admin);

        if(rental.getStatus()==BookingStatus.CANCELLED ||
                rental.getActualReturnTime() != null ||
                rental.getStatus() == BookingStatus.COMPLETED ||
                rental.getStatus() == BookingStatus.COMPLETED_WITH_DAMAGED){
            throw new CannotCancelException();
        }

        Car car=rental.getCar();
        car.setAvailable(true);

        rental.setStatus(BookingStatus.CANCELLED);
        rental.setCanceledTime(LocalDateTime.now());

        carRepository.save(car);
        carCacheService.evictAvailableCarsCache();

        log.info("Car With Id:{} Marked as Available and Saved",car.getCarId());
        rentalRepository.save(rental);
        log.info("Car With Id:{} Cancelled",rentalId);
    }


    @Transactional
    public void markCarAsRepaired(Integer carId) {

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(carId));

        if (car.getAvailable()) {
            throw new InvalidRentalStateException("Car is already available");
        }

        Rental latestRental = rentalRepository.findTopByCarCarIdOrderByStartTimeDesc(carId)
                .orElseThrow(() -> new InvalidRentalStateException("No rental history found for this car"));

        if (latestRental.getStatus() != BookingStatus.COMPLETED_WITH_DAMAGED ||
                latestRental.getActualReturnTime() == null) {
            throw new InvalidRentalStateException("Only damaged returned cars can be marked as repaired");
        }

        car.setAvailable(true);

        carRepository.save(car);
        carCacheService.evictAvailableCarsCache();

        log.info("Car {} marked as repaired and available", carId);
    }

    @Scheduled(fixedRateString = "${app.scheduling.overdue-check-ms:3600000}")
    @Transactional(readOnly = true)
    public void publishOverdueRentalEvents() {
        LocalDateTime now = LocalDateTime.now();
        rentalRepository.findByActualReturnTimeIsNullAndExpectedReturnTimeBefore(
                now, Pageable.unpaged()
        ).forEach(rental -> eventPublisher.publishEvent(new RentalOverdueEvent(
                rental.getRentalId(),
                rental.getCar().getCarId(),
                rental.getCustomer().getCustomerId(),
                rental.getExpectedReturnTime(),
                BigDecimal.ZERO
        )));
    }

    private Customer resolveBookingCustomer(Integer customerId, String requesterEmail, boolean admin) {
        if (admin) {
            if (customerId == null) {
                throw new CustomerIdRequiredException();
            }

            log.info("Fetching Customer With Id:{}",customerId);
            return customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.error("Customer With Id {} Not Found",customerId);
                        return new CustomerNotFoundException(customerId);
                    });
        }

        return customerRepository.findByCustomerEmailIgnoreCase(requesterEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    private void validateRentalAccess(Rental rental, String requesterEmail, boolean admin) {
        if (admin) {
            return;
        }

        if (!rental.getCustomer().getCustomerEmail().equalsIgnoreCase(requesterEmail)) {
            throw new AccessDeniedException("You are not allowed to access this rental");
        }
    }
}
