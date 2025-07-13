package com.resume.generator.repository;

import com.resume.generator.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM VisitRecord v")
    long countDistinctIpAddresses();

    Optional<VisitRecord> findTopByIpAddressOrderByVisitTimeDesc(String ipAddress);
}