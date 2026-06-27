# CareerFlow API Overview

This document describes **implemented APIs** as of Phase 2. It complements the Bruno collection in `bruno/`.

**Base URLs:**

- Gateway: `http://localhost:9000`
- User Service (direct): `http://localhost:8081`
- Application Service (direct): `http://localhost:8083`

All business endpoints require:

```http
Authorization: Bearer <access_token>
```

Obtain a token via Keycloak (see Bruno **Auth → Get Candidate Token**) or:

```http
POST http://localhost:8080/realms/careerflow-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=careerflow-api-gateway&username=candidate@careerflow.com&password=password
```

---

## Gateway Routes

| Path prefix | Upstream | Status |
|-------------|----------|--------|
| `/api/v1/users/**` | User Service `:8081` | Implemented |
| `/api/v1/applications/**` | Application Service `:8083` | Implemented |
| `/api/v1/resumes/**` | Resume Service `:8082` | **Not implemented** |
| `/api/v1/interviews/**` | Interview Service `:8084` | **Not implemented** |

Actuator (no auth required on services):

- Gateway: `GET http://localhost:9000/actuator/health`
- User Service: `GET http://localhost:8081/actuator/health`
- Application Service: `GET http://localhost:8083/actuator/health`

---

## User Service

Base path: `/api/v1/users`

Authentication: Valid JWT required. Role checks (`ROLE_CANDIDATE`) are **not enforced** in code yet.

---

### GET /api/v1/users/me

**Purpose:** Return the authenticated user, syncing from Keycloak on first request if not yet persisted.

**Request body:** None

**Response `200 OK`:**

```json
{
  "id": "8430b8f0-1090-410a-b31c-d784a9e527b1",
  "email": "candidate@careerflow.com",
  "firstName": "John",
  "lastName": "Candidate",
  "role": "CANDIDATE"
}
```

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |
| 500 | Database/repository failure |

---

### GET /api/v1/users/me/profile

**Purpose:** Return the candidate profile (target roles, salary range, skills).

**Request body:** None

**Response `200 OK`:**

```json
{
  "userId": "8430b8f0-1090-410a-b31c-d784a9e527b1",
  "targetRoles": "Senior Java Developer, Staff Engineer",
  "targetSalaryMin": 130000.0,
  "targetSalaryMax": 170000.0,
  "skills": ["Java", "Spring Boot", "PostgreSQL"]
}
```

If no profile exists, an empty profile for the user is returned.

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |

---

### PUT /api/v1/users/me/profile

**Purpose:** Update candidate profile fields.

**Request body:**

```json
{
  "targetRoles": "Staff Backend Engineer",
  "targetSalaryMin": 150000.0,
  "targetSalaryMax": 190000.0,
  "skills": ["Java 21", "Spring Boot 3", "Kubernetes"]
}
```

**Response `200 OK`:** Same shape as GET profile.

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |
| 500 | User not found (should not occur after `/me` sync) |

---

## Application Service

Base path: `/api/v1/applications`

Authentication: Valid JWT required. All data scoped to JWT `sub`.

---

### POST /api/v1/applications

**Purpose:** Create a new job application.

**Request body:**

```json
{
  "companyName": "Stripe",
  "jobTitle": "Staff Backend Engineer",
  "location": "Remote",
  "jobUrl": "https://stripe.com/jobs/staff-backend-129481",
  "source": "REFERRAL",
  "status": "APPLIED",
  "applicationDate": "2026-06-18",
  "notes": "Referred by a former coworker.",
  "referralInfo": {
    "referred": true,
    "referrerName": "Jane Smith",
    "referrerCompanyEmail": "jane@stripe.com",
    "relationship": "Coworker"
  }
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `companyName` | Yes | |
| `jobTitle` | Yes | |
| `source` | Yes | Enum — see below |
| `status` | No | Defaults to `WISHLIST` |
| `jobUrl` | No | Must be valid URL if provided |
| `referralInfo` | No | Embedded value object |

**Response `201 Created`:**

```json
{
  "id": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
  "companyName": "Stripe",
  "jobTitle": "Staff Backend Engineer",
  "location": "Remote",
  "jobUrl": "https://stripe.com/jobs/staff-backend-129481",
  "source": "REFERRAL",
  "status": "APPLIED",
  "applicationDate": "2026-06-18",
  "notes": "Referred by a former coworker.",
  "referralInfo": {
    "referred": true,
    "referrerName": "Jane Smith",
    "referrerCompanyEmail": "jane@stripe.com",
    "relationship": "Coworker"
  },
  "createdAt": "2026-06-18T10:10:00Z",
  "updatedAt": "2026-06-18T10:10:00Z",
  "version": 0
}
```

**Side effect:** Creates an `APPLICATION_CREATED` activity.

**Common errors:**

| Status | Cause |
|--------|-------|
| 400 | Validation failure (missing company/title, invalid URL, invalid enum) |
| 401 | Missing or invalid JWT |

Validation errors return RFC 7807 `ProblemDetail` with field messages.

---

### GET /api/v1/applications

**Purpose:** List applications for the authenticated user with optional filters and pagination.

**Query parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `status` | enum | Filter by application status |
| `company` | string | Case-insensitive partial match on company name |
| `page` | int | Page index (default 0) |
| `size` | int | Page size (default 20) |
| `sort` | string | Spring Data sort (e.g. `createdAt,desc`) |

**Example:** `GET /api/v1/applications?status=INTERVIEWING&company=Google&page=0&size=20`

**Response `200 OK`:** Spring Data `Page` JSON:

```json
{
  "content": [
    {
      "id": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
      "companyName": "Google",
      "jobTitle": "Senior Software Engineer",
      "status": "INTERVIEWING",
      "applicationDate": "2026-06-01",
      "createdAt": "2026-06-18T10:10:00Z"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |

---

### GET /api/v1/applications/{id}

**Purpose:** Full application detail including optional offer and recent activities.

**Request body:** None

**Response `200 OK`:**

```json
{
  "application": {
    "id": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
    "companyName": "Stripe",
    "jobTitle": "Staff Backend Engineer",
    "location": "Remote",
    "jobUrl": "https://stripe.com/jobs/staff-backend-129481",
    "source": "REFERRAL",
    "status": "INTERVIEWING",
    "applicationDate": "2026-06-18",
    "notes": "Referred by a former coworker.",
    "referralInfo": {
      "referred": true,
      "referrerName": "Jane Smith",
      "referrerCompanyEmail": "jane@stripe.com",
      "relationship": "Coworker"
    },
    "createdAt": "2026-06-18T10:10:00Z",
    "updatedAt": "2026-06-18T11:00:00Z",
    "version": 1
  },
  "offer": {
    "id": "f5819ca1-285b-4395-ac4f-6a9c1e1498b3",
    "applicationId": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
    "baseSalary": 160000.00,
    "joiningBonus": 15000.00,
    "annualBonus": 20000.00,
    "stockValue": 45000.00,
    "currency": "USD",
    "joiningDate": "2026-08-01",
    "offerStatus": "PENDING",
    "notes": "Standard package",
    "createdAt": "2026-06-18T12:00:00Z"
  },
  "recentActivities": [
    {
      "id": "a181fa3a-231a-4d14-8789-9a2c3d4e5f1b",
      "applicationId": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
      "type": "STATUS_CHANGED",
      "description": "Status changed from APPLIED to INTERVIEWING for Stripe - Staff Backend Engineer",
      "createdAt": "2026-06-18T11:00:00Z"
    }
  ]
}
```

`offer` is `null` if no offer exists. `recentActivities` returns up to 10 entries.

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |
| 404 | Application not found or belongs to another user |

---

### PATCH /api/v1/applications/{id}/status

**Purpose:** Update application status. Any status may transition to any other status (no workflow validation in v1).

**Request body:**

```json
{
  "status": "INTERVIEWING"
}
```

**Response `200 OK`:** Same shape as GET detail (application + offer + recentActivities).

**Side effect:** Creates a `STATUS_CHANGED` activity.

**Common errors:**

| Status | Cause |
|--------|-------|
| 400 | Missing or invalid status enum |
| 401 | Missing or invalid JWT |
| 404 | Application not found or belongs to another user |

---

### PUT /api/v1/applications/{id}/offer

**Purpose:** Create or replace the offer for an application (one offer per application).

**Request body:**

```json
{
  "baseSalary": 160000.00,
  "joiningBonus": 15000.00,
  "annualBonus": 20000.00,
  "stockValue": 45000.00,
  "currency": "USD",
  "joiningDate": "2026-08-01",
  "offerStatus": "PENDING",
  "notes": "Standard offer package"
}
```

| Field | Required | Validation |
|-------|----------|------------|
| `currency` | Yes | Exactly 3 uppercase letters (ISO 4217 style, e.g. `USD`) |
| Salary fields | No | If present, must be `>= 0` |

**Response `200 OK`:** Same shape as GET detail.

**Side effect:** Creates `OFFER_ADDED` or `OFFER_UPDATED` activity.

**Common errors:**

| Status | Cause |
|--------|-------|
| 400 | Validation failure (currency format, negative salary) |
| 401 | Missing or invalid JWT |
| 404 | Application not found or belongs to another user |

---

### GET /api/v1/applications/activities

**Purpose:** Chronological activity feed for the authenticated user across all applications.

**Query parameters:**

| Param | Default | Description |
|-------|---------|-------------|
| `limit` | 50 | Max entries (capped at 100) |

**Response `200 OK`:**

```json
[
  {
    "id": "a181fa3a-231a-4d14-8789-9a2c3d4e5f1b",
    "applicationId": "c1f729db-6014-4fb5-9fb9-dcbca9c00424",
    "type": "STATUS_CHANGED",
    "description": "Status changed from APPLIED to INTERVIEWING for Stripe - Staff Backend Engineer",
    "createdAt": "2026-06-18T11:00:00Z"
  }
]
```

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |

---

### GET /api/v1/applications/dashboard

**Purpose:** Aggregated metrics for the authenticated user's pipeline.

**Request body:** None

**Response `200 OK`:**

```json
{
  "totalApplications": 24,
  "activeInterviews": 3,
  "offersReceived": 2,
  "rejections": 5,
  "responseRate": 41.67,
  "applicationsByStatus": {
    "WISHLIST": 4,
    "APPLIED": 10,
    "INTERVIEWING": 3,
    "OFFERED": 2,
    "REJECTED": 5
  }
}
```

| Field | Definition |
|-------|------------|
| `activeInterviews` | Count where status = `INTERVIEWING` |
| `offersReceived` | Count where status = `OFFERED` |
| `rejections` | Count where status = `REJECTED` |
| `responseRate` | `(count in ASSESSMENT, INTERVIEWING, OFFERED, HIRED, REJECTED) / (total - WISHLIST) × 100` |
| `applicationsByStatus` | Map of status → count (statuses with zero count may be omitted) |

Metrics are computed via DB aggregation queries, not by loading all rows into memory.

**Common errors:**

| Status | Cause |
|--------|-------|
| 401 | Missing or invalid JWT |

---

## Enumerations

### ApplicationStatus

`WISHLIST`, `APPLIED`, `ASSESSMENT`, `INTERVIEWING`, `OFFERED`, `HIRED`, `REJECTED`, `WITHDRAWN`

### ApplicationSource

`LINKEDIN`, `REFERRAL`, `COMPANY_WEBSITE`, `INDEED`, `NAUKRI`, `OTHER`

### ActivityType

`APPLICATION_CREATED`, `STATUS_CHANGED`, `NOTE_UPDATED`, `OFFER_ADDED`, `OFFER_UPDATED`

(`NOTE_UPDATED` is defined but not yet emitted by any endpoint.)

### OfferStatus

`PENDING`, `ACCEPTED`, `REJECTED`, `NEGOTIATING`, `EXPIRED`

---

## Global Error Format (Application Service)

Application Service returns RFC 7807 `ProblemDetail` for handled errors:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Application not found"
}
```

User Service does not yet have a global exception handler.

---

## Planned APIs (Not Implemented)

The following are documented in [api-contracts.md](./api-contracts.md) but **do not exist** in code:

- Resume Service (`/api/v1/resumes/**`)
- Interview Service (`/api/v1/interviews/**`)

Gateway routes for these paths will fail until services are implemented.

---

## Bruno Collection Mapping

| Bruno request | Endpoint |
|---------------|----------|
| Create Application | `POST /api/v1/applications` |
| List Applications | `GET /api/v1/applications` |
| Get Application Details | `GET /api/v1/applications/{id}` |
| Update Application Status | `PATCH /api/v1/applications/{id}/status` |
| Upsert Offer | `PUT /api/v1/applications/{id}/offer` |
| Get Activity Timeline | `GET /api/v1/applications/activities` |
| Get Dashboard | `GET /api/v1/applications/dashboard` |
| Get Current User | `GET /api/v1/users/me` |
| Get Candidate Profile | `GET /api/v1/users/me/profile` |
| Update Candidate Profile | `PUT /api/v1/users/me/profile` |

Use `{{applicationId}}` variable in Bruno for ID-based requests after creating an application.
