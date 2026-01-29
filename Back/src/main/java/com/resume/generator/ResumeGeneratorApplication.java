package com.resume.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
public class ResumeGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeGeneratorApplication.class, args);
    }
}