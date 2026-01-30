package com.resume.generator.repository;

import com.resume.generator.entity.Template;
import com.resume.generator.entity.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByType(String type);

    List<Template> findByStatus(TemplateStatus status);

    long countByStatus(TemplateStatus status);
}