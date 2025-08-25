package com.example.aitutor.debug;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class EnvLog {

    @Value("${spring.datasource.url:NOT_SET}")
    private String dbUrl;

    @Value("${spring.datasource.username:NOT_SET}")
    private String dbUser;

    @Value("${spring.datasource.password:NOT_SET}")
    private String dbPass;

    @PostConstruct
    public void printEnv() {
        System.out.println("✅ [EnvLog] spring.datasource.url = " + dbUrl);
        System.out.println("✅ [EnvLog] spring.datasource.username = " + dbUser);
        System.out.println("✅ [EnvLog] spring.datasource.password = " + (dbPass.isEmpty() ? "(empty)" : "(provided)"));
    }
}
