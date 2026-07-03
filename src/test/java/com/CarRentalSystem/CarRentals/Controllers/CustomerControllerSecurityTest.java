package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.Config.SecurityBeansConfig;
import com.CarRentalSystem.CarRentals.Config.SecurityConfig;
import com.CarRentalSystem.CarRentals.DTO.Response.CustomerResponse;
import com.CarRentalSystem.CarRentals.DTO.Response.PageResponse;
import com.CarRentalSystem.CarRentals.ExceptionHandler.CustomAccessDeniedHandler;
import com.CarRentalSystem.CarRentals.Security.CustomerUserDetailsService;
import com.CarRentalSystem.CarRentals.Security.JwtFilter;
import com.CarRentalSystem.CarRentals.Security.JwtService;
import com.CarRentalSystem.CarRentals.Services.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@Import({SecurityConfig.class, SecurityBeansConfig.class, JwtFilter.class, CustomAccessDeniedHandler.class})
class CustomerControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomerUserDetailsService customerUserDetailsService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotAccessCustomerManagementEndpoints() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessCustomerManagementEndpoints() throws Exception {
        PageResponse<CustomerResponse> page = new PageResponse<>(List.of(), 0, 5, 0, 0, true);
        when(customerService.getAllCustomers(any())).thenReturn(page);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk());
    }
}
