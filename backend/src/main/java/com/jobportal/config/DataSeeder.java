package com.jobportal.config;

import com.jobportal.entity.*;
import com.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Seeds sample data for local testing / demos. Only runs when app.seed-data=true
 * AND the users table is empty, so it never overwrites real data.
 *
 * Seeded accounts (all passwords: "password123"):
 *   admin@jobportal.com      (ADMIN)
 *   recruiter1@acme.com      (RECRUITER, Acme Corp)
 *   recruiter2@globex.com    (RECRUITER, Globex Inc)
 *   candidate1@example.com   (CANDIDATE, skills: Java, Spring Boot, MySQL)
 *   candidate2@example.com   (CANDIDATE, skills: React, JavaScript, CSS)
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-data:false}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || userRepository.count() > 0) {
            return;
        }

        String rawPassword = "password123";

        // --- Admin ---
        userRepository.save(User.builder()
                .email("admin@jobportal.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        // --- Recruiters ---
        User recruiterUser1 = userRepository.save(User.builder()
                .email("recruiter1@acme.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.RECRUITER)
                .enabled(true)
                .build());
        RecruiterProfile recruiter1 = recruiterProfileRepository.save(RecruiterProfile.builder()
                .user(recruiterUser1)
                .companyName("Acme Corp")
                .companyWebsite("https://acme.example.com")
                .designation("Technical Recruiter")
                .build());

        User recruiterUser2 = userRepository.save(User.builder()
                .email("recruiter2@globex.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.RECRUITER)
                .enabled(true)
                .build());
        RecruiterProfile recruiter2 = recruiterProfileRepository.save(RecruiterProfile.builder()
                .user(recruiterUser2)
                .companyName("Globex Inc")
                .companyWebsite("https://globex.example.com")
                .designation("HR Manager")
                .build());

        // --- Skills ---
        Skill java = skill("Java");
        Skill spring = skill("Spring Boot");
        Skill mysql = skill("MySQL");
        Skill react = skill("React");
        Skill js = skill("JavaScript");
        Skill css = skill("CSS");
        Skill docker = skill("Docker");
        Skill aws = skill("AWS");
        Skill python = skill("Python");

        // --- Candidates ---
        User candidateUser1 = userRepository.save(User.builder()
                .email("candidate1@example.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.CANDIDATE)
                .enabled(true)
                .build());
        candidateProfileRepository.save(CandidateProfile.builder()
                .user(candidateUser1)
                .fullName("Asha Rao")
                .phone("9876543210")
                .experienceLevel(ExperienceLevel.MID)
                .bio("Backend developer with 4 years experience in Java microservices.")
                .skills(setOf(java, spring, mysql, docker))
                .build());

        User candidateUser2 = userRepository.save(User.builder()
                .email("candidate2@example.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.CANDIDATE)
                .enabled(true)
                .build());
        candidateProfileRepository.save(CandidateProfile.builder()
                .user(candidateUser2)
                .fullName("Rahul Mehta")
                .phone("9123456780")
                .experienceLevel(ExperienceLevel.ENTRY)
                .bio("Frontend developer passionate about building clean UIs.")
                .skills(setOf(react, js, css))
                .build());

        // --- Jobs ---
        jobRepository.save(Job.builder()
                .title("Backend Engineer - Java/Spring")
                .description("Build and maintain scalable REST APIs using Java and Spring Boot.")
                .location("Bangalore")
                .jobType(JobType.FULL_TIME)
                .experienceLevel(ExperienceLevel.MID)
                .minSalary(1200000.0)
                .maxSalary(2000000.0)
                .postedBy(recruiter1)
                .requiredSkills(setOf(java, spring, mysql))
                .active(true)
                .build());

        jobRepository.save(Job.builder()
                .title("DevOps Engineer")
                .description("Own our CI/CD pipeline and container infrastructure.")
                .location("Remote")
                .jobType(JobType.REMOTE)
                .experienceLevel(ExperienceLevel.SENIOR)
                .minSalary(1800000.0)
                .maxSalary(2800000.0)
                .postedBy(recruiter1)
                .requiredSkills(setOf(docker, aws, python))
                .active(true)
                .build());

        jobRepository.save(Job.builder()
                .title("Frontend Developer - React")
                .description("Build delightful, responsive user interfaces with React.")
                .location("Pune")
                .jobType(JobType.FULL_TIME)
                .experienceLevel(ExperienceLevel.ENTRY)
                .minSalary(600000.0)
                .maxSalary(1000000.0)
                .postedBy(recruiter2)
                .requiredSkills(setOf(react, js, css))
                .active(true)
                .build());

        jobRepository.save(Job.builder()
                .title("Full Stack Intern")
                .description("6-month internship working across our Java/React stack.")
                .location("Bangalore")
                .jobType(JobType.INTERNSHIP)
                .experienceLevel(ExperienceLevel.ENTRY)
                .minSalary(240000.0)
                .maxSalary(360000.0)
                .postedBy(recruiter2)
                .requiredSkills(setOf(java, react, mysql))
                .active(true)
                .build());
    }

    private Skill skill(String name) {
        return skillRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build()));
    }

    private Set<Skill> setOf(Skill... skills) {
        return new HashSet<>(Set.of(skills));
    }
}
