package com.stayfinder.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 80) String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 100) String password
    ) {}

    public record AuthResponse(
            String token,
            Long userId,
            String name,
            String email,
            String role
    ) {}
}
