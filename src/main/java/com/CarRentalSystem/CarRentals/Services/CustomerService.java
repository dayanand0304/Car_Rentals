package com.CarRentalSystem.CarRentals.Services;

import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;


    //GET ALL CUSTOMERS
    public List<Customer> getAllCustomers(){
        log.info("Fetching All Customers");
        return customerRepository.findAll();
    }

    //GET CUSTOMER BY CUSTOMER ID
    public Optional<Customer> getCustomerById(Integer customerId){
        log.info("Fetching All Customers By Id:{}",customerId);
        return customerRepository.findById(customerId);
    }

    //GET CUSTOMER BY CUSTOMER NAME
    public List<Customer> getCustomerByName(String customerName){
        log.info("Fetching All Customers By Customer Name:{}",customerName);
        return customerRepository.findByCustomerName(customerName);
    }

    //ADD CUSTOMER
    public Customer addCustomer(Customer customer){
        log.info("Adding Customers With Name:{} and Email:{}",customer.getCustomerName(),customer.getCustomerEmail());
        Customer saved=customerRepository.save(customer);
        log.info("Added Customer With Id:{}",saved.getCustomerId());
        return saved;
    }

    //DELETE CUSTOMER BY CUSTOMER ID
    public String deleteCustomer(Integer customerId){
        log.info("Attempting to delete customer with id: {}",customerId);
        if(customerRepository.existsById(customerId)){
            customerRepository.deleteById(customerId);
            log.info("Customer With Id:{} is Deleted",customerId);
            return "Customer Deleted";
        }
        log.warn("Customer With Id: {} Not Found",customerId);
        return "Customer Not Found";
    }

    //UPDATE CUSTOMER DETAILS BY CUSTOMER ID
    public Customer updateCustomer(Integer customerId,Customer updated){
        log.info("Updating Customer with id: {}", customerId);
        return customerRepository.findById(customerId)
                .map(existing->{
                    if(updated.getCustomerName()!=null){
                        existing.setCustomerName(updated.getCustomerName());
                    }
                    if(updated.getCustomerPhoneNo()!=null){
                        existing.setCustomerPhoneNo(updated.getCustomerPhoneNo());
                    }
                    if(updated.getCustomerEmail()!=null){
                        existing.setCustomerEmail(updated.getCustomerEmail());
                    }
                    Customer saved=customerRepository.save(existing);
                    log.info("Customer with id:{} updated successfully", saved.getCustomerId());
                    return saved;
                })
                .orElse(null);
    }
}
