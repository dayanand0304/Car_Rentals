package com.CarRentalSystem.CarRentals.Repositories;

import com.CarRentalSystem.CarRentals.Entities.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental,Integer> {
    List<Rental> findByCustomerCustomerId(Integer customerId);
}
