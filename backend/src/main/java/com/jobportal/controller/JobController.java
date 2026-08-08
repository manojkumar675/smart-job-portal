package com.jobportal.controller;

import com.jobportal.dto.JobDtos.JobRequest;
import com.jobportal.dto.JobDtos.JobResponse;
import com.jobportal.entity.ExperienceLevel;
import com.jobportal.entity.JobType;
import com.jobportal.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * Dynamic, filterable, paginated job search.
     * All filters are optional and combinable, e.g.:
     * GET /api/jobs?location=bangalore&skills=java,spring&experienceLevel=MID&jobType=FULL_TIME&keyword=backend&page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(jobService.searchJobs(location, skills, experienceLevel, jobType, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.createJob(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobResponse> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(jobService.toggleActive(id, active));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
