# HireSmart ATS — AI-Powered Interview & Resume Screening

[![Java](https://img.shields.io/badge/Java-21%2F22-blue.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-orange.svg)](https://openjfx.io/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.5-red.svg)](https://poi.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)]()

> **HireSmart ATS** is an enterprise-grade Applicant Tracking System (ATS) and AI-assisted recruitment platform. It connects a fluid, smoothly animated **JavaFX Desktop Client** with a resilient **Spring Boot REST API backend**, **JDBC repositories**, **MySQL database** (with automatic zero-config embedded fallback), **Apache PDFBox** resume parsing, **Apache POI** Excel reporting, **Jakarta Mail** email notifications, and an interactive **AI Mock Interview Practice Studio**.

---

## 📑 Table of Contents
1. [Architecture Overview](#-architecture-overview)
2. [Key Features & Modules](#-key-features--modules)
3. [Technology Stack](#-technology-stack)
4. [Prerequisites & System Requirements](#-prerequisites--system-requirements)
5. [Database Configuration](#-database-configuration)
6. [Quick Start & Launch Commands (Windows PowerShell)](#-quick-start--launch-commands-windows-powershell)
7. [Pre-Seeded Demo Accounts](#-pre-seeded-demo-accounts)
8. [Automated Testing](#-automated-testing)
9. [REST API Endpoints Reference](#-rest-api-endpoints-reference)
10. [Troubleshooting & Common FAQs](#-troubleshooting--common-faqs)

---

## 🏛 Architecture Overview

HireSmart ATS strictly implements a decoupled, 3-tier enterprise architecture:

```
┌────────────────────────────────────────────────────────┐
│             JavaFX Desktop User Interface              │
│       FXML Layouts + Vanilla CSS Modern Styling        │
│   Smooth JavaFX Transitions (Fade, Scale, Slide)       │
└───────────────────────────┬────────────────────────────┘
                            │ Non-blocking Asynchronous Calls
                            │ (CompletableFuture + Platform.runLater)
                            ▼
┌────────────────────────────────────────────────────────┐
│             HireSmart Java Client Services             │
│            ApiClient & Thread-Safe UserSession         │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP JSON / Multipart REST APIs
                            │ Default Port: 8085
                            ▼
┌────────────────────────────────────────────────────────┐
│              Spring Boot REST Controllers              │
│  AuthController | CandidateRestController | JobController │
│  ResumeRestController | ScreeningRestController | ... │
└───────────────────────────┬────────────────────────────┘
                            │ Service Layer Orchestration
                            ▼
┌────────────────────────────────────────────────────────┐
│             Business & Intelligence Services           │
│   ScreeningService (Multi-Vector ATS Match Algorithm)  │
│   ResumeService (PDFBox + Apache POI Text Extractor)   │
│   ExcelReportService (Apache POI 5-Sheet .xlsx Export) │
│   EmailNotificationService (Jakarta Mail Dispatcher)   │
│   MockInterviewService (Q&A Evaluation Studio)         │
└───────────────────────────┬────────────────────────────┘
                            │ Safe JDBC Prepared Statements
                            ▼
┌────────────────────────────────────────────────────────┐
│            Data Access Object (JDBC DAO)               │
│      UserDao | ResumeDao | JobDao | ApplicationDao     │
│       InterviewDao | NotificationDao | AuditLogDao     │
└───────────────────────────┬────────────────────────────┘
                            │
            ┌───────────────┴──────────────┐
            ▼                              ▼
┌──────────────────────┐        ┌──────────────────────┐
│  MySQL Database 8.x  │   OR   │ Embedded In-Memory DB│
│  (Production Mode)   │        │ (Zero-Config Mode)   │
└──────────────────────┘        └──────────────────────┘
```

---

## 🌟 Key Features & Modules

### Module A: Authentication & Role-Based Access Control (RBAC)
- **Roles**: `Candidate`, `Recruiter`, and `Administrator`.
- **Security**: Passwords securely hashed using BCrypt.
- **Protection**: Backend-enforced role permissions. Public registration restricted to Candidate and Recruiter roles; Admin role cannot be registered publicly.
- **Convenience**: 1-click demo credential fillers on the animated login screen.

### Module B: Candidate Profile Management
- Dynamic profile completion calculation (0–100%) based on phone, location, education, experience, and uploaded resume.
- Candidate skills chip management and experience updates.

### Module C: Resume Upload, Versioning & Parsing
- Supports both **PDF** (`.pdf` via Apache PDFBox) and **DOCX** (`.docx` via Apache POI).
- Automatic text extraction, contact parsing, and skill tokenization.
- Multi-version resume archive per candidate.

### Module D: Algorithmic & AI Resume Screening
- Transparent, multi-vector ATS scoring calculation:
  - Required skills match percentage.
  - Experience level alignment.
  - Educational qualification compatibility.
- Interactive UI chips highlighting **Matched Skills** (Green) and **Missing Skills** (Red).
- Actionable resume improvement recommendations.
- Optional external AI API integration support.

### Module E: Job Requisition Management
- Recruiters can create, publish, edit, and close job postings.
- Filter and search jobs by keyword, title, company, or location.
- 1-click candidate job application submission with duplicate-application safeguards.

### Module F: Application Pipeline Tracking
- Full candidate application lifecycle:
  `APPLIED` ➔ `UNDER REVIEW` ➔ `SHORTLISTED` ➔ `INTERVIEW SCHEDULED` ➔ `SELECTED` / `REJECTED` / `WITHDRAWN`
- Complete timestamped status history audit trail.
- Pipeline management modal for recruiters.

### Module G: Interview Management & Scorecard
- Interview scheduling with date, time, format (Technical, HR, Cultural), and interviewer info.
- Multi-factor evaluation scorecard:
  - Technical Score (0–100)
  - Communication Score (0–100)
  - Problem Solving Score (0–100)
  - Overall Recommendation and Qualitative Recruiter Notes.

### Module H: Notification Center & Email Alerts
- Jakarta Mail integration sending automated notifications for application confirmation, status changes, and interview invitations.
- Safe development fallback: logs notifications to MySQL without throwing runtime errors if SMTP credentials are not configured.

### Module I: Executive Admin Dashboard & Audit Logs
- Live aggregate recruitment KPIs (Total Candidates, Active Jobs, Funnel Breakdown, ATS Screening Metrics).
- Full system audit logging tracking user activities and role actions.

### Module J: Professional Excel Reports (Apache POI)
- One-click export of formatted `.xlsx` workbooks containing real database records:
  1. Full Recruitment Master Workbook (5 sheets with headers and auto-adjusted columns)
  2. Candidates Report
  3. Job Openings Report
  4. Applications Pipeline Report
  5. Screening Results Report
  6. Interview Schedules Report

### Bonus Feature: AI Mock Interview Practice Studio
- Text-based interactive technical and behavioral interview practice for candidates.
- Instant automated answer grading (0–100) and actionable improvement feedback.

---

## 💻 Technology Stack

| Component | Technology | Version |
|---|---|---|
| **Programming Language** | Java (JDK) | 21 or 22 |
| **Desktop UI Framework** | JavaFX / FXML | 21.0.2 |
| **Styling** | Vanilla JavaFX CSS | Custom Modern Design System |
| **Backend Framework** | Spring Boot | 3.2.3 |
| **Database Access** | Spring JDBC (`JdbcTemplate`) | Standard Prepared Statements |
| **Relational Database** | MySQL Connector/J | 8.3.0 |
| **Fallback Database** | Embedded H2 (MySQL Mode) | 2.2.224 |
| **Document Parsing** | Apache PDFBox & Apache POI | 2.0.31 / 5.2.5 |
| **Spreadsheet Engine** | Apache POI (`poi-ooxml`) | 5.2.5 |
| **Email Protocol** | Jakarta Mail | 2.0.2 |
| **Security & Hashing** | Spring Security Crypto (BCrypt) | Latest |
| **Build Tool** | Apache Maven | 3.9+ |

---

## 📋 Prerequisites & System Requirements

1. **Java Development Kit (JDK)**: JDK 21 or JDK 22 installed.
   - Verify with: `java -version`
2. **Apache Maven**: Version 3.9+ installed.
   - Verify with: `mvn -version`
3. **Operating System**: Windows 10/11, macOS, or Linux.
4. *(Optional)* **MySQL Server**: MySQL 8.0+ running on `localhost:3306`.
   - *Note: If MySQL is not running, HireSmart ATS automatically falls back to its embedded in-memory database in MySQL mode so you can test immediately with zero friction!*

---

## 🗄 Database Configuration

### Option A: Automatic Embedded Mode (Zero-Config)
If MySQL is not installed or not running, **you do not need to configure anything**. HireSmart ATS detects this automatically at startup, spins up an embedded in-memory database with full MySQL syntax compatibility, executes `schema.sql`, and pre-seeds sample data.

### Option B: Local MySQL Server (Production)
1. Open MySQL Workbench or your MySQL terminal.
2. Run the initialization script provided in the project root:
   ```sql
   source hiresmart_mysql_schema.sql;
   ```
   *(Or execute the SQL commands in `hiresmart_mysql_schema.sql` directly).*
3. Verify your MySQL credentials in `.env` or `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hiresmart_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```

---

## 🚀 Quick Start & Launch Commands (Windows PowerShell)

Open **PowerShell** in the project directory:
```powershell
cd "c:\Users\ARYA SINGH\Desktop\Project"
```

### 1. Build the Application
To compile all classes, execute tests, and package the standalone fat JAR:
```powershell
mvn clean package -DskipTests
```
*(Or use `C:\Users\ARYA SINGH\apache-maven\apache-maven-3.9.9\bin\mvn.cmd clean package`)*

### 2. Launch Option A: Full Desktop Application (JavaFX UI + Embedded Backend)
To launch the complete animated JavaFX Desktop Application:
```powershell
java -jar target\hiresmart-ats-1.0.0.jar
```
*Or via Maven:*
```powershell
mvn javafx:run
```

### 3. Launch Option B: Dedicated REST API Server Mode
To run the Spring Boot backend as a headless server on port `8085`:
```powershell
java -jar target\hiresmart-ats-1.0.0.jar --server
```

---

## 🔑 Pre-Seeded Demo Accounts

The application automatically seeds standard accounts for instant testing:

| Role | Email | Password | Access Capabilities |
|---|---|---|---|
| **Administrator** | `admin@hiresmart.com` | `admin123` | KPI Analytics, Audit Logs, Excel Export, User Management |
| **Recruiter** | `recruiter@hiresmart.com` | `password123` | Post Jobs, Candidate Pipeline, ATS Screening, Schedule Interviews |
| **Candidate** | `candidate@hiresmart.com` | `password123` | Profile, Upload Resume, Search & Apply Jobs, Mock Interview Studio |

> **Pro Tip**: On the Login screen, click any of the **Quick Demo Fill** buttons to instantly fill credentials for Admin, Recruiter, or Candidate!

---

## 🧪 Automated Testing

The project includes an end-to-end integration and business logic test suite in `src/test/java/com/hiresmart/HireSmartApplicationTests.java`.

To run all automated tests:
```powershell
mvn test
```

### Verified Test Cases:
1. `testUserAuthenticationWithBCrypt()`: Verifies secure password hashing and token generation.
2. `testPublicAdminRegistrationPrevention()`: Ensures unauthorized users cannot register as Admin.
3. `testAlgorithmicResumeScreening()`: Verifies multi-vector match scoring, matched skills, and recommendations.
4. `testPreventDuplicateApplication()`: Verifies that candidates cannot apply twice to the same job opening.
5. `testMockInterviewAnswerGrading()`: Verifies real-time AI mock interview answer scoring.
6. `testApachePoiExcelReportGeneration()`: Verifies generation of formatted `.xlsx` files with multiple sheets.
7. `testDashboardStatisticsCalculation()`: Validates live KPI calculations.

---

## 🌐 REST API Endpoints Reference

All endpoints are hosted at `http://localhost:8085/api/`:

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user & return session |
| `POST` | `/api/auth/register` | Register new candidate or recruiter |
| `GET` | `/api/stats` | Retrieve live recruitment metrics & KPIs |
| `GET` | `/api/jobs` | Retrieve all active job postings |
| `POST` | `/api/jobs` | Create a new job requisition |
| `GET` | `/api/candidates` | Retrieve candidate list |
| `POST` | `/api/resumes/upload` | Upload & parse PDF/DOCX resume |
| `POST` | `/api/screening/screen` | Calculate ATS match score for resume vs job |
| `GET` | `/api/applications` | Retrieve recruitment pipeline applications |
| `POST` | `/api/applications` | Apply for a job opening |
| `PUT` | `/api/applications/{id}/status` | Update candidate status (Shortlist, Hire, Reject) |
| `GET` | `/api/interviews` | List all scheduled interviews |
| `POST` | `/api/interviews/schedule` | Schedule a new interview round |
| `POST` | `/api/interviews/feedback` | Submit interview scorecard & ratings |
| `GET` | `/api/mock-interview/questions` | Get technical questions for practice |
| `POST` | `/api/mock-interview/submit` | Submit candidate answer for automated evaluation |
| `GET` | `/api/reports/excel/all` | Download consolidated 5-sheet Excel report |

---

## 🛠 Troubleshooting & Common FAQs

### Q1: Port 8080 or 8085 is already in use
**Solution**: Port 8080 is often used by Oracle Database (`TNSLSNR`) or other web services. HireSmart ATS uses port `8085` by default. If port 8085 is in use, modify `SERVER_PORT=8086` in `.env` or set `server.port=8086` in `application.properties`.

### Q2: MySQL is not installed on my machine
**Solution**: No installation is required! HireSmart ATS includes an intelligent database abstraction layer. If MySQL is unreachable, it automatically initializes an in-memory database with the full MySQL schema and seeds sample data so you can test all features right away.

### Q3: How do I enable real Gmail SMTP email delivery?
**Solution**: 
1. Enable 2-Factor Authentication in your Google Account.
2. Generate an **App Password** (Google Account > Security > App Passwords).
3. Set in `.env`:
   ```properties
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_actual_email@gmail.com
   SMTP_PASSWORD=your_16_character_app_password
   ```

---

## 👥 Contributors & Academic Credits
- **Project**: AI-Powered Interview & Resume Screening
- **Brand Name**: HireSmart ATS
- **Degree/Program**: MCA / B.Tech Computer Science Project
