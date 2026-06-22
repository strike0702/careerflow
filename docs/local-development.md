# CareerFlow Local Development Guide

This document outlines configurations, properties, custom converter patterns, and run instructions necessary to build, run, and test the CareerFlow microservices on a local development machine.

---

## 1. Directory Structure Blueprint

The project is organized as a Gradle multi-module monorepo:

```
careerflow/
├── README.md
├── docs/                           # Documentation
├── infrastructure/                 # Keycloak & Postgres orchestration
│   ├── docker-compose.yml
│   ├── keycloak/
│   └── postgres/
└── backend/                        # JVM Microservices (Gradle Monorepo)
    ├── build.gradle                # Central build script (Spring version definition)
    ├── settings.gradle             # Module imports declaration
    ├── api-gateway/                # Spring Cloud Gateway
    └── user-service/               # User Service
```

---

## 2. Shared Spring Boot Configuration Pattern

Each microservice acting as an OAuth2 Resource Server validates Keycloak Access Tokens locally using the following `src/main/resources/application.yml` layout:

### 2.1 Microservice Security Configuration Example (`application.yml`)
```yaml
server:
  port: 8083 # Vary port for each service

spring:
  application:
    name: application-service
  datasource:
    url: jdbc:postgresql://localhost:5432/careerflow_application
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  security:
    oauth2:
      resourceserver:
        jwt:
          # Points to Keycloak JWKS endpoint running inside Docker container (resolved from local host)
          jwk-set-uri: http://localhost:8080/realms/careerflow-realm/protocol/openid-connect/certs
```

---

## 3. JWT Custom Authority Converter

To permit Spring Security to read Keycloak roles, register a custom `JwtAuthenticationConverter` bean within each service's Security Configuration class:

```java
package com.careerflow.shared.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return jwtConverter;
    }

    private static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) {
                return List.of();
            }
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        }
    }
}
```

---

## 4. Cross-Service Communication (OpenFeign)

The **Application Service** queries the **Interview Service** to retrieve active interview counts for its Dashboard API.

### 4.1 Feign Client Definition
Define the client interface in Application Service:

```java
package com.careerflow.applicationservice.adapters.out.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "interview-service", url = "http://localhost:8084")
public interface InterviewServiceClient {

    @GetMapping("/api/v1/interviews/count/active")
    long getActiveInterviewCount(@RequestHeader("Authorization") String bearerToken);
}
```

> **Security Note**: We must explicitly pass down the `Authorization` header so the downstream resource server can authenticate the request.

---

## 5. Development Build & Run Workflow

Follow this sequence to boot up the backend microservices locally:

```bash
# 1. Navigate to the backend directory
cd backend

# 2. Clean build all projects via Gradle Wrapper
./gradlew clean build -x test

# 3. Run API Gateway
./gradlew :api-gateway:bootRun

# 4. Open a new terminal and run User Service
./gradlew :user-service:bootRun
```

---

## 6. Troubleshooting Local Security

### Token Issuer Error
If you receive `Jwt validation failed: Issuer (iss) claim value does not match expected value`:
Ensure that the issuer URI in Keycloak matches what the services expect. When Keycloak runs on Docker, it might advertise `http://keycloak:8080/realms/careerflow-realm`, but your services on localhost expect `http://localhost:8080/realms/careerflow-realm`.
* Solution: Access Keycloak locally using `http://localhost:8080` and verify JWKS config parameters in Spring Boot reference `localhost`.
