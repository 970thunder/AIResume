package com.resume.generator.controller;

import com.resume.generator.dto.VisitStatsDTO;
import com.resume.generator.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0];
    }

    @PostMapping("/record")
    public ResponseEntity<Void> recordVisit(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        visitService.recordVisit(ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<VisitStatsDTO> getStats() {
        VisitStatsDTO stats = visitService.getVisitStats();
        return ResponseEntity.ok(stats);
    }
}