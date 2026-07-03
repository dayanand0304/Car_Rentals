package com.CarRentalSystem.CarRentals.Security;

import com.CarRentalSystem.CarRentals.CustomExceptions.Customers.CustomerInActiveException;
import com.CarRentalSystem.CarRentals.CustomExceptions.Customers.CustomerNotFoundException;
import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Enums.Role;
import com.CarRentalSystem.CarRentals.Repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerUserDetailsService customerUserDetailsService;

    @Test
    void loadUserByUsername_activeCustomer_returnsRoleAuthority() {
        Customer customer = activeCustomer("admin@example.com", Role.ADMIN);
        when(customerRepository.findByCustomerEmailIgnoreCase("admin@example.com"))
                .thenReturn(Optional.of(customer));

        UserDetails userDetails = customerUserDetailsService.loadUserByUsername("admin@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("admin@example.com");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_inactiveCustomer_isRejected() {
        Customer customer = activeCustomer("inactive@example.com", Role.CUSTOMER);
        customer.setActive(false);
        when(customerRepository.findByCustomerEmailIgnoreCase("inactive@example.com"))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername("inactive@example.com"))
                .isInstanceOf(CustomerInActiveException.class);
    }

    @Test
    void loadUserByUsername_missingCustomer_isRejected() {
        when(customerRepository.findByCustomerEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    private Customer activeCustomer(String email, Role role) {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setCustomerName("Test User");
        customer.setCustomerEmail(email);
        customer.setPassword("encoded-password");
        customer.setRole(role);
        customer.setActive(true);
        return customer;
    }
}
