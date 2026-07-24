# TrainingIT — complete installation and startup guide

TrainingIT is a full-stack web application for managing courses and customer
relationships. It includes a public catalog, authentication for customers and
administrators, purchases and reviews, session scheduling, invoicing, contact
management and optional AI features.

## Table of contents

1. [Technology and architecture](#technology-and-architecture)
2. [Requirements](#requirements)
3. [Quick start on Windows](#quick-start-on-windows)
4. [Startup on macOS or Linux](#startup-on-macos-or-linux)
5. [Demo accounts](#demo-accounts)
6. [Configuration](#configuration)
7. [Verifying the application](#verifying-the-application)
8. [Build and tests](#build-and-tests)
9. [Stopping the application](#stopping-the-application)
10. [Troubleshooting](#troubleshooting)
11. [Project structure](#project-structure)

## Technology and architecture

- **Frontend:** Next.js 16, React 19, TypeScript and Tailwind CSS 4
- **Backend:** Java 17+, Spring Boot 3.5 and Maven
- **Database:** MariaDB/MySQL, accessed through JDBC and HikariCP
- **Optional AI:** Anthropic Claude
- **Frontend port:** `3000`
- **Backend port:** `8080`
- **Default MariaDB/MySQL port:** `3306`

The application flow is:

```text
Browser
  └── Next.js: http://localhost:3000
        └── REST API: http://localhost:8080/api
              └── MariaDB: localhost:3306/crm_training
```

## Requirements

Install the following software before the first startup:

- **JDK 17 or newer** — check: `java -version`
- **Node.js 20.9 or newer** — check: `node --version`
- **npm** — check: `npm --version`
- **MariaDB or MySQL** — the MySQL server bundled with XAMPP works as well
- **Git**, if the project is cloned from a repository

A global Maven installation is not required. The project includes the Maven
Wrapper through `mvnw.cmd` and `mvnw`.

On first use, Maven and npm need internet access to download dependencies.

## Quick start on Windows

All of the commands below are run from the project root:

```powershell
cd D:\TrainingIT_site
```

### 1. Start MariaDB/MySQL

If you use XAMPP, open the **XAMPP Control Panel** and start the **MySQL**
module.

If you use MariaDB/MySQL installed as a Windows service, start the corresponding
service and confirm that it is listening on port `3306`.

### 2. Initialize the database

These two scripts are run in order and only when setting up a new database:

```powershell
mysql --default-character-set=utf8mb4 -u root -p -e "source sql/schema.sql"
mysql --default-character-set=utf8mb4 -u root -p -e "source sql/seed-data.sql"
```

Enter the `root` user's password when prompted. For the default XAMPP setup,
where the `root` user has no password, drop the `-p` flag:

```powershell
mysql --default-character-set=utf8mb4 -u root -e "source sql/schema.sql"
mysql --default-character-set=utf8mb4 -u root -e "source sql/seed-data.sql"
```

If the `mysql` executable is not on your `PATH`, use the full path:

```powershell
& "C:\xampp\mysql\bin\mysql.exe" --default-character-set=utf8mb4 -u root -e "source sql/schema.sql"
& "C:\xampp\mysql\bin\mysql.exe" --default-character-set=utf8mb4 -u root -e "source sql/seed-data.sql"
```

> The `seed-data.sql` script adds demo data and should not be run on every
> startup. Running it repeatedly can produce duplicate-key errors.

### 3. Configure database access

The backend's default configuration is:

```properties
db.url=jdbc:mariadb://localhost:3306/crm_training?createDatabaseIfNotExist=true
db.username=root
db.password=
```

If your server uses a different user, password or port, set the matching values
in `src/main/resources/application.properties`. Do not publish the database
password and do not include it in a commit.

### 4. Configure the AI features — optional

In the project root, the `.env` file may contain:

```dotenv
ANTHROPIC_API_KEY=your_key
```

The `.env` file is ignored by Git. If it is missing or the key is empty, the
application still starts normally, but the chatbot, translation and AI
recommendations will not be available.

### 5. Start the backend

Open the first PowerShell terminal in the project root:

```powershell
.\run-backend.ps1
```

The script automatically loads the variables from `.env`, then starts Spring
Boot. The backend is ready when the terminal shows a message similar to:

```text
Tomcat started on port 8080
Started CrmWebApplication
```

If the PowerShell policy blocks the script, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-backend.ps1
```

Alternatively, without the PowerShell script:

```powershell
.\mvnw.cmd spring-boot:run
```

With this variant, the AI key must be defined in the terminal before startup:

```powershell
$env:ANTHROPIC_API_KEY="your_key"
.\mvnw.cmd spring-boot:run
```

### 6. Start the frontend

Leave the backend running and open a second terminal:

```powershell
cd D:\TrainingIT_site\frontend
npm.cmd install
npm.cmd run dev
```

On systems where PowerShell scripts for npm are allowed, the commands can also
be written as `npm install` and `npm run dev`.

### 7. Open the application

- Web interface: [http://localhost:3000](http://localhost:3000)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- Public catalog API:
  [http://localhost:8080/api/public/courses](http://localhost:8080/api/public/courses)
- AI feature status:
  [http://localhost:8080/api/ai/status](http://localhost:8080/api/ai/status)

## Startup on macOS or Linux

From the project root:

```bash
# One-time database initialization
mysql --default-character-set=utf8mb4 -u root -p < sql/schema.sql
mysql --default-character-set=utf8mb4 -u root -p < sql/seed-data.sql

# The AI key is optional
export ANTHROPIC_API_KEY="your_anthropic_key"

# Start the backend
chmod +x mvnw
./mvnw spring-boot:run
```

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Then open [http://localhost:3000](http://localhost:3000).

## Demo accounts

On the backend's first startup, the existing accounts in the `admins`,
`contacts` and `employees` tables are assigned the default demo password `1234`.

Example admin accounts created by the seed data:

- `andreibirceanu@adminit.ro`
- `costachemazarescu@adminit.ro`
- `pablovarga@adminit.ro`

An administrator can sign in, for example, with:

```text
Email: andreibirceanu@adminit.ro
Password: 1234
```

Example accounts from the `contacts` table:

| Name / company | Contact type | Email | Password |
| --- | --- | --- | --- |
| Alexandru Stoica | Individual | `alex.stoica@example.com` | `1234` |
| Cristina Marin | Individual | `cristina.marin@example.com` | `1234` |
| Bogdan Tudor | Individual | `bogdan.tudor@example.com` | `1234` |
| BankTech Solutions SRL | Company | `hr@banktech.ro` | `1234` |

These contacts are added by `sql/seed-data.sql`. The password `1234` is assigned
automatically on the backend's first startup.

Example accounts from the `employees` table:

| Name | Company | Email | Password |
| --- | --- | --- | --- |
| Andrei Popescu | SensiDEV | `andreipopescu@sensidev.ro` | `1234` |
| Maria Ionescu | SensiDEV | `mariaionescu@sensidev.ro` | `1234` |
| Gabriel Toma | Dedeman | `gabrieltoma@dedeman.ro` | `1234` |
| Ștefan Cojocaru | Antibiotice | `stefancojocaru@antibiotice.ro` | `1234` |

Employee accounts automatically receive the **60%** employee discount. The
examples above exist in the local demo database. When initializing a brand-new
database, employees must be added or imported from the
**Admin → Employees** area before these addresses can be used for sign-in. Every
new employee is assigned the default password `1234`.

These credentials are for development and demo purposes only. Do not use the
password `1234` in a public or production environment.

A visitor can create their own account from the **Register** page. Accounts
created this way receive the user role, not the administrator role.

## Configuration

### Backend

Main file:
`src/main/resources/application.properties`

The important settings are:

| Setting | Default value | Role |
| --- | --- | --- |
| `db.url` | `jdbc:mariadb://localhost:3306/crm_training?...` | The database connection |
| `db.username` | `root` | The database user |
| `db.password` | empty | The database password |
| `crm.cors.allowed-origins` | `http://localhost:3000` | The frontend origin allowed by CORS |
| `crm.ai.api-key` | the `ANTHROPIC_API_KEY` variable | Enables the AI integration |
| `crm.ai.model` | `claude-opus-4-8` | The Anthropic model used |
| `crm.ai.max-tokens` | `2048` | The AI response limit |

The Spring Boot port defaults to `8080`. To change it, add:

```properties
server.port=8081
```

### Frontend

By default the frontend uses the API at `http://localhost:8080`. For a different
address, create `frontend/.env.local`:

```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8081
```

After changing the frontend variables, restart the Next.js server.

If you change the frontend port or run the application on a different domain,
also update `crm.cors.allowed-origins` in the backend.

## Verifying the application

With the database and backend running, verify from PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/public/courses
Invoke-RestMethod http://localhost:8080/api/ai/status
```

Or with `curl`:

```bash
curl http://localhost:8080/api/public/courses
curl http://localhost:8080/api/ai/status
```

Checklist for a complete startup:

- MariaDB/MySQL is running on port `3306`.
- The `crm_training` database contains the schema and the demo data.
- The backend responds on `http://localhost:8080`.
- The frontend responds on `http://localhost:3000`.
- The catalog displays the courses.
- Signing in with a demo account works.
- The AI endpoint reports the features as active only if the Anthropic key is
  configured.

## Build and tests

### Backend

Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd compile
```

macOS/Linux:

```bash
./mvnw test
./mvnw compile
```

### Frontend

```bash
cd frontend
npm run lint
npm run build
npm run start
```

`npm run start` launches the local production build and must be run only after
an `npm run build`.

## Stopping the application

In each of the two terminals, press `Ctrl+C`:

1. stops the Next.js server;
2. stops the Spring Boot backend;
3. stop MariaDB/MySQL from XAMPP or from the service manager, if it is no longer
   used by another application.

The database data stays saved and does not need to be re-imported on the next
startup.

On subsequent startups only these steps are needed:

1. start MariaDB/MySQL;
2. run `.\run-backend.ps1`;
3. run `npm.cmd run dev` from the `frontend` directory;
4. open `http://localhost:3000`.

## Troubleshooting

### The backend does not connect to the database

Common messages:

- `Connection refused` — the MariaDB/MySQL server is not running or the port is
  wrong.
- `Access denied for user` — the user or password in `application.properties`
  does not match the server.
- `Unknown database 'crm_training'` — the `sql/schema.sql` script was not run.
- `Table ... doesn't exist` — the schema was not imported completely or the
  scripts were run in the wrong order.

### Port 3000 or 8080 is already in use

In PowerShell:

```powershell
Get-NetTCPConnection -State Listen |
  Where-Object LocalPort -in 3000,8080 |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

Stop the old process or change the service's port. If you change the backend
port, also update `NEXT_PUBLIC_API_URL`.

### `npm.ps1 cannot be loaded because running scripts is disabled`

Use the Windows executable:

```powershell
npm.cmd install
npm.cmd run dev
```

### The Maven Wrapper does not start

Check first:

```powershell
java -version
.\mvnw.cmd -version
```

If the wrapper cannot download Maven, check internet access, the proxy and the
firewall. As an alternative, install Maven 3.9+ and run:

```powershell
mvn spring-boot:run
```

### The frontend shows network or CORS errors

Check at the same time:

- the backend is running at the address defined in `NEXT_PUBLIC_API_URL`;
- `crm.cors.allowed-origins` contains the frontend's exact address;
- both servers use the same protocol, usually `http` in development.

### The AI features are unavailable

Check:

```powershell
Invoke-RestMethod http://localhost:8080/api/ai/status
```

Then confirm that `.env` is in the project root, contains `ANTHROPIC_API_KEY`,
and that the backend was started through `run-backend.ps1`. Restart the backend
after any change to the key.

### Where the logs are

The backend writes the application logs to the `logs/` directory, in particular
to:

- `logs/crm-application.log`
- `logs/backend-run.log`, when startup is redirected to this file

## Project structure

```text
TrainingIT_site/
├── frontend/                       # The Next.js application
│   ├── public/                     # Images and static assets
│   ├── src/app/                    # Pages and routes
│   ├── src/components/             # React components
│   └── src/lib/                    # API client and utilities
├── src/main/java/crm/              # Java backend and REST API
├── src/main/resources/
│   ├── application.properties      # Backend configuration
│   └── logback.xml                 # Logging configuration
├── sql/
│   ├── schema.sql                  # Initial database schema
│   └── seed-data.sql               # Demo data
├── docs/                           # Diagrams and technical documentation
├── logs/                           # Local logs
├── .env                            # Local AI key; ignored by Git
├── pom.xml                         # Maven configuration
├── mvnw / mvnw.cmd                 # Maven Wrapper
└── run-backend.ps1                 # Backend startup on Windows
```

The architecture and application-flow diagrams are also described in
`docs/application-diagrams.md`.
