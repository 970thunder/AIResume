package com.resume.generator.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visit_record")
public class VisitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
}