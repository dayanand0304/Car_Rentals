package com.CarRentalSystem.CarRentals.DTO;


import lombok.Data;

@Data
public class CarUpdateRequest {

    private String brand;
    private String model;
    private Integer rentPerDay;
    private Boolean isAvailable;
}
