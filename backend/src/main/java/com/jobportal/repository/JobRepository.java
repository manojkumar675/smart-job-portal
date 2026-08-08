package com.jobportal.repository;

import com.jobportal.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    long countByPostedByIdAndActiveTrue(Long recruiterId);
    java.util.List<Job> findByPostedById(Long recruiterId);
}
