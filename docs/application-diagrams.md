# TrainingIT Application Diagrams

These diagrams describe the current TrainingIT implementation: a Next.js client portal and admin portal, a Spring Boot REST API, a pattern-based Java domain layer, and a MariaDB database.

## 1. System context

```mermaid
flowchart LR
    Learner["Learner or company employee"]
    Admin["TrainingIT administrator"]
    Trainer["Trainer"]
    App["TrainingIT platform"]
    AI["Anthropic Claude API"]
    DB[("MariaDB database")]

    Learner -->|"Browses and purchases courses, reviews courses, books tutoring"| App
    Admin -->|"Manages CRM data, courses, purchases, invoices, analytics, employees, and issues"| App
    Trainer -->|"Provides bookable tutoring availability"| App
    App -->|"Stores operational and CRM data"| DB
    App -->|"Requests chat, recommendations, sales support, and translation"| AI
```

## 2. Application architecture

```mermaid
flowchart TB
    subgraph Browser["Web browser"]
        Pages["Next.js 16 pages<br/>Client and admin portals"]
        Components["React components<br/>Catalog, booking, reviews, chatbot, reports"]
        Guard["AuthGuard and admin route guard"]
        Session["Local browser session<br/>USER or ADMIN"]
        ApiClient["Typed REST API client"]
        SseClient["Public statistics SSE client"]

        Guard --> Pages
        Session --> Guard
        Pages --> Components
        Components --> ApiClient
        Components --> SseClient
    end

    subgraph Backend["Spring Boot 3.5 backend"]
        Controllers["REST controllers"]
        StatsStream["Public statistics broadcaster"]
        Facade["CrmFacade"]
        DirectDomain["Specialized web services and DAOs<br/>Authentication, AI, trainers, reports"]

        ApiClient -->|"JSON, file upload, PDF or Excel download"| Controllers
        SseClient -->|"Server-Sent Events"| StatsStream
        Controllers --> Facade
        Controllers --> DirectDomain
    end

    subgraph Domain["Java domain layer"]
        Commands["CommandInvoker and domain commands"]
        Services["CRM services"]
        EventBus["EventBus"]
        Observers["Audit, notifications, lead scoring, invoice generation"]
        Persistence["Repositories and DAOs"]

        Facade --> Commands
        Facade --> Services
        DirectDomain --> Services
        DirectDomain --> Persistence
        Commands --> Services
        Services --> EventBus
        DirectDomain --> EventBus
        EventBus --> Observers
        Observers --> Services
        Services --> Persistence
    end

    Database[("MariaDB")]
    Claude["Anthropic Claude API"]

    Persistence -->|"JDBC through HikariCP"| Database
    DirectDomain -->|"Anthropic Java SDK"| Claude
    StatsStream -->|"Polls computed statistics while clients are subscribed"| Facade
```

## 3. Role-based navigation

```mermaid
flowchart TD
    Start["Open the application"] --> ReadSession{"Browser session exists?"}

    ReadSession -->|"No"| PublicAuth["Login, registration, or password recovery"]
    PublicAuth --> Login["POST /api/auth/login"]

    Login --> AccountType{"Which account table matches the email?"}
    AccountType -->|"admins"| AdminSession["Create ADMIN session"]
    AccountType -->|"contacts"| UserSession["Create USER session"]
    AccountType -->|"employees"| ResolveContact["Resolve or create a portal contact<br/>Apply employee discount"]
    ResolveContact --> UserSession
    AccountType -->|"No match or wrong password"| Reject["Reject sign-in"]

    ReadSession -->|"ADMIN"| AdminPortal
    ReadSession -->|"USER"| ClientPortal
    AdminSession --> AdminPortal["Admin portal"]
    UserSession --> ClientPortal["Client portal"]

    AdminPortal --> AdminFeatures["Contacts, courses, purchases, invoices,<br/>analytics, employees, and issues"]
    ClientPortal --> ClientFeatures["Course catalog, my courses, reviews,<br/>trainer schedule, my sessions, and issue reporting"]

    AdminPortal -.->|"Client route requested"| AdminRedirect["Redirect to /admin/contacts"]
    ClientPortal -.->|"Admin route requested"| ClientRedirect["Redirect to /"]
```

## 4. Core data model

This is a conceptual view of both the CRM tables and the web-feature tables created at application startup. It intentionally omits most scalar fields.

```mermaid
erDiagram
    USER_ACCOUNT o|--o{ CONTACT : "is assigned"
    USER_ACCOUNT o|--o{ OPPORTUNITY : "owns"
    USER_ACCOUNT o|--o{ ACTIVITY : "handles"

    CONTACT ||--o{ ENROLLMENT : "has"
    CONTACT ||--o{ OPPORTUNITY : "creates"
    CONTACT ||--o{ ACTIVITY : "receives"
    CONTACT ||--o{ MEDITATION_SESSION : "books"
    CONTACT ||--o{ SESSION_INVOICE : "is billed"
    CONTACT o|--o{ ISSUE_REPORT : "reports by email"

    CONTACT ||--o| CORPORATE_CONTACT : "may represent"
    CORPORATE_CONTACT ||--o{ EMPLOYEE : "employs"
    COURSE ||--o{ COURSE_SESSION : "offers"
    COURSE ||--o| COURSE_METRICS : "accumulates"
    COURSE_SESSION ||--o{ ENROLLMENT : "contains"
    OPPORTUNITY o|--o{ ACTIVITY : "is followed up by"
    TRAINER ||--o{ MEDITATION_SESSION : "delivers"
    MEDITATION_SESSION ||--o| SESSION_INVOICE : "generates"

    USER_ACCOUNT {
        bigint id PK
        string email UK
        string role
    }
    CONTACT {
        bigint id PK
        string contact_type
        string email UK
        string lead_status
        int lead_score
    }
    CORPORATE_CONTACT {
        bigint contact_id PK
        string company_name
        string industry
    }
    EMPLOYEE {
        bigint id PK
        bigint company_id
        string email
        string work_profile
    }
    COURSE {
        bigint id PK
        string code UK
        string name
        string category
        boolean active
    }
    COURSE_SESSION {
        bigint id PK
        bigint course_id FK
        string status
        date start_date
        date end_date
    }
    ENROLLMENT {
        bigint id PK
        bigint contact_id FK
        bigint session_id FK
        string status
        int rating
        string feedback
    }
    OPPORTUNITY {
        bigint id PK
        bigint client_id FK
        string stage
        decimal estimated_value
    }
    ACTIVITY {
        bigint id PK
        bigint contact_id FK
        bigint opportunity_id FK
        string activity_type
    }
    TRAINER {
        bigint id PK
        string email UK
        string first_name
        string last_name
    }
    MEDITATION_SESSION {
        bigint id PK
        bigint trainer_id FK
        bigint contact_id
        date session_date
        int start_hour
        int end_hour
    }
    SESSION_INVOICE {
        bigint id PK
        bigint session_id
        bigint client_id
        decimal subtotal
        decimal discount_rate
        decimal total
        string status
    }
    COURSE_METRICS {
        bigint course_id PK
        bigint impressions
        bigint clicks
    }
    ISSUE_REPORT {
        bigint id PK
        string reporter_email
        string status
        string message
    }
```

## 5. Course purchase and review flow

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant UI as Next.js catalog
    participant API as PublicCatalogController
    participant Facade as CrmFacade
    participant Review as ReviewService
    participant Enrollment as EnrollmentService
    participant DB as MariaDB
    participant Events as EventBus
    participant Stats as PublicStatsBroadcaster

    Client->>UI: Select a course and confirm purchase
    UI->>API: POST /api/public/courses/{courseId}/purchase
    API->>Facade: purchaseCourse(email, name, courseId)
    Facade->>Review: purchaseCourse(...)
    Review->>DB: Find or create contact
    Review->>DB: Find or create course session
    Review->>Enrollment: enrollContact(contactId, sessionId)
    Enrollment->>DB: Save enrollment
    Enrollment->>Events: Publish EnrollmentCreatedEvent
    Events-->>Enrollment: Notify enrollment observers
    Stats->>Facade: Poll computed statistics while a browser is subscribed
    Facade->>Review: getSiteStats()
    Review->>DB: Read courses, sessions, enrollments, and reviews
    Stats-->>UI: Push changed figures through SSE
    API-->>UI: Enrollment ID and success message

    Client->>UI: Submit rating and comment
    UI->>API: POST /api/public/courses/{courseId}/reviews
    API->>Facade: reviewCourse(email, courseId, rating, comment)
    Facade->>Review: Validate prior purchase
    Review->>DB: Save rating and feedback on enrollment
    DB-->>API: Updated review
    API-->>UI: Published review
```

## 6. Trainer booking and automatic invoice flow

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant UI as Schedule page
    participant Trainers as TrainerController
    participant Sessions as MeditationSessionDao
    participant Events as EventBus
    participant Observer as InvoiceGenerationObserver
    participant Invoice as InvoiceService
    participant Employees as EmployeeDao
    participant Invoices as InvoiceRepository

    Client->>UI: Choose trainer and date range
    UI->>Trainers: GET /api/trainers/{id}/availability
    Trainers->>Sessions: Load existing bookings
    Sessions-->>Trainers: Booked intervals
    Trainers-->>UI: Working days and free intervals

    Client->>UI: Choose start time and duration
    UI->>Trainers: POST /api/trainers/{id}/sessions
    Trainers->>Sessions: Revalidate account, hours, date, and overlap
    Sessions-->>Trainers: Validation result
    Trainers->>Sessions: Save tutoring session
    Trainers->>Events: Publish SessionBookedEvent
    Events->>Observer: Notify observer
    Observer->>Invoice: generateForSession(session)
    Invoice->>Invoices: Check for an existing invoice
    Invoice->>Employees: Check employee email
    Employees-->>Invoice: 60% discount or no discount
    Invoice->>Invoices: Save paid invoice
    Trainers-->>UI: Booking confirmation

    Note over Invoice,Invoices: Invoice creation is idempotent; observer failure is logged without rolling back the booking.
```

## 7. Domain patterns and event reactions

```mermaid
flowchart LR
    Controller["REST controller"] --> Facade["CrmFacade<br/>Facade and Singleton"]

    Facade --> Invoker["CommandInvoker<br/>Command and Singleton"]
    Invoker --> ContactCommands["Contact commands"]
    Invoker --> EnrollmentCommands["Enrollment commands"]
    Invoker --> OpportunityCommands["Opportunity commands"]

    Facade --> Services["Domain services<br/>Singletons"]
    ContactCommands --> Services
    EnrollmentCommands --> Services
    OpportunityCommands --> Services

    Services --> Repositories["Repositories and DAOs<br/>Generic repository pattern"]
    Repositories --> DB[("MariaDB")]

    Services --> Events["EventBus<br/>Observer and Singleton"]
    Controller -->|"SessionBookedEvent"| Events

    Events --> Audit["AuditLogObserver<br/>All CRM events"]
    Events --> Welcome["WelcomeEmailObserver<br/>ContactCreatedEvent"]
    Events --> Confirm["EnrollmentConfirmationObserver<br/>EnrollmentCreatedEvent"]
    Events --> Invoice["InvoiceGenerationObserver<br/>SessionBookedEvent"]
    Events --> Score["LeadScoreUpdateObserver<br/>ActivityCompletedEvent"]

    Builders["ContactBuilder and OpportunityBuilder"] --> ContactCommands
    Strategy["LeadScoringContext<br/>B2B and simple strategies"] --> Services
    Factory["Activity and notification factories"] --> Services
```
