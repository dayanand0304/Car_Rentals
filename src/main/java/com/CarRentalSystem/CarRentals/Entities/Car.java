package com.CarRentalSystem.CarRentals.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Car_List")
@Data
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carId;

    private String carBrand;

    private String carModel;

    private Integer carRentPerDay;

    private Boolean available=true;
}
