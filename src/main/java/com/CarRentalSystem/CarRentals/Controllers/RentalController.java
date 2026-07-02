package com.CarRentalSystem.CarRentals.Controllers;

import com.CarRentalSystem.CarRentals.DTO.Request.RentalCreateRequest;
import com.CarRentalSystem.CarRentals.DTO.Request.RentalReturnRequest;
import com.CarRentalSystem.CarRentals.DTO.Response.PageResponse;
import com.CarRentalSystem.CarRentals.DTO.Response.RentalResponse;
import com.CarRentalSystem.CarRentals.Enums.BookingStatus;
import com.CarRentalSystem.CarRentals.Enums.RentalType;
import com.CarRentalSystem.CarRentals.Services.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Rental APIs", description = "Operations related to car rentals and booking management")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RentalController {

    private final RentalService rentalService;

    // GET ALL RENTALS
    @Operation(
            summary = "Get Rentals",
            description = "Fetch rentals with optional filters like customer ID, car ID, booking status, and rental type"
    )
    @ApiResponse(responseCode = "200", description = "Rentals fetched successfully",
            content = @Content(schema = @Schema(implementation = RentalResponse.class)))
    @GetMapping
    public ResponseEntity<PageResponse<RentalResponse>> getAllRentals(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer carId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) RentalType rentalType,
            @ParameterObject @PageableDefault(size = 5, sort = "rentalId") Pageable pageable
    ) {

        PageResponse<RentalResponse> rentals;

        if (customerId != null && status != null) {
            rentals = rentalService.getRentalByCustomerIdAndStatus(customerId, status, pageable);

        } else if (customerId != null) {
            rentals = rentalService.getRentalsByCustomerId(customerId, pageable);

        } else if (carId != null) {
            rentals = rentalService.getRentalByCarId(carId, pageable);

        } else if (status != null) {
            rentals = rentalService.getRentalByStatus(status, pageable);

        } else if (rentalType != null) {
            rentals = rentalService.getRentalsByRentalType(rentalType, pageable);

        } else {
            rentals = rentalService.getAllRentals(pageable);
        }

        return ResponseEntity.ok(rentals);
    }

    // GET DAMAGED RENTALS
    @Operation(
            summary = "Get Damaged Rentals",
            description = "Fetch all rentals where cars are marked as damaged"
    )
    @ApiResponse(responseCode = "200", description = "Damaged rentals fetched",
            content = @Content(schema = @Schema(implementation = RentalResponse.class)))
    @GetMapping("/damaged")
    public ResponseEntity<PageResponse<RentalResponse>> getAllDamagedRentals(
            @ParameterObject @PageableDefault(size = 10, sort = "rentalId") Pageable pageable) {

        return ResponseEntity.ok(rentalService.getRentalsOfDamaged(pageable));
    }

    // GET OVERDUE RENTALS
    @Operation(
            summary = "Get Overdue Rentals",
            description = "Fetch rentals that are overdue and not returned on time"
    )
    @ApiResponse(responseCode = "200", description = "Overdue rentals fetched",
            content = @Content(schema = @Schema(implementation = RentalResponse.class)))
    @GetMapping("/over-dues")
    public ResponseEntity<PageResponse<RentalResponse>> getOverDues(
            @ParameterObject @PageableDefault(size = 10, sort = "rentalId") Pageable pageable) {

        return ResponseEntity.ok(rentalService.getAllOverdueRentals(pageable));
    }

    // GET RENTAL BY ID
    @Operation(
            summary = "Get Rental by ID",
            description = "Fetch a rental using its unique rental ID"
    )
    @ApiResponse(responseCode = "200", description = "Rental found",
            content = @Content(schema = @Schema(implementation = RentalResponse.class)))
    @ApiResponse(responseCode = "404", description = "Rental not found")
    @GetMapping("/{rentalId}")
    public ResponseEntity<RentalResponse> getRentalById(@PathVariable Integer rentalId) {
        return ResponseEntity.ok(rentalService.getRentalByRentalId(rentalId));
    }

    // RENT A CAR
    @Operation(
            summary = "Rent a Car",
            description = "Create a new rental booking for a car"
    )
    @ApiResponse(responseCode = "201", description = "Car rented successfully",
            content = @Content(schema = @Schema(implementation = RentalResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping("/rent-car")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<RentalResponse> rentACar(
            @Valid @RequestBody RentalCreateRequest request,
            Authentication authentication) {

        RentalResponse rental = rentalService.rentACar(
                request.getCarId(),
                request.getCustomerId(),
                request.getRentalType(),
                request.getDuration(),
                authentication.getName(),
                isAdmin(authentication)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    // RETURN CAR
    @Operation(
            summary = "Return a Car",
            description = "Return a rented car and calculate fees if applicable"
    )
    @ApiResponse(responseCode = "200", description = "Car returned successfully",
            content = @Content(schema = @Schema(implementation = RentalResponse.class)))
    @ApiResponse(responseCode = "404", description = "Rental not found")
    @PutMapping("/{rentalId}/return")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<RentalResponse> returnCar(
            @PathVariable Integer rentalId,
            @Valid @RequestBody RentalReturnRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                rentalService.returnACar(
                        rentalId,
                        request.getDamaged(),
                        request.getDamagedFee(),
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    // CANCEL RENTAL
    @Operation(
            summary = "Cancel Rental",
            description = "Cancel an existing rental booking"
    )
    @ApiResponse(responseCode = "204", description = "Rental cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Rental not found")
    @DeleteMapping("/{rentalId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<String> cancelRental(@PathVariable Integer rentalId,
                                               Authentication authentication) {
        rentalService.cancelCar(rentalId, authentication.getName(), isAdmin(authentication));
        return ResponseEntity.ok("Car with Rental Id: "+rentalId+" Cancelled");
    }

    // MARK CAR AS REPAIRED
    @Operation(
            summary = "Mark Car as Repaired",
            description = "Mark a damaged car as repaired and available"
    )
    @ApiResponse(responseCode = "204", description = "Car marked as available")
    @PutMapping("/cars/{carId}/set-available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> repairCar(@PathVariable Integer carId) {
        rentalService.markCarAsRepaired(carId);
        return ResponseEntity.ok("Car With Id: "+carId+" successfully Repaired and available now");
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
