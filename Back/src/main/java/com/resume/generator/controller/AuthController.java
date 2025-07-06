package com.resume.generator.controller;

import com.resume.generator.dto.AuthResponse;
import com.resume.generator.dto.LoginRequest;
import com.resume.generator.dto.RegisterRequest;
import com.resume.generator.auth.AuthService;
import com.resume.generator.service.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam @Email(message = "请输入有效的邮箱地址") String email) {
        boolean exists = authService.isEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // 验证验证码
        if (!emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            return ResponseEntity.badRequest().body("验证码错误或已过期");
        }
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam @Email(message = "请输入有效的邮箱地址") String email) {
        // 先检查邮箱是否已注册
        if (authService.isEmailExists(email)) {
            return ResponseEntity.badRequest().body("该邮箱已被注册");
        }

        try {
            emailService.sendVerificationCode(email);
            return ResponseEntity.ok().body("验证码已发送到邮箱");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发送验证码失败：" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}