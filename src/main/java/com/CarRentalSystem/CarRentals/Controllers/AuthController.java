package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.DTO.Request.LoginRequest;
import com.CarRentalSystem.CarRentals.DTO.Request.RefreshTokenRequest;
import com.CarRentalSystem.CarRentals.DTO.Request.RegisterRequest;
import com.CarRentalSystem.CarRentals.DTO.Response.AuthResponse;
import com.CarRentalSystem.CarRentals.DTO.Response.CustomerResponse;
import com.CarRentalSystem.CarRentals.Entities.Customer;
import com.CarRentalSystem.CarRentals.Entities.RefreshToken;
import com.CarRentalSystem.CarRentals.Security.JwtService;
import com.CarRentalSystem.CarRentals.Services.CustomerService;
import com.CarRentalSystem.CarRentals.Services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.CarRentalSystem.CarRentals.ExceptionHandler.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication APIs", description = "APIs for user login and registration")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // LOGIN
    @Operation(summary = "User Login",
            description = "Authenticate user and return access & refresh tokens")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))

    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCustomerEmail(),
                        request.getPassword()
                )
        );

        String email = auth.getName();
        Customer customer=customerService.getCustomerEntityByEmail(email);

        String accessToken = jwtService.generateToken(customer.getCustomerEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer);

        return ResponseEntity.ok(
                new AuthResponse(accessToken, refreshToken.getToken())
        );
    }

    // REFRESH ACCESS TOKEN
    @Operation(summary = "Refresh Access Token",
            description = "Issue a new access token using a valid refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "403", description = "Refresh token expired",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Refresh token not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshAccessToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.VerifyTokenExpiry(refreshToken);

        String accessToken = jwtService.generateToken(refreshToken.getCustomer().getCustomerEmail());

        return ResponseEntity.ok(
                new AuthResponse(accessToken, refreshToken.getToken())
        );
    }

    // REGISTER
    @Operation(summary = "Register User",
            description = "Create a new customer account")
    @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class)))

    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

    @ApiResponse(responseCode = "409", description = "User already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> addCustomer(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.addCustomer(request));
    }


}