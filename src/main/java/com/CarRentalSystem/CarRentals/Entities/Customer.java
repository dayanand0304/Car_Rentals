package com.CarRentalSystem.CarRentals.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Customer_List")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    private String customerName;

    private String customerPhoneNo;

    private String customerEmail;
}
