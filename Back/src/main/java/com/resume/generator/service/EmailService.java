package com.resume.generator.service;

import com.resume.generator.entity.VerificationRecord;
import com.resume.generator.repository.VerificationRecordRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationRecordRepository verificationRecordRepository;

    @Autowired
    private HttpServletRequest request;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 验证码有效期（分钟）
    private static final int VERIFICATION_CODE_EXPIRATION = 5;

    @Transactional
    public void sendVerificationCode(String email) throws MessagingException {
        String code = generateVerificationCode();
        String ipAddress = getClientIp();

        // 保存验证码记录
        VerificationRecord record = VerificationRecord.builder()
                .email(email)
                .code(code)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION))
                .used(false)
                .build();

        verificationRecordRepository.save(record);

        // 发送HTML邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Hyper 简生 - 注册验证码");

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        .email-container {
                            font-family: 'Helvetica Neue', Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f7f7f7;
                        }
                        .header {
                            background-color: #2196F3;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: white;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .verification-code {
                            font-size: 32px;
                            font-weight: bold;
                            color: #2196F3;
                            text-align: center;
                            padding: 20px;
                            margin: 20px 0;
                            background-color: #f0f8ff;
                            border-radius: 5px;
                            letter-spacing: 5px;
                        }
                        .expiration {
                            color: #666;
                            text-align: center;
                            font-size: 14px;
                            margin-top: 20px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #999;
                            font-size: 12px;
                        }
                        .warning {
                            color: #ff5722;
                            font-size: 13px;
                            margin-top: 20px;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h2>Hyper 简生</h2>
                        </div>
                        <div class="content">
                            <p>尊敬的用户：</p>
                            <p>您好！感谢您注册Hyper 简生。请使用以下验证码完成注册：</p>
                            <div class="verification-code">%s</div>
                            <p class="expiration">验证码有效期为 %d 分钟</p>
                            <p class="warning">如果这不是您的操作，请忽略此邮件。</p>
                        </div>
                        <div class="footer">
                            <p>此邮件由系统自动发送，请勿回复</p>
                            <p>© %d 小泥人Hyper</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(code, VERIFICATION_CODE_EXPIRATION, LocalDateTime.now().getYear());

        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        return verificationRecordRepository
                .findByEmailAndCodeAndUsedFalseAndExpiresAtAfter(email, code, LocalDateTime.now())
                .map(record -> {
                    record.setUsed(true);
                    verificationRecordRepository.save(record);
                    return true;
                })
                .orElse(false);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}