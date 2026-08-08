package com.jobportal.service;

import com.jobportal.dto.ProfileDtos.CandidateProfileRequest;
import com.jobportal.dto.ProfileDtos.CandidateProfileResponse;
import com.jobportal.entity.CandidateProfile;
import com.jobportal.entity.Skill;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final SkillService skillService;

    @Transactional(readOnly = true)
    public CandidateProfileResponse getMyProfile() {
        return toResponse(currentProfile());
    }

    @Transactional
    public CandidateProfileResponse updateMyProfile(CandidateProfileRequest request) {
        CandidateProfile profile = currentProfile();

        if (request.fullName() != null) profile.setFullName(request.fullName());
        if (request.phone() != null) profile.setPhone(request.phone());
        if (request.experienceLevel() != null) profile.setExperienceLevel(request.experienceLevel());
        if (request.resumeUrl() != null) profile.setResumeUrl(request.resumeUrl());
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.skills() != null) {
            Set<Skill> skills = skillService.resolveOrCreate(request.skills());
            profile.setSkills(skills);
        }

        profile = candidateProfileRepository.save(profile);
        return toResponse(profile);
    }

    private CandidateProfile currentProfile() {
        String email = SecurityUtils.currentUserEmail();
        return candidateProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for current user"));
    }

    private CandidateProfileResponse toResponse(CandidateProfile profile) {
        return new CandidateProfileResponse(
                profile.getId(),
                profile.getUser().getEmail(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getExperienceLevel(),
                profile.getResumeUrl(),
                profile.getBio(),
                profile.getSkills().stream().map(Skill::getName).collect(Collectors.toSet())
        );
    }
}
