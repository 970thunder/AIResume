package com.resume.generator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    private String email;
    private String verificationCode;
}
