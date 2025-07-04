package com.resume.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ResumeGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeGeneratorApplication.class, args);
    }
}