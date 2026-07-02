package com.CarRentalSystem.CarRentals.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for refreshing an access token")
public class RefreshTokenRequest {

    @Schema(description = "Valid refresh token issued at login", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}
