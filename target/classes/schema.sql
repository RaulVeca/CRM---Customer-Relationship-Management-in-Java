-- ========================================
-- CRM Training IT - Database Schema
-- ========================================
-- Compatible cu H2 și PostgreSQL

-- Drop tables if exist (pentru development)
DROP TABLE IF EXISTS activities CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS opportunities CASCADE;
DROP TABLE IF EXISTS course_sessions CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS contacts CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ========================================
-- USERS - Utilizatori sistem (sales, trainers)
-- ========================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL, -- ADMIN, SALES, TRAINER, COORDINATOR
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- CONTACTS - Leads B2C și Clienți B2B
-- ========================================
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_type VARCHAR(50) NOT NULL, -- INDIVIDUAL, CORPORATE
    
    -- Date individuale (B2C)
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    birth_date DATE,
    
    -- Date corporate (B2B)
    company_name VARCHAR(255),
    fiscal_code VARCHAR(50),
    registration_number VARCHAR(50),
    industry VARCHAR(100),
    employee_count INT,
    
    -- Date comune
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_county VARCHAR(100),
    address_postal_code VARCHAR(20),
    
    -- Lead management
    lead_source VARCHAR(50), -- WEBSITE, REFERRAL, SOCIAL_MEDIA, EVENT, COLD_CALL
    lead_status VARCHAR(50) NOT NULL DEFAULT 'NEW', -- NEW, CONTACTED, QUALIFIED, CONVERTED, LOST
    lead_score INT DEFAULT 0,
    experience_level VARCHAR(50), -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    learning_goal TEXT,
    
    -- Tracking
    first_contact_date TIMESTAMP,
    last_contact_date TIMESTAMP,
    assigned_to BIGINT,
    
    -- GDPR
    gdpr_consent BOOLEAN DEFAULT FALSE,
    gdpr_consent_date TIMESTAMP,
    marketing_consent BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_contact_assigned_user FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_lead_status ON contacts(lead_status);
CREATE INDEX idx_contacts_lead_score ON contacts(lead_score DESC);
CREATE INDEX idx_contacts_type ON contacts(contact_type);

-- ========================================
-- COURSES
-- ========================================
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- PROGRAMMING, DATABASE, CLOUD, DEVOPS, TESTING, SECURITY
    duration_hours INT NOT NULL,
    level VARCHAR(50), -- BEGINNER, INTERMEDIATE, ADVANCED
    base_price DECIMAL(10,2) NOT NULL,
    prerequisites TEXT,
    objectives TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- COURSE_SESSIONS
-- ========================================
CREATE TABLE course_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    schedule_details TEXT, -- "Luni-Vineri 18:00-21:00"
    location VARCHAR(255), -- "Online" sau "Str. Academiei 14, București"
    delivery_mode VARCHAR(50) NOT NULL, -- ONLINE, ONSITE, HYBRID
    max_participants INT DEFAULT 15,
    current_participants INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PLANNED', -- PLANNED, ACTIVE, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_session_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- ========================================
-- ENROLLMENTS - Înscrieri la cursuri
-- ========================================
CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    session_id BIGINT,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    
    -- Pricing
    original_price DECIMAL(10,2) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    final_price DECIMAL(10,2) NOT NULL,
    
    -- Payment
    payment_status VARCHAR(50) DEFAULT 'UNPAID', -- UNPAID, PARTIAL, PAID, REFUNDED
    amount_paid DECIMAL(10,2) DEFAULT 0,
    
    -- Completion
    completion_date TIMESTAMP,
    grade DECIMAL(5,2), -- 0-10
    certificate_issued BOOLEAN DEFAULT FALSE,
    feedback TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_enrollment_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollment_session FOREIGN KEY (session_id) REFERENCES course_sessions(id)
);

CREATE INDEX idx_enrollments_contact ON enrollments(contact_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_payment ON enrollments(payment_status);

-- ========================================
-- OPPORTUNITIES - Pipeline B2B
-- ========================================
CREATE TABLE opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Pipeline
    stage VARCHAR(50) NOT NULL DEFAULT 'LEAD_QUALIFICATION',
    -- LEAD_QUALIFICATION, NEEDS_ANALYSIS, PROPOSAL_SENT, NEGOTIATION, CONTRACT_REVIEW, WON, LOST
    probability INT DEFAULT 10, -- 0-100
    
    -- Financial
    estimated_value DECIMAL(10,2),
    expected_close_date DATE,
    actual_close_date DATE,
    
    -- Outcome
    won_reason TEXT,
    lost_reason TEXT,
    
    -- Tracking
    assigned_to BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_opportunity_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_opportunity_assigned FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE INDEX idx_opportunities_stage ON opportunities(stage);
CREATE INDEX idx_opportunities_contact ON opportunities(contact_id);

-- ========================================
-- ACTIVITIES - Interacțiuni cu contactele
-- ========================================
CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    opportunity_id BIGINT,
    activity_type VARCHAR(50) NOT NULL, -- CALL, EMAIL, MEETING, DEMO, FOLLOW_UP, NOTE
    subject VARCHAR(255),
    description TEXT,
    scheduled_date TIMESTAMP,
    completed_date TIMESTAMP,
    outcome TEXT,
    next_steps TEXT,
    assigned_to BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_activity_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_activity_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT fk_activity_assigned FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE INDEX idx_activities_contact ON activities(contact_id);
CREATE INDEX idx_activities_date ON activities(scheduled_date);

-- ========================================
-- INVOICES
-- ========================================
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    vat_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, SENT, PAID, OVERDUE, CANCELLED
    payment_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_invoice_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    CONSTRAINT fk_invoice_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

-- ========================================
-- INSERT SAMPLE DATA
-- ========================================

-- Users
INSERT INTO users (username, email, password_hash, first_name, last_name, role) VALUES
('admin', 'admin@trainingit.ro', '$2a$10$HASH', 'Admin', 'System', 'ADMIN'),
('ion.popescu', 'ion.popescu@trainingit.ro', '$2a$10$HASH', 'Ion', 'Popescu', 'SALES'),
('maria.ionescu', 'maria.ionescu@trainingit.ro', '$2a$10$HASH', 'Maria', 'Ionescu', 'COORDINATOR');

-- Courses
INSERT INTO courses (code, title, description, category, duration_hours, level, base_price) VALUES
('JAVA-101', 'Java Fundamentals', 'Curs introductiv în programarea Java', 'PROGRAMMING', 40, 'BEGINNER', 1200.00),
('JAVA-201', 'Spring Framework', 'Dezvoltare aplicații enterprise cu Spring', 'PROGRAMMING', 60, 'INTERMEDIATE', 1800.00),
('DB-101', 'SQL și Baze de Date', 'Fundamente SQL, PostgreSQL, MySQL', 'DATABASE', 32, 'BEGINNER', 900.00),
('CLOUD-101', 'AWS Fundamentals', 'Introducere în Amazon Web Services', 'CLOUD', 40, 'INTERMEDIATE', 1500.00),
('PYTHON-101', 'Python Programming', 'Python pentru începători', 'PROGRAMMING', 40, 'BEGINNER', 1100.00);

-- Sample Contacts
INSERT INTO contacts (contact_type, first_name, last_name, email, phone, lead_source, lead_status, lead_score, experience_level, learning_goal, gdpr_consent, first_contact_date) VALUES
('INDIVIDUAL', 'Alexandru', 'Marinescu', 'alex.marinescu@example.com', '0731111111', 'WEBSITE', 'QUALIFIED', 75, 'BEGINNER', 'Reconversie profesională în IT', TRUE, CURRENT_TIMESTAMP),
('INDIVIDUAL', 'Diana', 'Popescu', 'diana.popescu@example.com', '0732222222', 'REFERRAL', 'CONTACTED', 45, 'BEGINNER', 'Învățare Python pentru analiză date', TRUE, CURRENT_TIMESTAMP),
('CORPORATE', NULL, NULL, 'contact@techcorp.ro', '0213334444', 'EVENT', 'NEW', 85, NULL, NULL, TRUE, CURRENT_TIMESTAMP);

UPDATE contacts SET company_name='TechCorp SRL', fiscal_code='RO12345678', industry='IT Software', employee_count=250 WHERE email='contact@techcorp.ro';

-- Sample Course Session
INSERT INTO course_sessions (course_id, start_date, end_date, schedule_details, delivery_mode, max_participants) VALUES
(1, DATE_ADD(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'Luni-Vineri 18:00-21:00', 'ONLINE', 15);

-- Sample Enrollment
INSERT INTO enrollments (contact_id, course_id, session_id, original_price, final_price, payment_status) VALUES
(1, 1, 1, 1200.00, 1200.00, 'UNPAID');

-- Sample Opportunity
INSERT INTO opportunities (contact_id, title, stage, estimated_value, probability, assigned_to) VALUES
(3, 'Training corporativ Java 20 angajați', 'NEEDS_ANALYSIS', 24000.00, 50, 2);

-- Sample Activity
INSERT INTO activities (contact_id, activity_type, subject, description, scheduled_date, assigned_to) VALUES
(1, 'CALL', 'Follow-up pentru înscriere', 'Discuție detalii curs și modalități plată', DATE_ADD(NOW(), INTERVAL 2 DAY), 2),
(3, 'MEETING', 'Întâlnire needs assessment', 'Întâlnire cu HR Manager pentru analiza nevoilor', DATE_ADD(NOW(), INTERVAL 5 DAY), 2);

-- ========================================
-- VIEWS - Pentru raportare
-- ========================================

CREATE OR REPLACE VIEW v_hot_leads AS
SELECT c.*, u.first_name as assigned_first_name, u.last_name as assigned_last_name
FROM contacts c
LEFT JOIN users u ON c.assigned_to = u.id
WHERE c.lead_score >= 70 AND c.lead_status NOT IN ('CONVERTED', 'LOST');

CREATE OR REPLACE VIEW v_active_pipeline AS
SELECT o.*, c.company_name, c.email, c.phone, u.first_name as owner_first_name, u.last_name as owner_last_name
FROM opportunities o
JOIN contacts c ON o.contact_id = c.id
LEFT JOIN users u ON o.assigned_to = u.id
WHERE o.stage NOT IN ('WON', 'LOST')
ORDER BY o.probability DESC, o.estimated_value DESC;

-- ========================================
-- COMMIT
-- ========================================
COMMIT;
