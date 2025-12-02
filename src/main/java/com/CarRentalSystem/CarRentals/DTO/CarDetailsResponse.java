package com.CarRentalSystem.CarRentals.DTO;

import lombok.Data;

@Data
public class CarDetailsResponse {

    private Integer id;
    private String brand;
    private String model;
    private Integer rentPerDay;
    private Boolean isAvailable;
}
