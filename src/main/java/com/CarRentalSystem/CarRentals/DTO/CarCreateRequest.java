package com.CarRentalSystem.CarRentals.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class CarCreateRequest {

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    @Min(1000)
    private Integer rentPerDay;

}
