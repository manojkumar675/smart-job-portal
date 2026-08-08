package com.jobportal.controller;

import com.jobportal.dto.ApplicationDtos.ApplicationResponse;
import com.jobportal.dto.ApplicationDtos.StatusUpdateRequest;
import com.jobportal.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.apply(jobId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationResponse>> myApplications() {
        return ResponseEntity.ok(applicationService.myApplications());
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> applicantsForJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(applicationService.applicantsForJob(jobId, pageable));
    }

    @GetMapping("/recruiter/all")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> allApplicants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(applicationService.allApplicantsForRecruiter(pageable));
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody StatusUpdateRequest request
    ) {
        return ResponseEntity.ok(applicationService.updateStatus(applicationId, request.status()));
    }
}
