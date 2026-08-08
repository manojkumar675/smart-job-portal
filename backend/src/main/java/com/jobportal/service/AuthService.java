package com.jobportal.service;

import com.jobportal.dto.AuthDtos.AuthResponse;
import com.jobportal.dto.AuthDtos.LoginRequest;
import com.jobportal.dto.AuthDtos.RegisterRequest;
import com.jobportal.entity.*;
import com.jobportal.exception.BadRequestException;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.repository.RecruiterProfileRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final com.jobportal.security.CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        if (request.role() == Role.CANDIDATE) {
            CandidateProfile profile = CandidateProfile.builder()
                    .user(user)
                    .fullName(request.fullName() != null ? request.fullName() : request.email())
                    .build();
            candidateProfileRepository.save(profile);
        } else if (request.role() == Role.RECRUITER) {
            RecruiterProfile profile = RecruiterProfile.builder()
                    .user(user)
                    .companyName(request.companyName() != null ? request.companyName() : "Unnamed Company")
                    .build();
            recruiterProfileRepository.save(profile);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole(), user.getId());
    }
}
