-- =====================================================
-- CRM Training IT - Schema MySQL
-- =====================================================

CREATE DATABASE IF NOT EXISTS crm_training 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE crm_training;

-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- =====================================================
-- CONTACTS (B2C + B2B)
-- =====================================================
CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contact_type VARCHAR(20) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    company_name VARCHAR(200),
    fiscal_code VARCHAR(50),
    registration_number VARCHAR(50),
    industry VARCHAR(100),
    employee_count INT,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    birth_date DATE,
    address_street VARCHAR(200),
    address_city VARCHAR(100),
    address_county VARCHAR(100),
    address_postal_code VARCHAR(10),
    lead_source VARCHAR(50),
    lead_status VARCHAR(50) DEFAULT 'NEW',
    lead_score INT DEFAULT 0,
    experience_level VARCHAR(20),
    learning_goal VARCHAR(100),
    first_contact_date TIMESTAMP NULL,
    last_contact_date TIMESTAMP NULL,
    assigned_to BIGINT,
    gdpr_consent BOOLEAN DEFAULT FALSE,
    gdpr_consent_date TIMESTAMP NULL,
    marketing_consent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_lead_status (lead_status),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_contact_type (contact_type),
    FOREIGN KEY (assigned_to) REFERENCES users(id)
) ENGINE=InnoDB;

-- =====================================================
-- COURSES
-- =====================================================
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    syllabus TEXT,
    category VARCHAR(50),
    level VARCHAR(20),
    prerequisites TEXT,
    duration_hours INT NOT NULL,
    min_participants INT DEFAULT 3,
    max_participants INT DEFAULT 15,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_category (category)
) ENGINE=InnoDB;

-- =====================================================
-- ADMINS (login pentru opțiunea "Cont admin")
-- Trainerii care pot accesa zona de administrare. Autentificare doar pe email.
-- =====================================================
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- TRAINERS (trainerii care livrează cursurile)
-- Doar prenume, nume și email.
-- =====================================================
CREATE TABLE trainers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- MEDITATION SESSIONS (ședințe online cu un trainer)
-- Un contact rezervă ore consecutive (08:00-20:00) într-o zi.
-- =====================================================
CREATE TABLE meditation_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trainer_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    contact_email VARCHAR(255),
    session_date DATE NOT NULL,
    start_hour INT NOT NULL,
    end_hour INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_med_sessions_trainer_date (trainer_id, session_date),
    CONSTRAINT fk_med_sessions_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id)
) ENGINE=InnoDB;

-- =====================================================
-- COURSE SESSIONS
-- =====================================================
CREATE TABLE course_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    session_code VARCHAR(50) UNIQUE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    schedule_description VARCHAR(500),
    total_hours INT NOT NULL,
    delivery_mode VARCHAR(20),
    location VARCHAR(200),
    meeting_link VARCHAR(500),
    max_participants INT,
    current_participants INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PLANNED',
    average_rating DECIMAL(3,2),
    is_corporate BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    INDEX idx_start_date (start_date),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- ENROLLMENTS
-- =====================================================
CREATE TABLE enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    attended_sessions INT DEFAULT 0,
    attendance_rate DECIMAL(5,2),
    exam_passed BOOLEAN,
    final_grade DECIMAL(5,2),
    certificate_issued BOOLEAN DEFAULT FALSE,
    certificate_number VARCHAR(50),
    rating INT,
    feedback TEXT,
    notes TEXT,
    FOREIGN KEY (session_id) REFERENCES course_sessions(id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    UNIQUE KEY uk_enrollment (session_id, contact_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- OPPORTUNITIES (B2B)
-- =====================================================
CREATE TABLE opportunities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    estimated_participants INT,
    custom_requirements TEXT,
    delivery_mode VARCHAR(20),
    preferred_location VARCHAR(200),
    desired_start_date DATE,
    estimated_value DECIMAL(12,2),
    quoted_value DECIMAL(12,2),
    probability_percent INT DEFAULT 50,
    stage VARCHAR(50) DEFAULT 'LEAD_QUALIFICATION',
    expected_close_date DATE,
    actual_close_date DATE,
    assigned_to BIGINT,
    competitors VARCHAR(500),
    lost_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES contacts(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    INDEX idx_stage (stage)
) ENGINE=InnoDB;

-- =====================================================
-- PROPOSALS
-- =====================================================
CREATE TABLE proposals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    opportunity_id BIGINT NOT NULL,
    proposal_number VARCHAR(50) UNIQUE NOT NULL,
    version INT DEFAULT 1,
    executive_summary TEXT,
    delivery_plan TEXT,
    payment_terms TEXT,
    subtotal DECIMAL(12,2),
    discount DECIMAL(12,2) DEFAULT 0,
    tax DECIMAL(12,2),
    total DECIMAL(12,2),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    sent_date TIMESTAMP NULL,
    response_date TIMESTAMP NULL,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- =====================================================
-- CONTRACTS
-- =====================================================
CREATE TABLE contracts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    opportunity_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    contract_number VARCHAR(50) UNIQUE NOT NULL,
    sign_date DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    total_value DECIMAL(12,2) NOT NULL,
    payment_terms VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    FOREIGN KEY (client_id) REFERENCES contacts(id)
) ENGINE=InnoDB;

-- =====================================================
-- INVOICES
-- =====================================================
CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    contract_id BIGINT,
    enrollment_id BIGINT,
    client_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    tax DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    payment_date DATE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contracts(id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    FOREIGN KEY (client_id) REFERENCES contacts(id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB;

-- =====================================================
-- PAYMENTS
-- =====================================================
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT,
    enrollment_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_date DATE NOT NULL,
    transaction_reference VARCHAR(100),
    status VARCHAR(20) DEFAULT 'COMPLETED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- =====================================================
-- ACTIVITIES
-- =====================================================
CREATE TABLE activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_type VARCHAR(20) NOT NULL,
    contact_id BIGINT,
    opportunity_id BIGINT,
    subject VARCHAR(200) NOT NULL,
    description TEXT,
    scheduled_date TIMESTAMP NULL,
    completed_date TIMESTAMP NULL,
    duration_minutes INT,
    assigned_to BIGINT,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    outcome TEXT,
    next_steps TEXT,
    requires_followup BOOLEAN DEFAULT FALSE,
    followup_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_scheduled_date (scheduled_date)
) ENGINE=InnoDB;

-- =====================================================
-- LEAD SCORE LOG
-- =====================================================
CREATE TABLE lead_score_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contact_id BIGINT NOT NULL,
    score_change INT NOT NULL,
    reason VARCHAR(200),
    previous_score INT,
    new_score INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
    INDEX idx_contact_id (contact_id)
) ENGINE=InnoDB;

-- =====================================================
-- AUDIT LOG
-- =====================================================
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_values TEXT,
    new_values TEXT,
    user_id BIGINT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_entity (entity_type, entity_id)
) ENGINE=InnoDB;
