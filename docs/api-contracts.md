# CareerFlow API Specifications

All endpoints are prefixed with `/api/v1` and are routed via the API Gateway (`http://localhost:9000`). Requests must pass a valid Keycloak JWT Access Token within the `Authorization: Bearer <TOKEN>` header.

---

## 1. User Service (`/api/v1/users`)

Manages the core candidate profile and targets.

### 1.1 Get My Profile & Targets
* **Endpoint**: `GET /api/v1/users/me`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**:
```json
{
  "id": "8430b8f0-1090-410a-b31c-d784a9e527b1",
  "email": "candidate@careerflow.com",
  "firstName": "John",
  "lastName": "Doe",
  "targetRoles": "Senior Java Developer, Staff Engineer",
  "targetSalaryMin": 130000.00,
  "targetSalaryMax": 170000.00,
  "skills": ["Java", "Spring Boot", "Docker", "PostgreSQL", "OAuth2"],
  "createdAt": "2026-06-18T10:00:00Z"
}
```

### 1.2 Update My Profile & Targets
* **Endpoint**: `PUT /api/v1/users/profile`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "targetRoles": "Staff Backend Engineer",
  "targetSalaryMin": 150000.00,
  "targetSalaryMax": 190000.00,
  "skills": ["Java 21", "Spring Boot 3", "Kubernetes", "OAuth2"]
}
```
* **Response (HTTP 200)**: Same updated schema as Get My Profile.

---

## 2. Resume Service (`/api/v1/resumes`)

Manages candidate's multiple resume versions.

### 2.1 Register Resume Version
* **Endpoint**: `POST /api/v1/resumes`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "versionName": "Stripe-Staff-Backend",
  "resumeUrl": "https://careerflow-bucket.s3.amazonaws.com/resumes/stripe-staff-v1.pdf",
  "tailorNotes": "Emphasized financial transactions ledger design and high concurrency Spring experience."
}
```
* **Response (HTTP 201)**:
```json
{
  "id": "e4b6c31a-e8db-4e2b-bb66-cf318182b852",
  "versionName": "Stripe-Staff-Backend",
  "resumeUrl": "https://careerflow-bucket.s3.amazonaws.com/resumes/stripe-staff-v1.pdf",
  "tailorNotes": "Emphasized financial transactions ledger design and high concurrency Spring experience.",
  "createdAt": "2026-06-18T10:05:00Z"
}
```

### 2.2 List Resumes
* **Endpoint**: `GET /api/v1/resumes`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**:
```json
[
  {
    "id": "e4b6c31a-e8db-4e2b-bb66-cf318182b852",
    "versionName": "Stripe-Staff-Backend",
    "resumeUrl": "https://careerflow-bucket.s3.amazonaws.com/resumes/stripe-staff-v1.pdf",
    "createdAt": "2026-06-18T10:05:00Z"
  }
]
```

### 2.3 Delete Resume Metadata
* **Endpoint**: `DELETE /api/v1/resumes/{id}`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 204)**: No Content.

---

## 3. Application Service (`/api/v1/applications`)

Tracks job targets, referrals, and compensation offers.

### 3.1 Create Job Application Target
* **Endpoint**: `POST /api/v1/applications`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "companyName": "Stripe",
  "roleTitle": "Staff Backend Engineer",
  "jobUrl": "https://stripe.com/jobs/staff-backend-129481",
  "companyNotes": "Uses Ruby and Java. Highly focused on API and API Gateways scalability.",
  "source": "REFERRAL",
  "status": "WISHLIST",
  "resumeId": "e4b6c31a-e8db-4e2b-bb66-cf318182b852",
  "referrer": {
    "referrerName": "Jane Smith",
    "referrerEmail": "janesmith@stripe.com",
    "referrerCompany": "Stripe",
    "connectionType": "COWORKER",
    "status": "REQUESTED"
  }
}
```
* **Response (HTTP 201)**:
```json
{
  "id": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
  "companyName": "Stripe",
  "roleTitle": "Staff Backend Engineer",
  "jobUrl": "https://stripe.com/jobs/staff-backend-129481",
  "companyNotes": "Uses Ruby and Java. Highly focused on API and API Gateways scalability.",
  "source": "REFERRAL",
  "status": "WISHLIST",
  "resumeId": "e4b6c31a-e8db-4e2b-bb66-cf318182b852",
  "createdAt": "2026-06-18T10:10:00Z",
  "referrer": {
    "id": "d0e1f37e-7512-4cfb-819a-9e1234bcf123",
    "referrerName": "Jane Smith",
    "referrerEmail": "janesmith@stripe.com",
    "referrerCompany": "Stripe",
    "connectionType": "COWORKER",
    "status": "REQUESTED"
  }
}
```

### 3.2 List Job Applications
* **Endpoint**: `GET /api/v1/applications`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**:
```json
[
  {
    "id": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
    "companyName": "Stripe",
    "roleTitle": "Staff Backend Engineer",
    "status": "WISHLIST",
    "createdAt": "2026-06-18T10:10:00Z"
  }
]
```

### 3.3 Fetch Full Application Details
* **Endpoint**: `GET /api/v1/applications/{id}`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**:
```json
{
  "id": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
  "companyName": "Stripe",
  "roleTitle": "Staff Backend Engineer",
  "jobUrl": "https://stripe.com/jobs/staff-backend-129481",
  "companyNotes": "Uses Ruby and Java. Highly focused on API and API Gateways scalability.",
  "source": "REFERRAL",
  "status": "WISHLIST",
  "resumeId": "e4b6c31a-e8db-4e2b-bb66-cf318182b852",
  "createdAt": "2026-06-18T10:10:00Z",
  "referrer": {
    "id": "d0e1f37e-7512-4cfb-819a-9e1234bcf123",
    "referrerName": "Jane Smith",
    "referrerEmail": "janesmith@stripe.com",
    "connectionType": "COWORKER",
    "status": "REQUESTED"
  },
  "offer": {
    "id": "f5819ca1-285b-4395-ac4f-6a9c1e1498b3",
    "baseSalary": 160000.00,
    "equityValYearly": 45000.00,
    "signOnBonus": 15000.00,
    "performanceBonusPercent": 15.00,
    "currency": "USD",
    "status": "PENDING"
  }
}
```

### 3.4 Transition Application Status
* **Endpoint**: `PATCH /api/v1/applications/{id}/status`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "status": "INTERVIEWING"
}
```
* **Response (HTTP 200)**: Same as Full Application Details.
* *Side-Effect*: Generates a career activity event automatically.

### 3.5 Attach Offer details
* **Endpoint**: `POST /api/v1/applications/{id}/offers`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "baseSalary": 160000.00,
  "equityValYearly": 45000.00,
  "signOnBonus": 15000.00,
  "performanceBonusPercent": 15.00,
  "currency": "USD",
  "status": "PENDING",
  "expiresAt": "2026-07-01T17:00:00Z"
}
```
* **Response (HTTP 201)**: Standard Offer JSON response.

### 3.6 Chronological Activity Feed
* **Endpoint**: `GET /api/v1/applications/activities`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**:
```json
[
  {
    "id": "a181fa3a-231a-4d14-8789-9a2c3d4e5f1b",
    "applicationId": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
    "activityType": "STATUS_CHANGED",
    "description": "Moved application at Stripe for Staff Backend Engineer to INTERVIEWING.",
    "loggedAt": "2026-06-18T10:15:00Z"
  }
]
```

### 3.7 Candidate Dashboard Aggregator API
* **Endpoint**: `GET /api/v1/applications/dashboard`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**:
```json
{
  "totalApplications": 24,
  "activeInterviews": 3,
  "offersReceived": 2,
  "rejections": 5,
  "applicationsByStatus": {
    "WISHLIST": 4,
    "APPLIED": 10,
    "INTERVIEWING": 3,
    "OFFERED": 2,
    "REJECTED": 5
  }
}
```

---

## 4. Interview Service (`/api/v1/interviews`)

Handles specific interview rounds and post-round retros.

### 4.1 Schedule Interview Round
* **Endpoint**: `POST /api/v1/interviews`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "applicationId": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
  "roundName": "System Design & Scalability",
  "scheduledAt": "2026-06-21T15:00:00Z",
  "durationMinutes": 60,
  "interviewerNames": "Alex Rivera (Staff Architect)",
  "topicsCovered": "Distributed Ledger scaling, idempotent transactions, API gateway token relay."
}
```
* **Response (HTTP 201)**:
```json
{
  "id": "99ea78b0-51c3-41bb-9884-7a312ba9cfab",
  "applicationId": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
  "roundName": "System Design & Scalability",
  "scheduledAt": "2026-06-21T15:00:00Z",
  "durationMinutes": 60,
  "interviewerNames": "Alex Rivera (Staff Architect)",
  "topicsCovered": "Distributed Ledger scaling, idempotent transactions, API gateway token relay.",
  "status": "SCHEDULED"
}
```

### 4.2 Submit Post-Interview Retro Log
* **Endpoint**: `POST /api/v1/interviews/{id}/retro`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Request Body**:
```json
{
  "whatWentWell": "Answered standard questions about high-concurrency ledger patterns perfectly, matching Stripe design docs.",
  "challengesFaced": "Felt rushed during DB partitioning strategy discussions.",
  "questionsAsked": "How does the platform handle dual writes across ledger domains?",
  "confidenceRating": 4
}
```
* **Response (HTTP 201)**:
```json
{
  "id": "e8ba98b0-21a4-4a5f-9e11-12a3bc9df155",
  "interviewId": "99ea78b0-51c3-41bb-9884-7a312ba9cfab",
  "whatWentWell": "Answered standard questions about high-concurrency ledger patterns perfectly, matching Stripe design docs.",
  "challengesFaced": "Felt rushed during DB partitioning strategy discussions.",
  "questionsAsked": "How does the platform handle dual writes across ledger domains?",
  "confidenceRating": 4,
  "createdAt": "2026-06-21T16:15:00Z"
}
```

### 4.3 Get Active/Upcoming Interviews count (Internal/Dashboard Utility)
* **Endpoint**: `GET /api/v1/interviews/count/active`
* **Role Enforced**: `ROLE_CANDIDATE`
* **Response (HTTP 200)**: `3` (Standard raw HTTP response containing count).
