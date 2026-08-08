package com.jobportal.service;

import com.jobportal.entity.Skill;
import com.jobportal.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * Finds existing skills by name (case-insensitive) or creates them if they don't exist yet.
     */
    public Set<Skill> resolveOrCreate(List<String> names) {
        Set<Skill> result = new HashSet<>();
        if (names == null) {
            return result;
        }
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String trimmed = name.trim();
            Skill skill = skillRepository.findByNameIgnoreCase(trimmed)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(trimmed).build()));
            result.add(skill);
        }
        return result;
    }

    public List<Skill> findAll() {
        return skillRepository.findAll();
    }
}
