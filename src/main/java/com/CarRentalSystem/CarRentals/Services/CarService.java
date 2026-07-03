package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.CustomExceptions.Cars.*;
import com.CarRentalSystem.CarRentals.DTO.CarMapper;
import com.CarRentalSystem.CarRentals.DTO.PageMapper;
import com.CarRentalSystem.CarRentals.DTO.Request.CarCreateRequest;
import com.CarRentalSystem.CarRentals.DTO.Request.CarUpdateRequest;
import com.CarRentalSystem.CarRentals.DTO.Response.CarResponse;
import com.CarRentalSystem.CarRentals.DTO.Response.PageResponse;
import com.CarRentalSystem.CarRentals.Entities.Car;
import com.CarRentalSystem.CarRentals.Enums.FuelType;
import com.CarRentalSystem.CarRentals.Enums.SeatType;
import com.CarRentalSystem.CarRentals.Repositories.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class CarService {


    private final CarRepository carRepository;
    private final CarCacheService carCacheService;

    //1.GET ALL CAR DETAILS
    public PageResponse<CarResponse> getAllCars(Pageable pageable){
        log.info("Fetching All Cars");
        Page<Car> page=carRepository.findAll(pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //2.GET CAR DETAILS BY ID
    public CarResponse getCarById(Integer carId) {
        log.info("Fetching Car By carId:{}",carId);
        Car car=carRepository.findById(carId)
                .orElseThrow(()-> new CarNotFoundException(carId));

        return CarMapper.response(car);
    }

    public CarResponse getActiveCarById(Integer carId) {
        log.info("Fetching Active Car By carId:{}",carId);
        Car car=carRepository.findByCarIdAndActiveTrue(carId)
                .orElseThrow(()->new CarNotActiveException(carId));

        return CarMapper.response(car);
    }

    //3.GET AVAILABLE CARS
    @Cacheable(
            value = CarCacheService.AVAILABLE_CARS_CACHE,
            key = "'available-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort"
    )
    public PageResponse<CarResponse> getAvailableCars(Pageable pageable){
        log.info("Fetching Available Cars");
        Page<Car> page=carRepository.findByAvailableTrueAndActiveTrue(pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    @Cacheable(
            value = CarCacheService.AVAILABLE_CARS_CACHE,
            key = "'availability-' + #available + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort",
            condition = "#available == true"
    )
    public PageResponse<CarResponse> getCarsByAvailability(Boolean available, Pageable pageable){
        log.info("Fetching Cars By Availability:{}", available);
        Page<Car> page = Boolean.TRUE.equals(available)
                ? carRepository.findByAvailableTrueAndActiveTrue(pageable)
                : carRepository.findByAvailable(available, pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //4.GET CAR DETAILS BY CAR BRAND
    public PageResponse<CarResponse> getCarsByBrand(String carBrand,Pageable pageable){
        log.info("Fetching All Cars By BrandName:{}",carBrand);
        Page<Car> page=carRepository.findByCarBrandContainingIgnoreCase(carBrand,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //5.GET AVAILABLE CAR BRAND DETAILS
    public PageResponse<CarResponse> getCarsByBrandAndAvailableTrue(String carBrand,Pageable pageable){
        log.info("Fetching All Available Cars By BrandName:{}",carBrand );
        Page<Car> page=carRepository.findByCarBrandContainingIgnoreCaseAndAvailableTrue(carBrand,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    public PageResponse<CarResponse> getCarsByBrandAndAvailability(String carBrand, Boolean available, Pageable pageable){
        log.info("Fetching Cars By BrandName:{} and Availability:{}", carBrand, available);
        Page<Car> page = carRepository.findByCarBrandContainingIgnoreCaseAndAvailable(carBrand, available, pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //6.GET CAR DETAILS BY CAR BRAND AND MODEL
    public PageResponse<CarResponse> getCarsByBrandAndModel(String carBrand, String carModel,Pageable pageable){
        log.info("Fetching All Cars By BrandName:{} and CarModel:{}",carBrand,carModel);

        Page<Car> page=carRepository.findByCarBrandContainingIgnoreCaseAndCarModelContainingIgnoreCase(
                carBrand,
                carModel,
                pageable
        );

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    public PageResponse<CarResponse> getCarsByBrandModelAndAvailability(String carBrand,
                                                                        String carModel,
                                                                        Boolean available,
                                                                        Pageable pageable){
        log.info("Fetching Cars By BrandName:{}, CarModel:{} and Availability:{}", carBrand, carModel, available);
        Page<Car> page = carRepository.findByCarBrandContainingIgnoreCaseAndCarModelContainingIgnoreCaseAndAvailable(
                carBrand,
                carModel,
                available,
                pageable
        );

        return PageMapper.toPageResponse(page,CarMapper::response);
    }


    //7.CHECK IF CAR IS AVAILABLE OR NOT
    public boolean isAvailable(Integer carId){
        log.debug("checking Availability of CarId:{}",carId);
        Car car=carRepository.findById(carId)
                .orElseThrow(()->new CarNotFoundException(carId));
        return car.getAvailable();
    }

    //8.FIND CARS BY FUEL TYPE
    public PageResponse<CarResponse> getCarsByFuelType(FuelType fuelType,Pageable pageable){
        Page<Car> page=carRepository.findByFuelType(fuelType,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //9.FIND BY SEAT TYPE
    public PageResponse<CarResponse> getCarsBySeatType(SeatType seats,Pageable pageable){
        Page<Car> page=carRepository.findBySeats(seats,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //10.FIND CAR BY PRICE RANGE
    public PageResponse<CarResponse> getCarsByPriceRange(Integer min,Integer max,Pageable pageable){
        Page<Car> page=carRepository.findByCarRentPerDayBetween(min, max,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //11.FIND CAR BY REGISTRATION NUMBER
    public CarResponse getCarByRegisterNum(String number){
        Car car=carRepository.findByRegistrationNumberIgnoreCase(number)
                .orElseThrow(()->new CarNotFoundException("Car With Registration Number: "+number+" not found"));

        return CarMapper.response(car);
    }

    //12.GET CARS BY LAST FOUR DIGITS OF REGISTER NUMBER
    public PageResponse<CarResponse> getCarsByLastRegisterNum(String number,Pageable pageable){
        if(number.length()!=4){
            throw new RegistrationNumberFormatException("Enter exactly 4 digits");
        }
        if (!number.matches("\\d+")) {
            throw new RegistrationNumberFormatException("Only numeric digits allowed");
        }
        Page<Car> page=carRepository.findByRegistrationNumberEndingWith(number,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }

    //13.ADD CAR
    public CarResponse addCar(CarCreateRequest request){

        if(carRepository.existsByRegistrationNumber(request.getRegistrationNumber())){
            throw new CarAlreadyExistsException(request.getRegistrationNumber());
        }

        Car car=CarMapper.create(request);


        log.info("Adding New Car Of Brand:{} and Model:{}", car.getCarBrand(),car.getCarModel());

        Car saved=carRepository.save(car);
        carCacheService.evictAvailableCarsCache();
        log.info("Added New Car with id:{}",saved.getCarId());
        return CarMapper.response(saved);
    }

    //14.DELETE CAR
    @Transactional
    public void deActivateCar(Integer carId){
        log.info("Attempting to delete car with id: {}", carId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(carId));

        if (!car.getAvailable()) {
            throw new CarNotAvailableException(carId);
        }
        car.setActive(false);
        car.setAvailable(false);
        car.setDeletedAt(LocalDateTime.now());
        carRepository.save(car);
        carCacheService.evictAvailableCarsCache();
        log.info("Car with id:{} deleted successfully", carId);
    }

    //17.REACTIVATE CAR
    public void reActivateCar(Integer carId){
        Car car=carRepository.findById(carId)
                .orElseThrow(()->new CarNotFoundException(carId));

        if(Boolean.TRUE.equals(car.getActive())){
            return;
        }

        car.setActive(true);
        car.setAvailable(true);
        car.setDeletedAt(null);
        carRepository.save(car);
        carCacheService.evictAvailableCarsCache();
    }

    //15.UPDATE CAR DETAILS
    @Transactional
    public CarResponse updateCarDetails(Integer carId, CarUpdateRequest updatedDetails){
        log.info("Updating car with id: {}", carId);
        return carRepository.findById(carId)
                .map(existingDetails->{
                    if(!existingDetails.getActive()){
                        throw new CarNotFoundException(carId);
                    }
                    if(updatedDetails.getCarBrand()!=null){
                        existingDetails.setCarBrand(updatedDetails.getCarBrand());
                    }
                    if(updatedDetails.getCarModel()!=null){
                        existingDetails.setCarModel(updatedDetails.getCarModel());
                    }
                    if(updatedDetails.getRegistrationNumber()!=null){

                        if(!updatedDetails.getRegistrationNumber().matches("^[A-Z0-9-]+$")){
                            throw new RegistrationNumberFormatException("Registration number format is Wrong");
                        }

                        if(!updatedDetails.getRegistrationNumber().equals(existingDetails.getRegistrationNumber()) &&
                                carRepository.existsByRegistrationNumber(updatedDetails.getRegistrationNumber())){
                            throw new CarAlreadyExistsException(updatedDetails.getRegistrationNumber());
                        }
                        existingDetails.setRegistrationNumber(updatedDetails.getRegistrationNumber());
                    }
                    if(updatedDetails.getFuelType()!=null){
                        existingDetails.setFuelType(updatedDetails.getFuelType());
                    }
                    if(updatedDetails.getSeats()!=null){
                        existingDetails.setSeats(updatedDetails.getSeats());
                    }
                    if(updatedDetails.getCarRentPerDay()!=null){
                        existingDetails.setCarRentPerDay(updatedDetails.getCarRentPerDay());
                    }
                    if (updatedDetails.getAvailable() != null) {
                        existingDetails.setAvailable(updatedDetails.getAvailable());
                    }

                    Car saved=carRepository.save(existingDetails);
                    carCacheService.evictAvailableCarsCache();
                    log.info("Car with id:{} updated successfully", saved.getCarId());
                    return CarMapper.response(saved);
                })
                .orElseThrow(()->new CarNotFoundException(carId));
    }

    //16.FIND CAR BY ACTIVE
    public PageResponse<CarResponse> getCarsByActive(Boolean active,Pageable pageable){
        Page<Car> page=carRepository.findByActive(active,pageable);

        return PageMapper.toPageResponse(page,CarMapper::response);
    }


}
