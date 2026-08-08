package com.jobportal.specification;

import com.jobportal.entity.ExperienceLevel;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobType;
import com.jobportal.entity.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Builds a dynamic JPA Specification for Job search based on whichever
 * filters the caller actually supplied. Every predicate is optional -
 * null/blank values are simply skipped, so search combinations are not
 * hardcoded as separate repository methods.
 */
public class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> withFilters(String location,
                                                   List<String> skills,
                                                   ExperienceLevel experienceLevel,
                                                   JobType jobType,
                                                   String keyword,
                                                   Boolean activeOnly) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (Boolean.TRUE.equals(activeOnly)) {
                predicates = cb.and(predicates, cb.isTrue(root.get("active")));
            }

            if (StringUtils.hasText(location)) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (experienceLevel != null) {
                predicates = cb.and(predicates, cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            if (jobType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("jobType"), jobType));
            }

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (skills != null && !skills.isEmpty()) {
                // distinct results required since the skills join can multiply rows
                if (query != null) {
                    query.distinct(true);
                }
                Join<Job, Skill> skillJoin = root.join("requiredSkills", JoinType.LEFT);
                List<String> lower = skills.stream().map(String::toLowerCase).toList();
                predicates = cb.and(predicates, cb.lower(skillJoin.get("name")).in(lower));
            }

            return predicates;
        };
    }
}
