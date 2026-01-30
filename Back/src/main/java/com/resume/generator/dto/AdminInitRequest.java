package com.resume.generator.dto;

import lombok.Data;

@Data
public class AdminInitRequest {
    private String username;
    private String email;
    private String password;
}
