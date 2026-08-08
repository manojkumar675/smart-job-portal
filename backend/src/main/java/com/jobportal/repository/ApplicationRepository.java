package com.jobportal.repository;

import com.jobportal.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateId(Long candidateId);
    Page<Application> findByJobId(Long jobId, Pageable pageable);
    Page<Application> findByJobPostedById(Long recruiterId, Pageable pageable);
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    void deleteByCandidateId(Long candidateId);
    void deleteByJobId(Long jobId);
}
