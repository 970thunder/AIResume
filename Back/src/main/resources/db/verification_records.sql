-- 创建验证码记录表
CREATE TABLE verification_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 添加索引
    INDEX idx_email (email),
    INDEX idx_code (code),
    INDEX idx_expires_at (expires_at),
    INDEX idx_email_code (email, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加注释
ALTER TABLE verification_records
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN email VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    MODIFY COLUMN code VARCHAR(6) NOT NULL COMMENT '验证码',
    MODIFY COLUMN ip_address VARCHAR(50) NOT NULL COMMENT '用户IP地址',
    MODIFY COLUMN created_at DATETIME NOT NULL COMMENT '创建时间',
    MODIFY COLUMN expires_at DATETIME NOT NULL COMMENT '过期时间',
    MODIFY COLUMN is_used BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已使用'; 