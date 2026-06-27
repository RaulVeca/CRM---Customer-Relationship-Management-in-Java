-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: 127.0.0.1    Database: crm_training
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activities`
--

DROP TABLE IF EXISTS `activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_type` varchar(20) NOT NULL,
  `contact_id` bigint(20) DEFAULT NULL,
  `opportunity_id` bigint(20) DEFAULT NULL,
  `subject` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `scheduled_date` timestamp NULL DEFAULT NULL,
  `completed_date` timestamp NULL DEFAULT NULL,
  `duration_minutes` int(11) DEFAULT NULL,
  `assigned_to` bigint(20) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'SCHEDULED',
  `priority` varchar(20) DEFAULT 'MEDIUM',
  `outcome` text DEFAULT NULL,
  `next_steps` text DEFAULT NULL,
  `requires_followup` tinyint(1) DEFAULT 0,
  `followup_date` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `contact_id` (`contact_id`),
  KEY `opportunity_id` (`opportunity_id`),
  KEY `assigned_to` (`assigned_to`),
  KEY `idx_activity_type` (`activity_type`),
  KEY `idx_scheduled_date` (`scheduled_date`),
  CONSTRAINT `activities_ibfk_1` FOREIGN KEY (`contact_id`) REFERENCES `contacts` (`id`),
  CONSTRAINT `activities_ibfk_2` FOREIGN KEY (`opportunity_id`) REFERENCES `opportunities` (`id`),
  CONSTRAINT `activities_ibfk_3` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activities`
--

LOCK TABLES `activities` WRITE;
/*!40000 ALTER TABLE `activities` DISABLE KEYS */;
INSERT INTO `activities` VALUES (1,'CALL',1,NULL,'Apel inițial - Alexandru','Discuție despre nevoile de training','2026-06-05 16:06:22',NULL,NULL,2,'SCHEDULED','HIGH',NULL,NULL,0,NULL,'2026-06-03 16:06:22',NULL),(2,'EMAIL',2,NULL,'Trimitere ofertă cursuri','Ofertă personalizată după consultare','2026-06-03 16:06:22',NULL,NULL,2,'COMPLETED','MEDIUM',NULL,NULL,0,NULL,'2026-06-03 16:06:22',NULL),(3,'MEETING',4,1,'Întâlnire BankTech - prezentare ofertă','Prezentare detaliată ofertă','2026-06-08 16:06:22',NULL,NULL,4,'SCHEDULED','HIGH',NULL,NULL,0,NULL,'2026-06-03 16:06:22',NULL),(4,'EMAIL',7,NULL,'Java Introduction','Email manual','2026-06-13 00:02:40',NULL,NULL,NULL,'SCHEDULED','MEDIUM',NULL,NULL,0,NULL,'2026-06-13 00:02:40',1),(5,'CALL',8,NULL,'Consultare inițială',NULL,'2026-06-14 00:08:03','2026-06-13 00:08:03',30,1,'COMPLETED','HIGH','Persoana este interesată','Trimite ofertă',0,NULL,'2026-06-13 00:08:03',NULL),(6,'CALL',12,NULL,'Consultare inițială',NULL,'2026-06-14 11:21:31','2026-06-13 11:21:31',30,1,'COMPLETED','HIGH','Persoana este interesată','Trimite ofertă',0,NULL,'2026-06-13 11:21:31',NULL),(7,'MEETING',7,NULL,'MEETING',NULL,'2026-06-14 11:24:52',NULL,60,1,'SCHEDULED','HIGH',NULL,NULL,0,NULL,'2026-06-13 11:24:52',NULL),(8,'MEETING',6,NULL,'JAVA-ADV-MEET',NULL,'2026-06-19 08:17:36','2026-06-24 14:04:28',60,1,'COMPLETED','HIGH','GOOD','',0,NULL,'2026-06-18 08:17:36',NULL),(9,'CALL',14,NULL,'Consultare inițială',NULL,'2026-06-19 08:53:23','2026-06-18 08:53:23',30,1,'COMPLETED','HIGH','Persoana este interesată','Trimite ofertă',0,NULL,'2026-06-18 08:53:23',NULL);
/*!40000 ALTER TABLE `activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `agent_log`
--

DROP TABLE IF EXISTS `agent_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agent_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tip_agent` enum('COMUNICARE_CLIENT','FINANCIAR') NOT NULL,
  `client_id` int(11) DEFAULT NULL,
  `prompt_text` text DEFAULT NULL,
  `raspuns_text` text DEFAULT NULL,
  `data_executie` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_agentlog_client` (`client_id`),
  CONSTRAINT `fk_agentlog_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agent_log`
--

LOCK TABLES `agent_log` WRITE;
/*!40000 ALTER TABLE `agent_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `agent_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auctions`
--

DROP TABLE IF EXISTS `auctions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auctions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `starting_price` decimal(12,2) NOT NULL DEFAULT 0.00,
  `status` varchar(30) NOT NULL DEFAULT 'OPEN',
  `closes_at` timestamp NULL DEFAULT NULL,
  `winner_company_id` bigint(20) DEFAULT NULL,
  `winning_amount` decimal(12,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_auctions_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auctions`
--

LOCK TABLES `auctions` WRITE;
/*!40000 ALTER TABLE `auctions` DISABLE KEYS */;
INSERT INTO `auctions` VALUES (1,2,'Java Advanced - corporate cohort Q3','Exclusive corporate delivery slot',20000.00,'AWARDED',NULL,4,25000.00,'2026-06-24 21:04:00','2026-06-24 21:04:01'),(2,NULL,'New Auction',NULL,1000.00,'OPEN',NULL,NULL,NULL,'2026-06-24 21:27:42','2026-06-24 21:27:42');
/*!40000 ALTER TABLE `auctions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audit_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_type` varchar(50) NOT NULL,
  `entity_id` bigint(20) NOT NULL,
  `action` varchar(20) NOT NULL,
  `old_values` text DEFAULT NULL,
  `new_values` text DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `idx_entity` (`entity_type`,`entity_id`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,'ContactService',6,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@37d31475, contactType=INDIVIDUAL, firstName=Mihai, lastName=Radulescu, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=mihai.radulescu04@e-uvt.ro, phone=+40 768737531, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=NEW, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională, firstContactDate=2026-06-03T19:09:32.998, lastContactDate=2026-06-03T19:09:33.033, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-03T19:09:33.009, marketingConsent=false)',NULL,NULL,'2026-06-03 16:09:33'),(2,'ContactService',7,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@365405a1, contactType=INDIVIDUAL, firstName=Alexandru, lastName=Balasa, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=alexandru.balasa@gmail.com, phone=+40 737 122 911, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=NEW, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională, firstContactDate=2026-06-06T13:34:24.370, lastContactDate=2026-06-06T13:34:24.463, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-06T13:34:24.371, marketingConsent=false)',NULL,NULL,'2026-06-06 10:34:24'),(3,'ContactService',8,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@6f195bc3, contactType=INDIVIDUAL, firstName=Maria, lastName=Demo, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=maria.demo.1781309282984@example.com, phone=0712345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=NEW, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională în IT, firstContactDate=2026-06-13T03:08:02.984, lastContactDate=2026-06-13T03:08:02.991, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T03:08:02.984, marketingConsent=true)',NULL,NULL,'2026-06-13 00:08:03'),(4,'ActivityService',5,'ACTIVITY_COMPLETED',NULL,'Activity(super=crm.model.entity.Activity@3d921e20, activityType=CALL, contactId=8, opportunityId=null, subject=Consultare inițială, description=null, scheduledDate=2026-06-14T03:08:03, completedDate=2026-06-13T03:08:03.034, durationMinutes=30, assignedTo=1, status=COMPLETED, priority=HIGH, outcome=Persoana este interesată, nextSteps=Trimite ofertă, requiresFollowup=false, followupDate=null, createdBy=null)',NULL,NULL,'2026-06-13 00:08:03'),(5,'ContactService',8,'LEAD_STATUS_CHANGED',NULL,'Contact(super=crm.model.entity.Contact@6f195bc3, contactType=INDIVIDUAL, firstName=Maria, lastName=Demo, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=maria.demo.1781309282984@example.com, phone=0712345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=INTERESTED, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională în IT, firstContactDate=2026-06-13T03:08:02, lastContactDate=2026-06-13T03:08:03.046, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T03:08:02, marketingConsent=true)',NULL,NULL,'2026-06-13 00:08:03'),(6,'ContactService',9,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@6f195bc3, contactType=CORPORATE, firstName=null, lastName=null, birthDate=null, companyName=TechCorp SRL Demo, fiscalCode=RO12345678, registrationNumber=J40/1234/2020, industry=IT & Software, employeeCount=150, email=contact.demo.1781309283051@techcorp.ro, phone=0212345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=REFERRAL, leadStatus=NEW, leadScore=85, experienceLevel=null, learningGoal=null, firstContactDate=2026-06-13T03:08:03.051, lastContactDate=2026-06-13T03:08:03.052, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T03:08:03.051, marketingConsent=false)',NULL,NULL,'2026-06-13 00:08:03'),(7,'OpportunityService',4,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@1a1d6a08, clientId=9, title=Training Java pentru echipa dev, description=null, estimatedParticipants=20, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=25000.00, quotedValue=null, probabilityPercent=50, stage=PROPOSAL_SENT, expectedCloseDate=2026-07-13, actualCloseDate=null, assignedTo=null, competitors=null, lostReason=null)',NULL,NULL,'2026-06-13 00:08:03'),(8,'ContactService',10,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@7d70d1b1, contactType=CORPORATE, firstName=null, lastName=null, birthDate=null, companyName=Sensidev, fiscalCode=44556, registrationNumber=null, industry=IT, employeeCount=9, email=sebastian.kalciov@gmail.com, phone=null, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=REFERRAL, leadStatus=NEW, leadScore=55, experienceLevel=null, learningGoal=null, firstContactDate=2026-06-13T13:19:09.600, lastContactDate=2026-06-13T13:19:09.631, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T13:19:09.608, marketingConsent=false)',NULL,NULL,'2026-06-13 10:19:09'),(9,'ContactService',11,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@1142dc9e, contactType=CORPORATE, firstName=null, lastName=null, birthDate=null, companyName=Companie7, fiscalCode=226655, registrationNumber=null, industry=ML, employeeCount=4, email=companie7@gmail.com, phone=null, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=REFERRAL, leadStatus=NEW, leadScore=45, experienceLevel=null, learningGoal=null, firstContactDate=2026-06-13T13:30:59.832, lastContactDate=2026-06-13T13:30:59.858, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T13:30:59.837, marketingConsent=false)',NULL,NULL,'2026-06-13 10:30:59'),(10,'ContactService',12,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@80503, contactType=INDIVIDUAL, firstName=Maria, lastName=Demo, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=maria.demo.1781349691234@example.com, phone=0712345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=NEW, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională în IT, firstContactDate=2026-06-13T14:21:31.232, lastContactDate=2026-06-13T14:21:31.264, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T14:21:31.238, marketingConsent=true)',NULL,NULL,'2026-06-13 11:21:31'),(11,'ActivityService',6,'ACTIVITY_COMPLETED',NULL,'Activity(super=crm.model.entity.Activity@7d70d1b1, activityType=CALL, contactId=12, opportunityId=null, subject=Consultare inițială, description=null, scheduledDate=2026-06-14T14:21:31, completedDate=2026-06-13T14:21:31.317, durationMinutes=30, assignedTo=1, status=COMPLETED, priority=HIGH, outcome=Persoana este interesată, nextSteps=Trimite ofertă, requiresFollowup=false, followupDate=null, createdBy=null)',NULL,NULL,'2026-06-13 11:21:31'),(12,'ContactService',12,'LEAD_STATUS_CHANGED',NULL,'Contact(super=crm.model.entity.Contact@80503, contactType=INDIVIDUAL, firstName=Maria, lastName=Demo, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=maria.demo.1781349691234@example.com, phone=0712345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=INTERESTED, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională în IT, firstContactDate=2026-06-13T14:21:31, lastContactDate=2026-06-13T14:21:31.335, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T14:21:31, marketingConsent=true)',NULL,NULL,'2026-06-13 11:21:31'),(13,'ContactService',13,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@80503, contactType=CORPORATE, firstName=null, lastName=null, birthDate=null, companyName=TechCorp SRL Demo, fiscalCode=RO12345678, registrationNumber=J40/1234/2020, industry=IT & Software, employeeCount=150, email=contact.demo.1781349691338@techcorp.ro, phone=0212345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=REFERRAL, leadStatus=NEW, leadScore=85, experienceLevel=null, learningGoal=null, firstContactDate=2026-06-13T14:21:31.338, lastContactDate=2026-06-13T14:21:31.339, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-13T14:21:31.338, marketingConsent=false)',NULL,NULL,'2026-06-13 11:21:31'),(14,'OpportunityService',5,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@2353b3e6, clientId=13, title=Training Java pentru echipa dev, description=null, estimatedParticipants=20, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=25000.00, quotedValue=null, probabilityPercent=50, stage=PROPOSAL_SENT, expectedCloseDate=2026-07-13, actualCloseDate=null, assignedTo=null, competitors=null, lostReason=null)',NULL,NULL,'2026-06-13 11:21:31'),(15,'ContactService',7,'LEAD_STATUS_CHANGED',NULL,'Contact(super=crm.model.entity.Contact@4c5e540, contactType=INDIVIDUAL, firstName=Alexandru, lastName=Balasa, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=alexandru.balasa@gmail.com, phone=+40 737 122 911, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=QUALIFIED, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională, firstContactDate=2026-06-06T13:34:24, lastContactDate=2026-06-13T14:24:13.422, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-06T13:34:24, marketingConsent=false)',NULL,NULL,'2026-06-13 11:24:13'),(16,'ContactService',6,'LEAD_STATUS_CHANGED',NULL,'Contact(super=crm.model.entity.Contact@3043fe0e, contactType=INDIVIDUAL, firstName=Mihai, lastName=Radulescu, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=mihai.radulescu04@e-uvt.ro, phone=+40 768737531, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=INTERESTED, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională, firstContactDate=2026-06-03T19:09:32, lastContactDate=2026-06-18T11:14:30.475, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-03T19:09:33, marketingConsent=false)',NULL,NULL,'2026-06-18 08:14:30'),(17,'OpportunityService',3,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@2a742aa2, clientId=3, title=#1 Opportunity, description=null, estimatedParticipants=5, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=1000.00, quotedValue=null, probabilityPercent=70, stage=NEGOTIATION, expectedCloseDate=2026-08-13, actualCloseDate=null, assignedTo=null, competitors=null, lostReason=null)',NULL,NULL,'2026-06-18 08:23:54'),(18,'ContactService',14,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@467aecef, contactType=INDIVIDUAL, firstName=Maria, lastName=Demo, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=maria.demo.1781772803790@example.com, phone=0712345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=NEW, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională în IT, firstContactDate=2026-06-18T11:53:23.786, lastContactDate=2026-06-18T11:53:23.807, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-18T11:53:23.796, marketingConsent=true)',NULL,NULL,'2026-06-18 08:53:23'),(19,'ActivityService',9,'ACTIVITY_COMPLETED',NULL,'Activity(super=crm.model.entity.Activity@6f195bc3, activityType=CALL, contactId=14, opportunityId=null, subject=Consultare inițială, description=null, scheduledDate=2026-06-19T11:53:23, completedDate=2026-06-18T11:53:23.861, durationMinutes=30, assignedTo=1, status=COMPLETED, priority=HIGH, outcome=Persoana este interesată, nextSteps=Trimite ofertă, requiresFollowup=false, followupDate=null, createdBy=null)',NULL,NULL,'2026-06-18 08:53:23'),(20,'ContactService',14,'LEAD_STATUS_CHANGED',NULL,'Contact(super=crm.model.entity.Contact@467aecef, contactType=INDIVIDUAL, firstName=Maria, lastName=Demo, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=maria.demo.1781772803790@example.com, phone=0712345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=WEBSITE, leadStatus=INTERESTED, leadScore=100, experienceLevel=BEGINNER, learningGoal=Reconversie profesională în IT, firstContactDate=2026-06-18T11:53:23, lastContactDate=2026-06-18T11:53:23.877, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-18T11:53:23, marketingConsent=true)',NULL,NULL,'2026-06-18 08:53:23'),(21,'ContactService',15,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@467aecef, contactType=CORPORATE, firstName=null, lastName=null, birthDate=null, companyName=TechCorp SRL Demo, fiscalCode=RO12345678, registrationNumber=J40/1234/2020, industry=IT & Software, employeeCount=150, email=contact.demo.1781772803880@techcorp.ro, phone=0212345678, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=REFERRAL, leadStatus=NEW, leadScore=85, experienceLevel=null, learningGoal=null, firstContactDate=2026-06-18T11:53:23.880, lastContactDate=2026-06-18T11:53:23.881, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-18T11:53:23.880, marketingConsent=false)',NULL,NULL,'2026-06-18 08:53:23'),(22,'OpportunityService',7,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@11438d26, clientId=15, title=Training Java pentru echipa dev, description=null, estimatedParticipants=20, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=25000.00, quotedValue=null, probabilityPercent=50, stage=PROPOSAL_SENT, expectedCloseDate=2026-07-18, actualCloseDate=null, assignedTo=null, competitors=null, lostReason=null)',NULL,NULL,'2026-06-18 08:53:23'),(23,'ContactService',3,'LEAD_STATUS_CHANGED',NULL,'Contact(super=crm.model.entity.Contact@3043fe0e, contactType=INDIVIDUAL, firstName=Bogdan, lastName=Tudor, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=bogdan.tudor@example.com, phone=0722333666, addressStreet=null, addressCity=null, addressCounty=null, addressPostalCode=null, leadSource=FACEBOOK, leadStatus=INTERESTED, leadScore=35, experienceLevel=BEGINNER, learningGoal=Hobby, firstContactDate=2026-06-03T19:06:22, lastContactDate=2026-06-18T12:16:33.770, assignedTo=3, gdprConsent=true, gdprConsentDate=null, marketingConsent=false)',NULL,NULL,'2026-06-18 09:16:33'),(24,'OpportunityService',3,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@69a3d1d, clientId=3, title=#1 Opportunity, description=null, estimatedParticipants=5, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=1000.00, quotedValue=null, probabilityPercent=50, stage=PROPOSAL_SENT, expectedCloseDate=2026-08-13, actualCloseDate=null, assignedTo=null, competitors=null, lostReason=null)',NULL,NULL,'2026-06-18 09:17:52'),(25,'OpportunityService',1,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@2173f6d9, clientId=4, title=Training Java Spring pentru echipa Dev, description=Companie bancară dorește training intensiv, estimatedParticipants=25, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=35000.00, quotedValue=32000.00, probabilityPercent=70, stage=NEGOTIATION, expectedCloseDate=2026-08-01, actualCloseDate=null, assignedTo=4, competitors=null, lostReason=null)',NULL,NULL,'2026-06-19 16:36:49'),(26,'OpportunityService',7,'OPPORTUNITY_STAGE_CH',NULL,'Opportunity(super=crm.model.entity.Opportunity@2173f6d9, clientId=15, title=Training Java pentru echipa dev, description=null, estimatedParticipants=20, customRequirements=null, deliveryMode=ON_SITE, preferredLocation=null, desiredStartDate=null, estimatedValue=25000.00, quotedValue=null, probabilityPercent=70, stage=NEGOTIATION, expectedCloseDate=2026-07-18, actualCloseDate=null, assignedTo=null, competitors=null, lostReason=null)',NULL,NULL,'2026-06-19 16:37:23'),(27,'ActivityService',8,'ACTIVITY_COMPLETED',NULL,'Activity(super=crm.model.entity.Activity@59d73e5e, activityType=MEETING, contactId=6, opportunityId=null, subject=JAVA-ADV-MEET, description=null, scheduledDate=2026-06-19T11:17:36, completedDate=2026-06-24T17:04:28.359, durationMinutes=60, assignedTo=1, status=COMPLETED, priority=HIGH, outcome=GOOD, nextSteps=, requiresFollowup=false, followupDate=null, createdBy=null)',NULL,NULL,'2026-06-24 14:04:28'),(28,'ContactService',16,'CONTACT_CREATED',NULL,'Contact(super=crm.model.entity.Contact@edb297d, contactType=INDIVIDUAL, firstName=Flavius, lastName=Cornelius, birthDate=null, companyName=null, fiscalCode=null, registrationNumber=null, industry=null, employeeCount=null, email=flavius.cornelius@gmail.com, phone=0768676564, addressStreet=Izlaz, addressCity=Timisoara, addressCounty=Timis, addressPostalCode=300013, leadSource=COLD_CALL, leadStatus=ENROLLED, leadScore=85, experienceLevel=ADVANCED, learningGoal=To study., firstContactDate=2026-06-24T18:23:32.463, lastContactDate=2026-06-24T18:23:32.479, assignedTo=null, gdprConsent=true, gdprConsentDate=2026-06-24T18:23:32.463, marketingConsent=true)',NULL,NULL,'2026-06-24 15:23:32');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bids`
--

DROP TABLE IF EXISTS `bids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bids` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `auction_id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  `amount` decimal(12,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_bids_auction` (`auction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bids`
--

LOCK TABLES `bids` WRITE;
/*!40000 ALTER TABLE `bids` DISABLE KEYS */;
INSERT INTO `bids` VALUES (1,1,15,'TechCorp SRL Demo',22000.00,'2026-06-24 21:04:01','2026-06-24 21:04:01'),(2,1,4,'BankTech Solutions SRL',25000.00,'2026-06-24 21:04:01','2026-06-24 21:04:01');
/*!40000 ALTER TABLE `bids` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tip_client` enum('PERSOANA_FIZICA','PERSOANA_JURIDICA') NOT NULL,
  `nume` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `telefon` varchar(30) DEFAULT NULL,
  `adresa` varchar(255) DEFAULT NULL,
  `cnp` varchar(20) DEFAULT NULL,
  `cui` varchar(30) DEFAULT NULL,
  `reprezentant_legal` varchar(150) DEFAULT NULL,
  `data_inregistrare` timestamp NOT NULL DEFAULT current_timestamp(),
  `note` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_client_tip` (`tip_client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client`
--

LOCK TABLES `client` WRITE;
/*!40000 ALTER TABLE `client` DISABLE KEYS */;
/*!40000 ALTER TABLE `client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contacts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact_type` varchar(20) NOT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `company_name` varchar(200) DEFAULT NULL,
  `fiscal_code` varchar(50) DEFAULT NULL,
  `registration_number` varchar(50) DEFAULT NULL,
  `industry` varchar(100) DEFAULT NULL,
  `employee_count` int(11) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `address_street` varchar(200) DEFAULT NULL,
  `address_city` varchar(100) DEFAULT NULL,
  `address_county` varchar(100) DEFAULT NULL,
  `address_postal_code` varchar(10) DEFAULT NULL,
  `lead_source` varchar(50) DEFAULT NULL,
  `lead_status` varchar(50) DEFAULT 'NEW',
  `lead_score` int(11) DEFAULT 0,
  `experience_level` varchar(20) DEFAULT NULL,
  `learning_goal` varchar(100) DEFAULT NULL,
  `first_contact_date` timestamp NULL DEFAULT NULL,
  `last_contact_date` timestamp NULL DEFAULT NULL,
  `assigned_to` bigint(20) DEFAULT NULL,
  `gdpr_consent` tinyint(1) DEFAULT 0,
  `gdpr_consent_date` timestamp NULL DEFAULT NULL,
  `marketing_consent` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_lead_status` (`lead_status`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_contact_type` (`contact_type`),
  CONSTRAINT `contacts_ibfk_1` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contacts`
--

LOCK TABLES `contacts` WRITE;
/*!40000 ALTER TABLE `contacts` DISABLE KEYS */;
INSERT INTO `contacts` VALUES (1,'INDIVIDUAL','Alexandru','Stoica',NULL,NULL,NULL,NULL,NULL,'alex.stoica@example.com','0722333444',NULL,NULL,NULL,NULL,NULL,'WEBSITE','INTERESTED',65,'BEGINNER','Reconversie profesională în IT','2026-06-03 16:06:22',NULL,2,1,NULL,0,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(2,'INDIVIDUAL','Cristina','Marin',NULL,NULL,NULL,NULL,NULL,'cristina.marin@example.com','0722333555',NULL,NULL,NULL,NULL,NULL,'REFERRAL','QUALIFIED',80,'INTERMEDIATE','Avansare carieră','2026-06-03 16:06:22',NULL,2,1,NULL,0,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(3,'INDIVIDUAL','Bogdan','Tudor',NULL,NULL,NULL,NULL,NULL,'bogdan.tudor@example.com','0722333666',NULL,NULL,NULL,NULL,NULL,'FACEBOOK','INTERESTED',35,'BEGINNER','Hobby','2026-06-03 16:06:22','2026-06-18 09:16:33',3,1,NULL,0,'2026-06-03 16:06:22','2026-06-18 09:16:33'),(4,'CORPORATE',NULL,NULL,'BankTech Solutions SRL','RO12345678','J40/1234/2020','IT & Software',150,'hr@banktech.ro','0212223344',NULL,NULL,NULL,NULL,NULL,'REFERRAL','INTERESTED',85,NULL,NULL,'2026-06-03 16:06:22',NULL,4,1,NULL,0,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(5,'CORPORATE',NULL,NULL,'Health Innovations SA','RO87654321','J40/5678/2018','Healthcare',500,'training@healthinno.ro','0212223355',NULL,NULL,NULL,NULL,NULL,'WEBSITE','NEW',55,NULL,NULL,'2026-06-03 16:06:22',NULL,4,1,NULL,0,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(6,'INDIVIDUAL','Mihai','Radulescu',NULL,NULL,NULL,NULL,NULL,'mihai.radulescu04@e-uvt.ro','+40 768737531',NULL,NULL,NULL,NULL,NULL,'WEBSITE','INTERESTED',100,'BEGINNER','Reconversie profesională','2026-06-03 16:09:32','2026-06-18 08:14:30',NULL,1,'2026-06-03 16:09:33',0,'2026-06-03 16:09:33','2026-06-18 08:14:30'),(7,'INDIVIDUAL','Alexandru','Balasa',NULL,NULL,NULL,NULL,NULL,'alexandru.balasa@gmail.com','+40 737 122 911',NULL,NULL,NULL,NULL,NULL,'WEBSITE','QUALIFIED',100,'BEGINNER','Reconversie profesională','2026-06-06 10:34:24','2026-06-13 11:24:13',NULL,1,'2026-06-06 10:34:24',0,'2026-06-06 10:34:24','2026-06-13 11:24:13'),(8,'INDIVIDUAL','Maria','Demo',NULL,NULL,NULL,NULL,NULL,'maria.demo.1781309282984@example.com','0712345678',NULL,NULL,NULL,NULL,NULL,'WEBSITE','INTERESTED',100,'BEGINNER','Reconversie profesională în IT','2026-06-13 00:08:02','2026-06-13 00:08:03',NULL,1,'2026-06-13 00:08:02',1,'2026-06-13 00:08:03','2026-06-13 00:08:03'),(9,'CORPORATE',NULL,NULL,'TechCorp SRL Demo','RO12345678','J40/1234/2020','IT & Software',150,'contact.demo.1781309283051@techcorp.ro','0212345678',NULL,NULL,NULL,NULL,NULL,'REFERRAL','NEW',85,NULL,NULL,'2026-06-13 00:08:03','2026-06-13 00:08:03',NULL,1,'2026-06-13 00:08:03',0,'2026-06-13 00:08:03','2026-06-13 00:08:03'),(10,'CORPORATE',NULL,NULL,'Sensidev','44556',NULL,'IT',9,'sebastian.kalciov@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,'REFERRAL','NEW',55,NULL,NULL,'2026-06-13 10:19:09','2026-06-13 10:19:09',NULL,1,'2026-06-13 10:19:09',0,'2026-06-13 10:19:09','2026-06-13 10:19:09'),(11,'CORPORATE',NULL,NULL,'Companie7','226655',NULL,'ML',4,'companie7@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,'REFERRAL','NEW',45,NULL,NULL,'2026-06-13 10:30:59','2026-06-13 10:30:59',NULL,1,'2026-06-13 10:30:59',0,'2026-06-13 10:30:59','2026-06-13 10:30:59'),(12,'INDIVIDUAL','Maria','Demo',NULL,NULL,NULL,NULL,NULL,'maria.demo.1781349691234@example.com','0712345678',NULL,NULL,NULL,NULL,NULL,'WEBSITE','INTERESTED',100,'BEGINNER','Reconversie profesională în IT','2026-06-13 11:21:31','2026-06-13 11:21:31',NULL,1,'2026-06-13 11:21:31',1,'2026-06-13 11:21:31','2026-06-13 11:21:31'),(13,'CORPORATE',NULL,NULL,'TechCorp SRL Demo','RO12345678','J40/1234/2020','IT & Software',150,'contact.demo.1781349691338@techcorp.ro','0212345678',NULL,NULL,NULL,NULL,NULL,'REFERRAL','NEW',85,NULL,NULL,'2026-06-13 11:21:31','2026-06-13 11:21:31',NULL,1,'2026-06-13 11:21:31',0,'2026-06-13 11:21:31','2026-06-13 11:21:31'),(14,'INDIVIDUAL','Maria','Demo',NULL,NULL,NULL,NULL,NULL,'maria.demo.1781772803790@example.com','0712345678',NULL,NULL,NULL,NULL,NULL,'WEBSITE','INTERESTED',100,'BEGINNER','Reconversie profesională în IT','2026-06-18 08:53:23','2026-06-18 08:53:23',NULL,1,'2026-06-18 08:53:23',1,'2026-06-18 08:53:23','2026-06-18 08:53:23'),(15,'CORPORATE',NULL,NULL,'TechCorp SRL Demo','RO12345678','J40/1234/2020','IT & Software',150,'contact.demo.1781772803880@techcorp.ro','0212345678',NULL,NULL,NULL,NULL,NULL,'REFERRAL','NEW',85,NULL,NULL,'2026-06-18 08:53:23','2026-06-18 08:53:23',NULL,1,'2026-06-18 08:53:23',0,'2026-06-18 08:53:23','2026-06-18 08:53:23'),(16,'INDIVIDUAL','Flavius','Cornelius',NULL,NULL,NULL,NULL,NULL,'flavius.cornelius@gmail.com','0768676564',NULL,'Izlaz','Timisoara','Timis','300013','COLD_CALL','ENROLLED',85,'ADVANCED','To study.','2026-06-24 15:23:32','2026-06-24 15:23:32',NULL,1,'2026-06-24 15:23:32',1,'2026-06-24 15:23:32','2026-06-24 15:23:32');
/*!40000 ALTER TABLE `contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contract`
--

DROP TABLE IF EXISTS `contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contract` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) NOT NULL,
  `numar_contract` varchar(50) NOT NULL,
  `data_semnare` date DEFAULT NULL,
  `continut` text DEFAULT NULL,
  `cale_fisier` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numar_contract` (`numar_contract`),
  KEY `fk_contract_client` (`client_id`),
  CONSTRAINT `fk_contract_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contract`
--

LOCK TABLES `contract` WRITE;
/*!40000 ALTER TABLE `contract` DISABLE KEYS */;
/*!40000 ALTER TABLE `contract` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contracts`
--

DROP TABLE IF EXISTS `contracts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contracts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `opportunity_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `contract_number` varchar(50) NOT NULL,
  `sign_date` date NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `total_value` decimal(12,2) NOT NULL,
  `payment_terms` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `contract_number` (`contract_number`),
  KEY `opportunity_id` (`opportunity_id`),
  KEY `client_id` (`client_id`),
  CONSTRAINT `contracts_ibfk_1` FOREIGN KEY (`opportunity_id`) REFERENCES `opportunities` (`id`),
  CONSTRAINT `contracts_ibfk_2` FOREIGN KEY (`client_id`) REFERENCES `contacts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contracts`
--

LOCK TABLES `contracts` WRITE;
/*!40000 ALTER TABLE `contracts` DISABLE KEYS */;
/*!40000 ALTER TABLE `contracts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_sessions`
--

DROP TABLE IF EXISTS `course_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course_sessions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL,
  `session_code` varchar(50) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `schedule_description` varchar(500) DEFAULT NULL,
  `total_hours` int(11) NOT NULL,
  `delivery_mode` varchar(20) DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `meeting_link` varchar(500) DEFAULT NULL,
  `trainer_id` bigint(20) DEFAULT NULL,
  `max_participants` int(11) DEFAULT NULL,
  `current_participants` int(11) DEFAULT 0,
  `status` varchar(20) DEFAULT 'PLANNED',
  `average_rating` decimal(3,2) DEFAULT NULL,
  `is_corporate` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_code` (`session_code`),
  KEY `course_id` (`course_id`),
  KEY `trainer_id` (`trainer_id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_status` (`status`),
  CONSTRAINT `course_sessions_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  CONSTRAINT `course_sessions_ibfk_2` FOREIGN KEY (`trainer_id`) REFERENCES `trainers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_sessions`
--

LOCK TABLES `course_sessions` WRITE;
/*!40000 ALTER TABLE `course_sessions` DISABLE KEYS */;
INSERT INTO `course_sessions` VALUES (1,1,'JAVA-101-2026-01','2026-06-01','2026-07-15','L,M,J 18:00-21:00',60,'HYBRID','București - Sala 1',NULL,1,15,0,'OPEN_ENROLLMENT',NULL,0,'2026-06-03 16:06:22'),(2,3,'PYTHON-101-2026-01','2026-06-15','2026-07-30','M,J 18:00-21:00',50,'ONLINE',NULL,NULL,2,15,0,'OPEN_ENROLLMENT',NULL,0,'2026-06-03 16:06:22'),(3,4,'AI-INTRO-2026-01','2026-07-01','2026-08-15','L,M,V 18:00-21:00',70,'ONLINE',NULL,NULL,2,12,0,'PLANNED',NULL,0,'2026-06-03 16:06:22');
/*!40000 ALTER TABLE `course_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `courses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `syllabus` text DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `level` varchar(20) DEFAULT NULL,
  `prerequisites` text DEFAULT NULL,
  `duration_hours` int(11) NOT NULL,
  `price_individual` decimal(10,2) DEFAULT NULL,
  `price_group` decimal(10,2) DEFAULT NULL,
  `price_corporate_per_day` decimal(10,2) DEFAULT NULL,
  `min_participants` int(11) DEFAULT 3,
  `max_participants` int(11) DEFAULT 15,
  `active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'JAVA-101','Java Fundamentals','Curs introductiv în Java',NULL,'PROGRAMMING','BEGINNER',NULL,60,1500.00,1200.00,2500.00,5,15,1,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(2,'JAVA-ADV','Java Advanced & Spring','Curs avansat Java cu Spring Framework',NULL,'PROGRAMMING','ADVANCED',NULL,80,2500.00,2000.00,3500.00,5,12,1,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(3,'PYTHON-101','Python Fundamentals','Curs introductiv în Python',NULL,'PROGRAMMING','BEGINNER',NULL,50,1300.00,1100.00,2200.00,5,15,1,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(4,'AI-INTRO','AI și Machine Learning','Introducere în AI și ML cu Python',NULL,'AI','INTERMEDIATE',NULL,70,2200.00,1800.00,3000.00,5,12,1,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(5,'RECONV-IT','Reconversie Profesională IT','Program complet de reconversie 6 luni',NULL,'PROFESSIONAL_RECONVERSION','BEGINNER',NULL,480,6000.00,5000.00,NULL,10,20,1,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(6,'WS-DOCKER','Workshop Docker & Kubernetes','Workshop intensiv 2 zile',NULL,'WORKSHOP','INTERMEDIATE',NULL,16,800.00,700.00,1500.00,5,15,1,'2026-06-03 16:06:22','2026-06-03 16:06:22');
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `curs`
--

DROP TABLE IF EXISTS `curs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `curs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nume` varchar(200) NOT NULL,
  `descriere_generala` text DEFAULT NULL,
  `programa` text DEFAULT NULL,
  `durata_standard` varchar(50) DEFAULT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `tip_produs` enum('CURS_LUNG','SESIUNE_INTENSIVA','SESIUNE_PUNCTUALA') NOT NULL,
  `activ` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `curs`
--

LOCK TABLES `curs` WRITE;
/*!40000 ALTER TABLE `curs` DISABLE KEYS */;
INSERT INTO `curs` VALUES (1,'Curs de Reconversie JAVA','Curs complet de programare Java pentru incepatori, de la fundamentale la dezvoltare web.','Saptamana 1-4: Java Core; Saptamana 5-10: OOP & Colectii; Saptamana 11-18: Spring & Proiect final','18 saptamani','Programare','CURS_LUNG',1),(2,'Sesiune Intensiva Spring Framework','Sesiune intensiva pentru cei care vor sa invete Spring Boot rapid.','Zi 1: Spring Core; Zi 2: Spring Boot & REST; Zi 3: Proiect practic','3 zile','Programare','SESIUNE_INTENSIVA',1),(3,'Workshop Git & GitHub','Sesiune punctuala despre controlul versiunilor.','Notiuni de baza Git, branching, pull requests','1 zi','Unelte Dezvoltare','SESIUNE_PUNCTUALA',1);
/*!40000 ALTER TABLE `curs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employees` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `company_id` bigint(20) NOT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `job_title` varchar(150) DEFAULT NULL,
  `work_profile` varchar(50) DEFAULT NULL,
  `interest_profiles` varchar(500) DEFAULT NULL,
  `experience_level` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_employees_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,4,'Andrei','Pop','andrei@banktech.ro','Backend Engineer','SOFTWARE_DEVELOPMENT','MACHINE_LEARNING,ARTIFICIAL_INTELLIGENCE','INTERMEDIATE','2026-06-24 20:58:15','2026-06-24 20:58:15'),(2,4,'Ioana','Marin','ioana@banktech.ro','Data Analyst','DATA_SCIENCE','MACHINE_LEARNING','BEGINNER','2026-06-24 20:58:15','2026-06-24 20:58:15'),(3,15,'Vlad','Buftea',NULL,'Penetration Tester','CYBERSECURITY','ARTIFICIAL_INTELLIGENCE,IT_GENERAL,CYBERSECURITY,MACHINE_LEARNING','ADVANCED','2026-06-24 21:23:12','2026-06-24 21:23:12');
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enrollments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` bigint(20) NOT NULL,
  `contact_id` bigint(20) NOT NULL,
  `enrollment_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` varchar(20) DEFAULT 'PENDING',
  `price` decimal(10,2) NOT NULL,
  `discount` decimal(10,2) DEFAULT 0.00,
  `final_price` decimal(10,2) DEFAULT NULL,
  `payment_status` varchar(20) DEFAULT 'UNPAID',
  `paid_amount` decimal(10,2) DEFAULT 0.00,
  `attended_sessions` int(11) DEFAULT 0,
  `attendance_rate` decimal(5,2) DEFAULT NULL,
  `exam_passed` tinyint(1) DEFAULT NULL,
  `final_grade` decimal(5,2) DEFAULT NULL,
  `certificate_issued` tinyint(1) DEFAULT 0,
  `certificate_number` varchar(50) DEFAULT NULL,
  `rating` int(11) DEFAULT NULL,
  `feedback` text DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enrollment` (`session_id`,`contact_id`),
  KEY `contact_id` (`contact_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_status` (`payment_status`),
  CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `course_sessions` (`id`),
  CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`contact_id`) REFERENCES `contacts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura`
--

DROP TABLE IF EXISTS `factura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `factura` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `numar_factura` varchar(50) NOT NULL,
  `client_id` int(11) NOT NULL,
  `inscriere_id` int(11) NOT NULL,
  `suma` decimal(10,2) NOT NULL,
  `data_emitere` date NOT NULL,
  `data_scadenta` date DEFAULT NULL,
  `status` enum('EMISA','TRIMISA','PLATITA','ANULATA') NOT NULL DEFAULT 'EMISA',
  PRIMARY KEY (`id`),
  UNIQUE KEY `numar_factura` (`numar_factura`),
  KEY `fk_fact_client` (`client_id`),
  KEY `fk_fact_inscriere` (`inscriere_id`),
  KEY `idx_factura_status` (`status`),
  CONSTRAINT `fk_fact_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fact_inscriere` FOREIGN KEY (`inscriere_id`) REFERENCES `inscriere` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura`
--

LOCK TABLES `factura` WRITE;
/*!40000 ALTER TABLE `factura` DISABLE KEYS */;
/*!40000 ALTER TABLE `factura` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inscriere_id` int(11) NOT NULL,
  `nota` int(11) DEFAULT NULL CHECK (`nota` between 1 and 5),
  `comentariu` text DEFAULT NULL,
  `data_completare` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_feedback_inscriere` (`inscriere_id`),
  CONSTRAINT `fk_feedback_inscriere` FOREIGN KEY (`inscriere_id`) REFERENCES `inscriere` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inscriere`
--

DROP TABLE IF EXISTS `inscriere`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inscriere` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) NOT NULL,
  `prezentare_curs_id` int(11) NOT NULL,
  `data_inscriere` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('IN_ASTEPTARE','CONFIRMATA','RETRASA','FINALIZATA') NOT NULL DEFAULT 'IN_ASTEPTARE',
  `data_retragere` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_client_prezentare` (`client_id`,`prezentare_curs_id`),
  KEY `fk_insc_prez` (`prezentare_curs_id`),
  KEY `idx_inscriere_status` (`status`),
  CONSTRAINT `fk_insc_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_insc_prez` FOREIGN KEY (`prezentare_curs_id`) REFERENCES `prezentare_curs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inscriere`
--

LOCK TABLES `inscriere` WRITE;
/*!40000 ALTER TABLE `inscriere` DISABLE KEYS */;
/*!40000 ALTER TABLE `inscriere` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interactiune`
--

DROP TABLE IF EXISTS `interactiune`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interactiune` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) NOT NULL,
  `utilizator_id` int(11) NOT NULL,
  `tip` enum('FIZIC','ONLINE') NOT NULL,
  `data_interactiune` datetime NOT NULL,
  `rezultat` text DEFAULT NULL,
  `note` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_int_client` (`client_id`),
  KEY `fk_int_utilizator` (`utilizator_id`),
  CONSTRAINT `fk_int_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_int_utilizator` FOREIGN KEY (`utilizator_id`) REFERENCES `utilizator` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interactiune`
--

LOCK TABLES `interactiune` WRITE;
/*!40000 ALTER TABLE `interactiune` DISABLE KEYS */;
/*!40000 ALTER TABLE `interactiune` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoices`
--

DROP TABLE IF EXISTS `invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `invoices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_number` varchar(50) NOT NULL,
  `contract_id` bigint(20) DEFAULT NULL,
  `enrollment_id` bigint(20) DEFAULT NULL,
  `client_id` bigint(20) NOT NULL,
  `issue_date` date NOT NULL,
  `due_date` date NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `tax` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `paid_amount` decimal(12,2) DEFAULT 0.00,
  `status` varchar(20) DEFAULT 'DRAFT',
  `payment_date` date DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `invoice_number` (`invoice_number`),
  KEY `contract_id` (`contract_id`),
  KEY `enrollment_id` (`enrollment_id`),
  KEY `client_id` (`client_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`),
  CONSTRAINT `invoices_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`),
  CONSTRAINT `invoices_ibfk_2` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`id`),
  CONSTRAINT `invoices_ibfk_3` FOREIGN KEY (`client_id`) REFERENCES `contacts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoices`
--

LOCK TABLES `invoices` WRITE;
/*!40000 ALTER TABLE `invoices` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lead_score_logs`
--

DROP TABLE IF EXISTS `lead_score_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lead_score_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact_id` bigint(20) NOT NULL,
  `score_change` int(11) NOT NULL,
  `reason` varchar(200) DEFAULT NULL,
  `previous_score` int(11) DEFAULT NULL,
  `new_score` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_contact_id` (`contact_id`),
  CONSTRAINT `lead_score_logs_ibfk_1` FOREIGN KEY (`contact_id`) REFERENCES `contacts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lead_score_logs`
--

LOCK TABLES `lead_score_logs` WRITE;
/*!40000 ALTER TABLE `lead_score_logs` DISABLE KEYS */;
INSERT INTO `lead_score_logs` VALUES (1,8,50,'CALLBACK_REQUESTED',100,100,'2026-06-13 00:08:03'),(2,12,50,'CALLBACK_REQUESTED',100,100,'2026-06-13 11:21:31'),(3,14,50,'CALLBACK_REQUESTED',100,100,'2026-06-18 08:53:23'),(4,6,40,'MEETING_ATTENDED',100,100,'2026-06-24 14:04:28');
/*!40000 ALTER TABLE `lead_score_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opportunities`
--

DROP TABLE IF EXISTS `opportunities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opportunities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `estimated_participants` int(11) DEFAULT NULL,
  `custom_requirements` text DEFAULT NULL,
  `delivery_mode` varchar(20) DEFAULT NULL,
  `preferred_location` varchar(200) DEFAULT NULL,
  `desired_start_date` date DEFAULT NULL,
  `estimated_value` decimal(12,2) DEFAULT NULL,
  `quoted_value` decimal(12,2) DEFAULT NULL,
  `probability_percent` int(11) DEFAULT 50,
  `stage` varchar(50) DEFAULT 'LEAD_QUALIFICATION',
  `expected_close_date` date DEFAULT NULL,
  `actual_close_date` date DEFAULT NULL,
  `assigned_to` bigint(20) DEFAULT NULL,
  `competitors` varchar(500) DEFAULT NULL,
  `lost_reason` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `client_id` (`client_id`),
  KEY `assigned_to` (`assigned_to`),
  KEY `idx_stage` (`stage`),
  CONSTRAINT `opportunities_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `contacts` (`id`),
  CONSTRAINT `opportunities_ibfk_2` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opportunities`
--

LOCK TABLES `opportunities` WRITE;
/*!40000 ALTER TABLE `opportunities` DISABLE KEYS */;
INSERT INTO `opportunities` VALUES (1,4,'Training Java Spring pentru echipa Dev','Companie bancară dorește training intensiv',25,NULL,'ON_SITE',NULL,NULL,35000.00,32000.00,70,'NEGOTIATION','2026-08-01',NULL,4,NULL,NULL,'2026-06-03 16:06:22','2026-06-19 16:36:49'),(2,5,'Training Python & AI pentru echipa Data','Departament Data Science',15,NULL,'HYBRID',NULL,NULL,22000.00,NULL,40,'NEEDS_ANALYSIS','2026-09-01',NULL,4,NULL,NULL,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(3,3,'#1 Opportunity',NULL,5,NULL,'ON_SITE',NULL,NULL,1000.00,NULL,50,'PROPOSAL_SENT','2026-08-13',NULL,NULL,NULL,NULL,'2026-06-13 00:03:41','2026-06-18 09:17:52'),(4,9,'Training Java pentru echipa dev',NULL,20,NULL,'ON_SITE',NULL,NULL,25000.00,NULL,50,'PROPOSAL_SENT','2026-07-13',NULL,NULL,NULL,NULL,'2026-06-13 00:08:03','2026-06-13 00:08:03'),(5,13,'Training Java pentru echipa dev',NULL,20,NULL,'ON_SITE',NULL,NULL,25000.00,NULL,50,'PROPOSAL_SENT','2026-07-13',NULL,NULL,NULL,NULL,'2026-06-13 11:21:31','2026-06-13 11:21:31'),(6,3,'Java Swing',NULL,5,NULL,'ON_SITE',NULL,NULL,2000.00,NULL,10,'LEAD_QUALIFICATION','2026-08-13',NULL,NULL,NULL,NULL,'2026-06-13 11:26:22','2026-06-13 11:26:22'),(7,15,'Training Java pentru echipa dev',NULL,20,NULL,'ON_SITE',NULL,NULL,25000.00,NULL,70,'NEGOTIATION','2026-07-18',NULL,NULL,NULL,NULL,'2026-06-18 08:53:23','2026-06-19 16:37:23'),(8,12,'Java Beginner Guide',NULL,1,NULL,'ON_SITE',NULL,NULL,300.00,NULL,10,'LEAD_QUALIFICATION','2026-08-18',NULL,NULL,NULL,NULL,'2026-06-18 15:39:47','2026-06-18 15:39:47');
/*!40000 ALTER TABLE `opportunities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint(20) DEFAULT NULL,
  `enrollment_id` bigint(20) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `payment_date` date NOT NULL,
  `transaction_reference` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'COMPLETED',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `invoice_id` (`invoice_id`),
  KEY `enrollment_id` (`enrollment_id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`),
  CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`id`),
  CONSTRAINT `payments_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prezentare_curs`
--

DROP TABLE IF EXISTS `prezentare_curs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prezentare_curs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `curs_id` int(11) NOT NULL,
  `data_inceput` date NOT NULL,
  `data_sfarsit` date NOT NULL,
  `trainer_id` int(11) DEFAULT NULL,
  `locatie` varchar(255) DEFAULT NULL,
  `link_zoom` varchar(255) DEFAULT NULL,
  `capacitate_maxima` int(11) NOT NULL DEFAULT 20,
  `pret` decimal(10,2) NOT NULL,
  `status` enum('PLANIFICATA','IN_DESFASURARE','FINALIZATA','ANULATA') NOT NULL DEFAULT 'PLANIFICATA',
  PRIMARY KEY (`id`),
  KEY `fk_pc_curs` (`curs_id`),
  KEY `fk_pc_trainer` (`trainer_id`),
  KEY `idx_prezentare_status` (`status`),
  CONSTRAINT `fk_pc_curs` FOREIGN KEY (`curs_id`) REFERENCES `curs` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pc_trainer` FOREIGN KEY (`trainer_id`) REFERENCES `utilizator` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prezentare_curs`
--

LOCK TABLES `prezentare_curs` WRITE;
/*!40000 ALTER TABLE `prezentare_curs` DISABLE KEYS */;
INSERT INTO `prezentare_curs` VALUES (1,1,'2026-03-01','2026-08-30',3,'Sala A, Sediu Central','https://zoom.us/j/123456',20,4500.00,'PLANIFICATA'),(2,2,'2026-07-10','2026-07-12',3,NULL,'https://zoom.us/j/789012',15,900.00,'PLANIFICATA');
/*!40000 ALTER TABLE `prezentare_curs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proposals`
--

DROP TABLE IF EXISTS `proposals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `proposals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `opportunity_id` bigint(20) NOT NULL,
  `proposal_number` varchar(50) NOT NULL,
  `version` int(11) DEFAULT 1,
  `executive_summary` text DEFAULT NULL,
  `delivery_plan` text DEFAULT NULL,
  `payment_terms` text DEFAULT NULL,
  `subtotal` decimal(12,2) DEFAULT NULL,
  `discount` decimal(12,2) DEFAULT 0.00,
  `tax` decimal(12,2) DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  `issue_date` date NOT NULL,
  `expiry_date` date NOT NULL,
  `status` varchar(20) DEFAULT 'DRAFT',
  `sent_date` timestamp NULL DEFAULT NULL,
  `response_date` timestamp NULL DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `proposal_number` (`proposal_number`),
  KEY `opportunity_id` (`opportunity_id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `proposals_ibfk_1` FOREIGN KEY (`opportunity_id`) REFERENCES `opportunities` (`id`),
  CONSTRAINT `proposals_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proposals`
--

LOCK TABLES `proposals` WRITE;
/*!40000 ALTER TABLE `proposals` DISABLE KEYS */;
/*!40000 ALTER TABLE `proposals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainers`
--

DROP TABLE IF EXISTS `trainers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trainers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `specializations` varchar(500) DEFAULT NULL,
  `years_experience` int(11) DEFAULT NULL,
  `hourly_rate` decimal(10,2) DEFAULT NULL,
  `average_rating` decimal(3,2) DEFAULT NULL,
  `total_sessions` int(11) DEFAULT 0,
  `active` tinyint(1) DEFAULT 1,
  `user_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `trainers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainers`
--

LOCK TABLES `trainers` WRITE;
/*!40000 ALTER TABLE `trainers` DISABLE KEYS */;
INSERT INTO `trainers` VALUES (1,'Andrei','Mihai','andrei.mihai@trainingit.ro','0722111222',NULL,'Java, Spring, Microservices',10,150.00,4.80,25,1,NULL,'2026-06-03 16:06:22'),(2,'Elena','Vasilescu','elena.vasilescu@trainingit.ro','0722111333',NULL,'Python, AI, ML',8,140.00,4.90,18,1,NULL,'2026-06-03 16:06:22'),(3,'Mihai','Georgescu','mihai.georgescu@trainingit.ro','0722111444',NULL,'DevOps, Docker, Kubernetes',12,160.00,4.70,30,1,NULL,'2026-06-03 16:06:22');
/*!40000 ALTER TABLE `trainers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` varchar(30) NOT NULL,
  `active` tinyint(1) DEFAULT 1,
  `last_login` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin@trainingit.ro','$2a$10$abcdefghijklmnop','Admin','Principal',NULL,'ADMIN',1,NULL,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(2,'sales1','maria.popescu@trainingit.ro','$2a$10$abcdefghijklmnop','Maria','Popescu',NULL,'SALES_AGENT',1,NULL,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(3,'sales2','ion.ionescu@trainingit.ro','$2a$10$abcdefghijklmnop','Ion','Ionescu',NULL,'SALES_AGENT',1,NULL,'2026-06-03 16:06:22','2026-06-03 16:06:22'),(4,'manager','dana.manager@trainingit.ro','$2a$10$abcdefghijklmnop','Dana','Manager',NULL,'SALES_MANAGER',1,NULL,'2026-06-03 16:06:22','2026-06-03 16:06:22');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilizator`
--

DROP TABLE IF EXISTS `utilizator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `utilizator` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nume` varchar(100) NOT NULL,
  `prenume` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `parola_hash` varchar(255) NOT NULL,
  `rol` enum('ADMINISTRATOR','CONTABIL','TRAINER') NOT NULL,
  `telefon` varchar(30) DEFAULT NULL,
  `activ` tinyint(1) NOT NULL DEFAULT 1,
  `data_creare` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilizator`
--

LOCK TABLES `utilizator` WRITE;
/*!40000 ALTER TABLE `utilizator` DISABLE KEYS */;
INSERT INTO `utilizator` VALUES (1,'Popescu','Ion','admin@training.ro','admin123','ADMINISTRATOR',NULL,1,'2026-06-24 11:09:23'),(2,'Ionescu','Maria','contabil@training.ro','contabil123','CONTABIL',NULL,1,'2026-06-24 11:09:23'),(3,'Georgescu','Andrei','trainer.java@training.ro','trainer123','TRAINER',NULL,1,'2026-06-24 11:09:23');
/*!40000 ALTER TABLE `utilizator` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 18:16:54
