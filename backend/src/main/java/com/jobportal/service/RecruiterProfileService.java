package com.jobportal.service;

import com.jobportal.dto.ProfileDtos.RecruiterProfileRequest;
import com.jobportal.dto.ProfileDtos.RecruiterProfileResponse;
import com.jobportal.entity.RecruiterProfile;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.RecruiterProfileRepository;
import com.jobportal.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;

    @Transactional(readOnly = true)
    public RecruiterProfileResponse getMyProfile() {
        return toResponse(currentProfile());
    }

    @Transactional
    public RecruiterProfileResponse updateMyProfile(RecruiterProfileRequest request) {
        RecruiterProfile profile = currentProfile();
        if (request.companyName() != null) profile.setCompanyName(request.companyName());
        if (request.companyWebsite() != null) profile.setCompanyWebsite(request.companyWebsite());
        if (request.designation() != null) profile.setDesignation(request.designation());
        profile = recruiterProfileRepository.save(profile);
        return toResponse(profile);
    }

    private RecruiterProfile currentProfile() {
        String email = SecurityUtils.currentUserEmail();
        return recruiterProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for current user"));
    }

    private RecruiterProfileResponse toResponse(RecruiterProfile profile) {
        return new RecruiterProfileResponse(
                profile.getId(),
                profile.getUser().getEmail(),
                profile.getCompanyName(),
                profile.getCompanyWebsite(),
                profile.getDesignation()
        );
    }
}
