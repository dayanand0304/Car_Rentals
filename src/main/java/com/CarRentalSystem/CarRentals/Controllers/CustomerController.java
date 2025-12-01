package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {


    private final CustomerService customerService;

    //GET ALL CUSTOMERS
    @GetMapping("/get-all")
    public ResponseEntity<List<Customer>> getAllCustomers(){
        List<Customer> customers=customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    //GET CUSTOMER BY CUSTOMER ID
    @GetMapping("/get/id/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer customerId){
        Optional<Customer> customer=customerService.getCustomerById(customerId);
        return customer.map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    //GET CUSTOMER BY CUSTOMER NAME
    @GetMapping("/get/name/{customerName}")
    public ResponseEntity<List<Customer>> getByCustomerName(@PathVariable String customerName){
        List<Customer> customers=customerService.getCustomerByName(customerName);
        return ResponseEntity.ok(customers);
    }

    //ADD CUSTOMER
    @PostMapping("/add-customer")
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer){
        Customer newCustomer=customerService.addCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    //DELETE CUSTOMER BY CUSTOMER ID
    @DeleteMapping("/delete/{customerId}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable Integer customerId){
        String message=customerService.deleteCustomer(customerId);
        return ResponseEntity.ok(message);
    }

    //UPDATE CUSTOMER DETAILS BY CUSTOMER ID
    @PutMapping("/update/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Integer customerId,
                                                   @RequestBody Customer updated){
        Customer customer=customerService.updateCustomer(customerId,updated);
        if(customer!=null){
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
