package com.resume.generator.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "templates")
@Data
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private BigDecimal price;
    private String description;

    @Column(name = "template_path")
    private String templatePath;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "html_content", columnDefinition = "LONGTEXT")
    private String htmlContent;

    @Column(name = "preview_image")
    private String previewImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}