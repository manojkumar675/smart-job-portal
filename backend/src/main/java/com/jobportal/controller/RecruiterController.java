package com.jobportal.controller;

import com.jobportal.dto.JobDtos.JobResponse;
import com.jobportal.dto.ProfileDtos.RecruiterProfileRequest;
import com.jobportal.dto.ProfileDtos.RecruiterProfileResponse;
import com.jobportal.service.JobService;
import com.jobportal.service.RecruiterProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterProfileService recruiterProfileService;
    private final JobService jobService;

    @GetMapping("/profile")
    public ResponseEntity<RecruiterProfileResponse> getMyProfile() {
        return ResponseEntity.ok(recruiterProfileService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<RecruiterProfileResponse> updateMyProfile(@RequestBody RecruiterProfileRequest request) {
        return ResponseEntity.ok(recruiterProfileService.updateMyProfile(request));
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobResponse>> myJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobService.getJobsByRecruiter(pageable));
    }
}
