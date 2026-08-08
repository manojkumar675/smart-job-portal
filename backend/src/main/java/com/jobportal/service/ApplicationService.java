package com.jobportal.service;

import com.jobportal.dto.ApplicationDtos.ApplicationResponse;
import com.jobportal.entity.*;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.RecruiterProfileRepository;
import com.jobportal.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final SkillMatchService skillMatchService;

    @Transactional
    public ApplicationResponse apply(Long jobId) {
        CandidateProfile candidate = currentCandidate();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.isActive()) {
            throw new BadRequestException("This job listing is no longer active");
        }

        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new BadRequestException("You have already applied to this job");
        }

        double matchPercentage = skillMatchService.calculateMatchPercentage(candidate, job);

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .matchPercentage(matchPercentage)
                .build();

        application = applicationRepository.save(application);
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications() {
        CandidateProfile candidate = currentCandidate();
        return applicationRepository.findByCandidateId(candidate.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> applicantsForJob(Long jobId, Pageable pageable) {
        RecruiterProfile recruiter = currentRecruiter();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You can only view applicants for your own job listings");
        }

        return applicationRepository.findByJobId(jobId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> allApplicantsForRecruiter(Pageable pageable) {
        RecruiterProfile recruiter = currentRecruiter();
        return applicationRepository.findByJobPostedById(recruiter.getId(), pageable).map(this::toResponse);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status) {
        RecruiterProfile recruiter = currentRecruiter();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getJob().getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You can only manage applications for your own job listings");
        }

        application.setStatus(status);
        application = applicationRepository.save(application);
        return toResponse(application);
    }

    private CandidateProfile currentCandidate() {
        String email = SecurityUtils.currentUserEmail();
        return candidateProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for current user"));
    }

    private RecruiterProfile currentRecruiter() {
        String email = SecurityUtils.currentUserEmail();
        return recruiterProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for current user"));
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getCandidate().getId(),
                application.getCandidate().getFullName(),
                application.getCandidate().getUser().getEmail(),
                application.getStatus(),
                application.getMatchPercentage(),
                application.getAppliedAt()
        );
    }
}
