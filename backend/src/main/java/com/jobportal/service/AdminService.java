package com.jobportal.service;

import com.jobportal.dto.AdminDtos.UserSummary;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final com.jobportal.repository.CandidateProfileRepository candidateProfileRepository;
    private final com.jobportal.repository.RecruiterProfileRepository recruiterProfileRepository;
    private final com.jobportal.repository.ApplicationRepository applicationRepository;

    public Page<UserSummary> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toSummary);
    }

    @Transactional
    public UserSummary setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setEnabled(enabled);
        user = userRepository.save(user);
        return toSummary(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == com.jobportal.entity.Role.CANDIDATE) {
            candidateProfileRepository.findByUserId(userId).ifPresent(profile -> {
                applicationRepository.deleteByCandidateId(profile.getId());
                candidateProfileRepository.delete(profile);
            });
        } else if (user.getRole() == com.jobportal.entity.Role.RECRUITER) {
            recruiterProfileRepository.findByUserId(userId).ifPresent(profile -> {
                java.util.List<Job> jobs = jobRepository.findByPostedById(profile.getId());
                jobs.forEach(job -> applicationRepository.deleteByJobId(job.getId()));
                jobRepository.deleteAll(jobs);
                recruiterProfileRepository.delete(profile);
            });
        }

        userRepository.delete(user);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        jobRepository.delete(job);
    }

    @Transactional
    public void setJobActive(Long jobId, boolean active) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        job.setActive(active);
        jobRepository.save(job);
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
