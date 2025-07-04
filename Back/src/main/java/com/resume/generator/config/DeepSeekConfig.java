package com.resume.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deepseek")
@Data
public class DeepSeekConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
}