# CareerFlow Setup & Installation Guide

This guide will walk you through setting up the local Docker environment, configuring PostgreSQL databases, bootstrapping Keycloak, and running the CareerFlow microservices ecosystem.

---

## 1. Prerequisites

Ensure you have the following installed on your machine:
* **Java**: JDK 21 (Temurin or any standard OpenJDK distribution)
* **Build Tool**: Gradle 8.x/9.x (or Gradle Wrapper)
* **Containerization**: Docker (Engine 24.0+) & Docker Compose v2+
* **REST Client**: Postman (for end-to-end OAuth2 token generation and API testing)

---

## 2. Infrastructure Setup (Docker Compose)

CareerFlow relies on a shared Docker network and single PostgreSQL instance running multiple logical databases, coupled with a Keycloak identity manager.

### 2.1 Database Initialization Script
Create an initialization SQL script at `infrastructure/postgres/init.sql` to automatically bootstrap logical databases when the container is spun up:

```sql
CREATE DATABASE careerflow_user;
CREATE DATABASE careerflow_resume;
CREATE DATABASE careerflow_application;
CREATE DATABASE careerflow_interview;
CREATE DATABASE keycloak_db;
```

### 2.2 Docker Compose Configuration (`docker-compose.yml`)
The core environment runs using this `docker-compose.yml` situated at the project root:

```yaml
version: '3.8'

networks:
  careerflow-network:
    driver: bridge

services:
  postgres:
    image: postgres:16-alpine
    container_name: careerflow-postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - ./postgres/init.sql:/docker-entrypoint-initdb.d/init.sql
      - postgres_data:/var/lib/postgresql/data
    networks:
      - careerflow-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:24.0.5
    container_name: careerflow-keycloak
    command: start-dev --import-realm
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak_db
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: password
    ports:
      - "8080:8080"
    volumes:
      - ./keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - careerflow-network
```

---

## 3. Keycloak Setup & Configuration Strategy

To guarantee immediate developer onboarding, we pre-bake roles and client structures into an importable JSON file.

### 3.1 Realm Import JSON Location
Create the directory and copy your pre-configured realm settings to:
`infrastructure/keycloak/realm-export.json`

This file automatically provisions:
* **Realm**: `careerflow-realm`
* **Roles**: `CANDIDATE`, `ADMIN`
* **OAuth2 Client**: `careerflow-api-gateway`
  - Access Type: Public
  - Standard Flow Enabled: Yes (Authorization Code Flow)
  - Direct Access Grants Enabled: Yes (for Postman resource-owner password grant testing)
  - Redirect URIs: `http://localhost:8080/*`, `https://oauth.pstmn.io/v1/callback`
  - Proof Key for Code Exchange (PKCE): Required
* **Default Demo User**:
  - Username: `candidate@careerflow.com`
  - Password: `password`
  - Assigned Realm Role: `CANDIDATE`

---

## 4. Spin Up Ecosystem

Run the following commands from your project root or `infrastructure/` directory:

```bash
# 1. Navigate to infrastructure folder
cd infrastructure

# 2. Spin up PostgreSQL and Keycloak in background
docker compose up -d

# 3. Verify containers are running healthy
docker compose ps
```

Verify Keycloak is responsive by pointing your browser to:
`http://localhost:8080/admin` (Sign in using `admin`/`admin` to inspect the `careerflow-realm` configuration).

---

## 5. Next Steps

Your local backing infrastructure is fully active and waiting for microservice connections. Refer to `docs/local-development.md` for building and launching the Spring Boot service microservices.
