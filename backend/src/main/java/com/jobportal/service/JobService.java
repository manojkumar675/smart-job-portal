package com.jobportal.service;

import com.jobportal.dto.JobDtos.JobRequest;
import com.jobportal.dto.JobDtos.JobResponse;
import com.jobportal.entity.*;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.RecruiterProfileRepository;
import com.jobportal.security.SecurityUtils;
import com.jobportal.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final SkillService skillService;
    private final SkillMatchService skillMatchService;

    @Transactional
    public JobResponse createJob(JobRequest request) {
        RecruiterProfile recruiter = currentRecruiter();
        Set<Skill> skills = skillService.resolveOrCreate(request.requiredSkills());

        Job job = Job.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .jobType(request.jobType())
                .experienceLevel(request.experienceLevel())
                .minSalary(request.minSalary())
                .maxSalary(request.maxSalary())
                .postedBy(recruiter)
                .requiredSkills(skills)
                .active(true)
                .build();

        job = jobRepository.save(job);
        return toResponse(job, null);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        RecruiterProfile recruiter = currentRecruiter();
        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You can only edit your own job listings");
        }

        Set<Skill> skills = skillService.resolveOrCreate(request.requiredSkills());

        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setLocation(request.location());
        job.setJobType(request.jobType());
        job.setExperienceLevel(request.experienceLevel());
        job.setMinSalary(request.minSalary());
        job.setMaxSalary(request.maxSalary());
        job.setRequiredSkills(skills);

        job = jobRepository.save(job);
        return toResponse(job, null);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        RecruiterProfile recruiter = currentRecruiter();
        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You can only delete your own job listings");
        }

        jobRepository.delete(job);
    }

    @Transactional
    public JobResponse toggleActive(Long jobId, boolean active) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        RecruiterProfile recruiter = currentRecruiter();
        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You can only manage your own job listings");
        }
        job.setActive(active);
        jobRepository.save(job);
        return toResponse(job, null);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        Double match = tryCalculateMatchForCurrentCandidate(job);
        return toResponse(job, match);
    }

    /**
     * Dynamic, filterable, paginated job search built with JPA Specifications.
     * Any combination of filters may be null/empty - only supplied filters are applied.
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String location,
                                         List<String> skills,
                                         ExperienceLevel experienceLevel,
                                         JobType jobType,
                                         String keyword,
                                         Pageable pageable) {
        var spec = JobSpecification.withFilters(location, skills, experienceLevel, jobType, keyword, true);
        Page<Job> jobs = jobRepository.findAll(spec, pageable);

        // Only compute match percentage when the caller is an authenticated candidate
        CandidateProfile candidate = tryCurrentCandidate();

        return jobs.map(job -> {
            Double match = candidate != null ? skillMatchService.calculateMatchPercentage(candidate, job) : null;
            return toResponse(job, match);
        });
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByRecruiter(Pageable pageable) {
        RecruiterProfile recruiter = currentRecruiter();
        var spec = (org.springframework.data.jpa.domain.Specification<Job>)
                (root, query, cb) -> cb.equal(root.get("postedBy").get("id"), recruiter.getId());
        return jobRepository.findAll(spec, pageable).map(job -> toResponse(job, null));
    }

    private RecruiterProfile currentRecruiter() {
        String email = SecurityUtils.currentUserEmail();
        return recruiterProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for current user"));
    }

    private CandidateProfile tryCurrentCandidate() {
        String email = SecurityUtils.currentUserEmail();
        if (email == null) {
            return null;
        }
        return candidateProfileRepository.findByUserEmail(email).orElse(null);
    }

    private Double tryCalculateMatchForCurrentCandidate(Job job) {
        CandidateProfile candidate = tryCurrentCandidate();
        return candidate != null ? skillMatchService.calculateMatchPercentage(candidate, job) : null;
    }

    private JobResponse toResponse(Job job, Double matchPercentage) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getJobType(),
                job.getExperienceLevel(),
                job.getMinSalary(),
                job.getMaxSalary(),
                job.getPostedBy().getCompanyName(),
                job.getPostedBy().getId(),
                job.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.toSet()),
                job.isActive(),
                job.getCreatedAt(),
                matchPercentage
        );
    }
}
