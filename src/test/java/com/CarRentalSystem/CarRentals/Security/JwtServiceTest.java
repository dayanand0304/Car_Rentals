package com.CarRentalSystem.CarRentals.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "test-jwt-secret-key-for-ci-and-local-tests-1234567890"
        );
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void generateToken_andValidateToken_succeedsForMatchingEmail() {
        String email = "customer@example.com";

        String token = jwtService.generateToken(email);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
        assertThat(jwtService.isTokenValid(token, email)).isTrue();
    }

    @Test
    void isTokenValid_rejectsMismatchedEmail() {
        String token = jwtService.generateToken("owner@example.com");

        assertThat(jwtService.isTokenValid(token, "other@example.com")).isFalse();
    }
}
