package com.jobportal.controller;

import com.jobportal.dto.ProfileDtos.CandidateProfileRequest;
import com.jobportal.dto.ProfileDtos.CandidateProfileResponse;
import com.jobportal.service.CandidateProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateProfileService candidateProfileService;

    @GetMapping("/profile")
    public ResponseEntity<CandidateProfileResponse> getMyProfile() {
        return ResponseEntity.ok(candidateProfileService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<CandidateProfileResponse> updateMyProfile(@RequestBody CandidateProfileRequest request) {
        return ResponseEntity.ok(candidateProfileService.updateMyProfile(request));
    }
}
