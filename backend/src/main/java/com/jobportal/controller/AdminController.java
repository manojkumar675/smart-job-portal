package com.jobportal.controller;

import com.jobportal.dto.AdminDtos.UserSummary;
import com.jobportal.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserSummary>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.listUsers(pageable));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserSummary> setUserEnabled(@PathVariable Long userId, @RequestParam boolean enabled) {
        return ResponseEntity.ok(adminService.setUserEnabled(userId, enabled));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        adminService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/jobs/{jobId}/status")
    public ResponseEntity<Void> setJobActive(@PathVariable Long jobId, @RequestParam boolean active) {
        adminService.setJobActive(jobId, active);
        return ResponseEntity.noContent().build();
    }
}
