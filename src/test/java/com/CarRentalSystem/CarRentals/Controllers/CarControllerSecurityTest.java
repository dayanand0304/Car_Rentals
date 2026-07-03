package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.Config.SecurityBeansConfig;
import com.CarRentalSystem.CarRentals.Config.SecurityConfig;
import com.CarRentalSystem.CarRentals.DTO.Request.CarCreateRequest;
import com.CarRentalSystem.CarRentals.DTO.Response.CarResponse;
import com.CarRentalSystem.CarRentals.Enums.FuelType;
import com.CarRentalSystem.CarRentals.Enums.SeatType;
import com.CarRentalSystem.CarRentals.ExceptionHandler.CustomAccessDeniedHandler;
import com.CarRentalSystem.CarRentals.Security.CustomerUserDetailsService;
import com.CarRentalSystem.CarRentals.Security.JwtFilter;
import com.CarRentalSystem.CarRentals.Security.JwtService;
import com.CarRentalSystem.CarRentals.Services.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CarController.class)
@Import({SecurityConfig.class, SecurityBeansConfig.class, JwtFilter.class, CustomAccessDeniedHandler.class})
class CarControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomerUserDetailsService customerUserDetailsService;

    @Test
    void unauthenticatedRequestToCars_isUnauthorized() throws Exception {
        mockMvc.perform(get("/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCanListCars() throws Exception {
        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotAddCar() throws Exception {
        CarCreateRequest request = buildCarCreateRequest();

        mockMvc.perform(post("/cars")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddCar() throws Exception {
        CarResponse response = new CarResponse(
                1, "Toyota", "Innova", "AP01AB1234",
                FuelType.PETROL, SeatType.FIVE, 1500, true, true
        );
        when(carService.addCar(any())).thenReturn(response);

        mockMvc.perform(post("/cars")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCarCreateRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotDeactivateCar() throws Exception {
        mockMvc.perform(delete("/cars/1/de-activate").with(csrf()))
                .andExpect(status().isForbidden());
    }

    private CarCreateRequest buildCarCreateRequest() {
        CarCreateRequest request = new CarCreateRequest();
        request.setCarBrand("Toyota");
        request.setCarModel("Innova");
        request.setRegistrationNumber("AP01AB1234");
        request.setFuelType(FuelType.PETROL);
        request.setSeats(SeatType.FIVE);
        request.setCarRentPerDay(1500);
        return request;
    }
}
