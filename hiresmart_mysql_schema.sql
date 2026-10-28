-- ====================================================================
-- HireSmart ATS - Standalone MySQL Database Schema & Setup Script
-- Database: hiresmart_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS hiresmart_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hiresmart_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL, -- 'CANDIDATE', 'RECRUITER', 'ADMIN'
    phone VARCHAR(30),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. Candidate Profiles
CREATE TABLE IF NOT EXISTS candidate_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    headline VARCHAR(255),
    location VARCHAR(150),
    qualification VARCHAR(255),
    skills TEXT,
    experience_years INT DEFAULT 0,
    experience_summary TEXT,
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    profile_completion INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_cand_location (location)
) ENGINE=InnoDB;

-- 3. Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL, -- 'PDF', 'DOCX'
    file_size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    version_number INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    parsed_text LONGTEXT,
    extracted_skills TEXT,
    extracted_education TEXT,
    extracted_experience TEXT,
    extracted_projects TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    INDEX idx_resume_candidate (candidate_id)
) ENGINE=InnoDB;

-- 4. Jobs Table
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recruiter_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    department VARCHAR(100),
    location VARCHAR(150) NOT NULL,
    job_type VARCHAR(50) DEFAULT 'Full-time',
    experience_required INT DEFAULT 0,
    qualification_required VARCHAR(255),
    required_skills TEXT NOT NULL,
    description TEXT NOT NULL,
    application_deadline DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- 'ACTIVE', 'CLOSED', 'DRAFT'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_recruiter FOREIGN KEY (recruiter_id) REFERENCES users(id),
    INDEX idx_job_status (status),
    INDEX idx_job_title (title)
) ENGINE=InnoDB;

-- 5. Screening Results Table
CREATE TABLE IF NOT EXISTS screening_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    overall_match_score INT NOT NULL,
    ats_compatibility_score INT NOT NULL,
    skill_match_score INT NOT NULL,
    experience_match_score INT NOT NULL,
    matched_skills TEXT,
    missing_skills TEXT,
    recommendations TEXT,
    engine_type VARCHAR(50) DEFAULT 'BASELINE_ALGORITHMIC',
    screened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_screening_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    CONSTRAINT fk_screening_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_screening_score (overall_match_score)
) ENGINE=InnoDB;

-- 6. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'APPLIED', -- 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'SELECTED', 'REJECTED', 'WITHDRAWN'
    cover_letter TEXT,
    recruiter_notes TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id),
    CONSTRAINT fk_app_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    INDEX idx_app_status (status)
) ENGINE=InnoDB;

-- 7. Application Status History Table
CREATE TABLE IF NOT EXISTS application_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    change_reason VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Interviews Table
CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    round_type VARCHAR(50) DEFAULT 'TECHNICAL', -- 'TECHNICAL', 'HR', 'MANAGERIAL', 'AI_MOCK'
    scheduled_time DATETIME NOT NULL,
    interviewer_name VARCHAR(150) NOT NULL,
    interviewer_email VARCHAR(150),
    meeting_link VARCHAR(500),
    status VARCHAR(50) DEFAULT 'SCHEDULED', -- 'SCHEDULED', 'COMPLETED', 'RESCHEDULED', 'CANCELLED'
    technical_score INT DEFAULT 0,
    communication_score INT DEFAULT 0,
    problem_solving_score INT DEFAULT 0,
    feedback_notes TEXT,
    recommendation VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(id),
    CONSTRAINT fk_interview_job FOREIGN KEY (job_id) REFERENCES jobs(id)
) ENGINE=InnoDB;

-- 9. Mock Interview Q&A
CREATE TABLE IF NOT EXISTS mock_interview_qa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT,
    question_text TEXT NOT NULL,
    category VARCHAR(50) DEFAULT 'TECHNICAL',
    candidate_answer TEXT,
    ai_feedback TEXT,
    score INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mock_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    recipient_name VARCHAR(150),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'SENT', -- 'SENT', 'QUEUED', 'FAILED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 11. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
