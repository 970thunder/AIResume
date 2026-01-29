SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for interview_questions
-- ----------------------------
DROP TABLE IF EXISTS `interview_questions`;
CREATE TABLE `interview_questions` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目内容',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPEN_ENDED' COMMENT '题目类型: OPEN_ENDED, MULTIPLE_CHOICE',
  `options` json NULL COMMENT '选项(选择题用)',
  `reference_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '参考答案',
  `difficulty` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM' COMMENT '难度: EASY, MEDIUM, HARD',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类/技术栈',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic COMMENT = '面试题目表';

-- ----------------------------
-- Table structure for interview_sessions
-- ----------------------------
DROP TABLE IF EXISTS `interview_sessions`;
CREATE TABLE `interview_sessions` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '关联用户ID',
  `score` int DEFAULT 0 COMMENT '得分',
  `total_questions` int DEFAULT 10 COMMENT '题目总数',
  `status` varchar(20) DEFAULT 'IN_PROGRESS' COMMENT '状态: IN_PROGRESS, COMPLETED',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_interview_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic COMMENT = '面试会话表';

-- ----------------------------
-- Table structure for interview_records
-- ----------------------------
DROP TABLE IF EXISTS `interview_records`;
CREATE TABLE `interview_records` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '关联用户ID',
  `session_id` bigint(0) NOT NULL COMMENT '关联会话ID',
  `question_id` bigint(0) NOT NULL COMMENT '关联题目ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户回答',
  `score` int DEFAULT 0 COMMENT '得分',
  `is_correct` boolean DEFAULT NULL COMMENT '是否正确',
  `ai_evaluation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI评价',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_user_id`(`user_id`) USING BTREE,
  INDEX `idx_record_session_id`(`session_id`) USING BTREE,
  INDEX `idx_record_question_id`(`question_id`) USING BTREE,
  CONSTRAINT `fk_interview_records_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_interview_records_session` FOREIGN KEY (`session_id`) REFERENCES `interview_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_interview_records_question` FOREIGN KEY (`question_id`) REFERENCES `interview_questions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic COMMENT = '答题记录表';

SET FOREIGN_KEY_CHECKS = 1;
