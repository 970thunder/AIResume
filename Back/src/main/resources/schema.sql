-- SQL script for creating the database schema and inserting initial data.
-- You should first create the database, e.g., `CREATE DATABASE resume_generator;`
-- Then, connect to the database (`USE resume_generator;`) and run this script.

-- Table for storing user session information and uploaded content
CREATE TABLE IF NOT EXISTS `user_profiles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(255) NULL,
    `original_files` TEXT NULL,
    `extracted_content` TEXT NULL,
    `ai_analysis` TEXT NULL,
    `created_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for storing available resume templates
CREATE TABLE IF NOT EXISTS `templates` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NULL,
    `type` VARCHAR(255) NULL,
    `price` DECIMAL(19, 2) NULL,
    `description` VARCHAR(255) NULL,
    `template_path` VARCHAR(255) NULL,
    `preview_image` VARCHAR(255) NULL,
    `created_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for storing the final generated resumes
CREATE TABLE IF NOT EXISTS `resumes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_profile_id` BIGINT NULL,
    `template_id` BIGINT NULL,
    `resume_content` TEXT NULL,
    `file_path` VARCHAR(255) NULL,
    `created_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;


-- Insert initial data for templates
-- Note: template_path and preview_image are placeholders as per the design.
INSERT INTO templates (id, name, type, price, description, template_path, created_at) VALUES
(1, '现代简约', 'free', 0.00, '简洁现代，适合各种职位', '/templates/modern.html', NOW()),
(2, '经典商务', 'free', 0.00, '适合传统行业和商务场合', '/templates/classic.html', NOW()),
(3, '创意设计', 'premium', 29.00, '适合设计师和创意工作者', '/templates/modern.html', NOW()),
(4, '技术专业', 'premium', 35.00, '专为技术人员优化', '/templates/modern.html', NOW()),
(5, '高端商务', 'premium', 45.00, '高级管理层专用模板', '/templates/classic.html', NOW()),
(6, '学术研究', 'free', 0.00, '适合学术界和研究人员', '/templates/classic.html', NOW())
ON DUPLICATE KEY UPDATE 
    name=VALUES(name), 
    type=VALUES(type), 
    price=VALUES(price), 
    description=VALUES(description), 
    template_path=VALUES(template_path); 