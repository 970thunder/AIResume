package com.resume.generator.service;

import com.resume.generator.dto.VisitStatsDTO;
import com.resume.generator.entity.VisitRecord;
import com.resume.generator.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRecordRepository visitRecordRepository;

    @Transactional
    public void recordVisit(String ipAddress, String userAgent) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

        Optional<VisitRecord> lastVisit = visitRecordRepository.findTopByIpAddressOrderByVisitTimeDesc(ipAddress);

        if (lastVisit.isEmpty() || lastVisit.get().getVisitTime().isBefore(oneMinuteAgo)) {
            VisitRecord newVisit = new VisitRecord();
            newVisit.setIpAddress(ipAddress);
            newVisit.setUserAgent(userAgent);
            newVisit.setVisitTime(LocalDateTime.now());
            visitRecordRepository.save(newVisit);
        }
    }

    @Transactional(readOnly = true)
    public VisitStatsDTO getVisitStats() {
        long sitePv = visitRecordRepository.count();
        long siteUv = visitRecordRepository.countDistinctIpAddresses();
        return new VisitStatsDTO(sitePv, siteUv);
    }
}