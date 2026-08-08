package com.jobportal.service;

import com.jobportal.entity.CandidateProfile;
import com.jobportal.entity.Job;
import com.jobportal.entity.Skill;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule-based skill matching algorithm.
 *
 * Match percentage = (number of job-required skills the candidate has) / (total job-required skills) * 100.
 *
 * This is intentionally simple and deterministic (no ML) so results are explainable:
 * a candidate with 3 of 4 required skills gets a 75% match, regardless of anything else.
 */
@Service
public class SkillMatchService {

    public double calculateMatchPercentage(CandidateProfile candidate, Job job) {
        Set<String> requiredSkills = normalize(job.getRequiredSkills());
        if (requiredSkills.isEmpty()) {
            return 0.0;
        }

        Set<String> candidateSkills = normalize(candidate.getSkills());

        long matchedCount = requiredSkills.stream()
                .filter(candidateSkills::contains)
                .count();

        double percentage = (matchedCount / (double) requiredSkills.size()) * 100.0;
        return Math.round(percentage * 100.0) / 100.0; // round to 2 decimals
    }

    private Set<String> normalize(Set<Skill> skills) {
        return skills.stream()
                .map(s -> s.getName().trim().toLowerCase())
                .collect(Collectors.toSet());
    }
}
