package com.jobportal.dto;

import com.jobportal.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            @NotNull Role role,
            // Optional profile fields captured at registration time
            String fullName,          // candidate
            String companyName        // recruiter
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            String email,
            Role role,
            Long userId
    ) {}
}
