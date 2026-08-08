package com.jobportal.dto;

import com.jobportal.entity.ApplicationStatus;

import java.time.LocalDateTime;

public class ApplicationDtos {

    public record ApplicationResponse(
            Long id,
            Long jobId,
            String jobTitle,
            Long candidateId,
            String candidateName,
            String candidateEmail,
            ApplicationStatus status,
            Double matchPercentage,
            LocalDateTime appliedAt
    ) {}

    public record StatusUpdateRequest(
            ApplicationStatus status
    ) {}
}
