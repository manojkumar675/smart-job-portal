package com.jobportal.dto;

import com.jobportal.entity.ExperienceLevel;
import com.jobportal.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class JobDtos {

    public record JobRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String location,
            @NotNull JobType jobType,
            @NotNull ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary,
            @NotEmpty List<String> requiredSkills
    ) {}

    public record JobResponse(
            Long id,
            String title,
            String description,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary,
            String companyName,
            Long recruiterId,
            Set<String> requiredSkills,
            boolean active,
            LocalDateTime createdAt,
            Double matchPercentage // populated only for candidate searches; null otherwise
    ) {}
}
