package com.jobportal.dto;

import com.jobportal.entity.Role;

import java.time.LocalDateTime;

public class AdminDtos {

    public record UserSummary(
            Long id,
            String email,
            Role role,
            boolean enabled,
            LocalDateTime createdAt
    ) {}
}
