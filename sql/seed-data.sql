-- =====================================================
-- Seed Data - CRM Training IT
-- =====================================================
USE crm_training;

-- Utilizatori
INSERT INTO users (username, email, password_hash, first_name, last_name, role, active) VALUES
('admin', 'admin@trainingit.ro', '$2a$10$abcdefghijklmnop', 'Admin', 'Principal', 'ADMIN', TRUE),
('sales1', 'maria.popescu@trainingit.ro', '$2a$10$abcdefghijklmnop', 'Maria', 'Popescu', 'SALES_AGENT', TRUE),
('sales2', 'ion.ionescu@trainingit.ro', '$2a$10$abcdefghijklmnop', 'Ion', 'Ionescu', 'SALES_AGENT', TRUE),
('manager', 'dana.manager@trainingit.ro', '$2a$10$abcdefghijklmnop', 'Dana', 'Manager', 'SALES_MANAGER', TRUE);

-- Cursuri
INSERT INTO courses (code, name, description, category, level, duration_hours, 
                     price_individual, price_group, price_corporate_per_day, 
                     min_participants, max_participants, active) VALUES
('JAVA-101', 'Java Fundamentals', 'Curs introductiv în Java', 'PROGRAMMING', 'BEGINNER', 60, 
 1500.00, 1200.00, 2500.00, 5, 15, TRUE),
('JAVA-ADV', 'Java Advanced & Spring', 'Curs avansat Java cu Spring Framework', 'PROGRAMMING', 'ADVANCED', 80, 
 2500.00, 2000.00, 3500.00, 5, 12, TRUE),
('PYTHON-101', 'Python Fundamentals', 'Curs introductiv în Python', 'PROGRAMMING', 'BEGINNER', 50, 
 1300.00, 1100.00, 2200.00, 5, 15, TRUE),
('AI-INTRO', 'AI și Machine Learning', 'Introducere în AI și ML cu Python', 'AI', 'INTERMEDIATE', 70, 
 2200.00, 1800.00, 3000.00, 5, 12, TRUE),
('RECONV-IT', 'Reconversie Profesională IT', 'Program complet de reconversie 6 luni', 'PROFESSIONAL_RECONVERSION', 'BEGINNER', 480, 
 6000.00, 5000.00, NULL, 10, 20, TRUE),
('WS-DOCKER', 'Workshop Docker & Kubernetes', 'Workshop intensiv 2 zile', 'WORKSHOP', 'INTERMEDIATE', 16, 
 800.00, 700.00, 1500.00, 5, 15, TRUE);

-- Admini (login "Cont admin") - traineri cu adrese @adminit.ro
INSERT INTO admins (first_name, last_name, email) VALUES
('Andrei', 'Birceanu', 'andreibirceanu@adminit.ro'),
('Costache', 'Măzărescu', 'costachemazarescu@adminit.ro'),
('Oana', 'Badache', 'oanabadache@adminit.ro');

-- Traineri (livrează cursurile) - adrese @trainerit.ro
INSERT INTO trainers (first_name, last_name, email) VALUES
('Andrei', 'Birceanu', 'andreibirceanu@trainerit.ro'),
('Sorin', 'Dima', 'sorindima@trainerit.ro'),
('Claudiu', 'Antonescu', 'claudiuantonescu@trainerit.ro');

-- Contacte exemplu B2C
INSERT INTO contacts (contact_type, first_name, last_name, email, phone, lead_source, 
                      lead_status, lead_score, experience_level, learning_goal, 
                      assigned_to, gdpr_consent, first_contact_date) VALUES
('INDIVIDUAL', 'Alexandru', 'Stoica', 'alex.stoica@example.com', '0722333444', 'WEBSITE', 
 'INTERESTED', 65, 'BEGINNER', 'Reconversie profesională în IT', 2, TRUE, NOW()),
('INDIVIDUAL', 'Cristina', 'Marin', 'cristina.marin@example.com', '0722333555', 'REFERRAL', 
 'QUALIFIED', 80, 'INTERMEDIATE', 'Avansare carieră', 2, TRUE, NOW()),
('INDIVIDUAL', 'Bogdan', 'Tudor', 'bogdan.tudor@example.com', '0722333666', 'FACEBOOK', 
 'NEW', 35, 'BEGINNER', 'Hobby', 3, TRUE, NOW());

-- Contact corporate
INSERT INTO contacts (contact_type, company_name, email, phone, fiscal_code, 
                      registration_number, industry, employee_count, lead_source, 
                      lead_status, lead_score, assigned_to, gdpr_consent, first_contact_date) VALUES
('CORPORATE', 'BankTech Solutions SRL', 'hr@banktech.ro', '0212223344', 'RO12345678', 
 'J40/1234/2020', 'IT & Software', 150, 'REFERRAL', 'INTERESTED', 85, 4, TRUE, NOW()),
('CORPORATE', 'Health Innovations SA', 'training@healthinno.ro', '0212223355', 'RO87654321', 
 'J40/5678/2018', 'Healthcare', 500, 'WEBSITE', 'NEW', 55, 4, TRUE, NOW());

-- Sesiuni curs
INSERT INTO course_sessions (course_id, session_code, start_date, end_date,
                              schedule_description, total_hours, delivery_mode,
                              location, max_participants, status) VALUES
(1, 'JAVA-101-2026-01', '2026-06-01', '2026-07-15', 'L,M,J 18:00-21:00', 60, 'HYBRID',
 'București - Sala 1', 15, 'OPEN_ENROLLMENT'),
(3, 'PYTHON-101-2026-01', '2026-06-15', '2026-07-30', 'M,J 18:00-21:00', 50, 'ONLINE',
 NULL, 15, 'OPEN_ENROLLMENT'),
(4, 'AI-INTRO-2026-01', '2026-07-01', '2026-08-15', 'L,M,V 18:00-21:00', 70, 'ONLINE',
 NULL, 12, 'PLANNED');

-- Oportunitate B2B exemplu
INSERT INTO opportunities (client_id, title, description, estimated_participants, 
                           estimated_value, quoted_value, probability_percent, stage, 
                           expected_close_date, assigned_to, delivery_mode) VALUES
(4, 'Training Java Spring pentru echipa Dev', 'Companie bancară dorește training intensiv', 
 25, 35000.00, 32000.00, 60, 'PROPOSAL_SENT', '2026-08-01', 4, 'ON_SITE'),
(5, 'Training Python & AI pentru echipa Data', 'Departament Data Science', 
 15, 22000.00, NULL, 40, 'NEEDS_ANALYSIS', '2026-09-01', 4, 'HYBRID');

-- Activități exemplu
INSERT INTO activities (activity_type, contact_id, opportunity_id, subject, 
                        description, scheduled_date, assigned_to, status, priority) VALUES
('CALL', 1, NULL, 'Apel inițial - Alexandru', 'Discuție despre nevoile de training', 
 DATE_ADD(NOW(), INTERVAL 2 DAY), 2, 'SCHEDULED', 'HIGH'),
('EMAIL', 2, NULL, 'Trimitere ofertă cursuri', 'Ofertă personalizată după consultare', 
 NOW(), 2, 'COMPLETED', 'MEDIUM'),
('MEETING', 4, 1, 'Întâlnire BankTech - prezentare ofertă', 'Prezentare detaliată ofertă', 
 DATE_ADD(NOW(), INTERVAL 5 DAY), 4, 'SCHEDULED', 'HIGH');
