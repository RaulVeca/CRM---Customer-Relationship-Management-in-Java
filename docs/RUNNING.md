# Running the CRM web application

The project has two parts that run together:

| Part | Tech | Port | Start command |
|------|------|------|---------------|
| Backend REST API | Spring Boot (Java) | 8080 | `./mvnw -q -DskipTests spring-boot:run` |
| Frontend | Next.js (React) | 3000 | `cd frontend && npm run dev` |

The original Swing desktop app is still available via `crm.CrmApplication` — both
front-ends share the same business layer.

## Prerequisites

- Java 17+ (tested on Java 25)
- Node 18+ / npm (tested on Node 22)
- MariaDB/MySQL running on `localhost:3306` with database `crm_training`
  (auto-created on first run; user `root`, no password by default — see
  `crm.config.AppConfig`).

## AI features (Claude)

The AI features (course recommendations, chatbot, sales assistant) call the
Anthropic API and require an API key. Set it in the `ANTHROPIC_API_KEY`
environment variable — it is **never** stored in source. A convenient option is
a gitignored `.env` file at the project root:

```
ANTHROPIC_API_KEY=sk-ant-...
```

If no key is set, the app still runs — the AI buttons and chatbot simply stay
hidden. The model defaults to `claude-opus-4-8`; set `crm.ai.model=claude-haiku-4-5`
in `application.properties` to reduce cost.

## Start (two terminals)

**Terminal 1 — backend:**

```bash
# load the API key from .env, then run
set -a && . ./.env && set +a
./mvnw -q -DskipTests spring-boot:run
```

Wait for `Started CrmWebApplication`. The API is then at <http://localhost:8080/api>.
Quick check: <http://localhost:8080/api/public/courses>.

**Terminal 2 — frontend:**

```bash
cd frontend
npm install   # first time only
npm run dev
```

Open <http://localhost:3000>.

> The frontend MUST run on port 3000 — the backend CORS configuration
> (`crm.web.config.WebConfig`) only allows that origin in development. To change
> it, set `crm.cors.allowed-origins` for the backend and `NEXT_PUBLIC_API_URL`
> for the frontend.

## Key endpoints

- `GET /api/public/courses[?category=AI]` — public course catalog
- `GET /api/public/companies` — public company list
- `GET /api/stats/dashboard` — admin dashboard metrics
- `GET /api/contacts`, `/api/courses`, `/api/opportunities`, `/api/activities`,
  `/api/enrollments`
