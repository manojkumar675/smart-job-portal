package com.jobportal.dto;

import com.jobportal.entity.ExperienceLevel;

import java.util.List;
import java.util.Set;

public class ProfileDtos {

    public record CandidateProfileRequest(
            String fullName,
            String phone,
            ExperienceLevel experienceLevel,
            String resumeUrl,
            String bio,
            List<String> skills
    ) {}

    public record CandidateProfileResponse(
            Long id,
            String email,
            String fullName,
            String phone,
            ExperienceLevel experienceLevel,
            String resumeUrl,
            String bio,
            Set<String> skills
    ) {}

    public record RecruiterProfileRequest(
            String companyName,
            String companyWebsite,
            String designation
    ) {}

    public record RecruiterProfileResponse(
            Long id,
            String email,
            String companyName,
            String companyWebsite,
            String designation
    ) {}
}
