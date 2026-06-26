package com.careerflow.applicationservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestJwtDecoderConfig {

    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
            .header("alg", "none")
            .subject(extractSubject(token))
            .build();
    }

    private String extractSubject(String token) {
        if (token == null || token.isBlank()) {
            return "test-user";
        }
        return token;
    }
}
