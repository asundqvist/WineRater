# WineRater

WineRater är en fullstack-applikation för att hantera viner och betyg. Projektet består av en Spring Boot-backend, en MySQL-databas och en React/Vite-frontend.

Frontend är fortfarande under utveckling. Backend, databas, tester och Docker-grunden är prioriterade i nuläget.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security med JWT
- Spring Data JPA
- MySQL
- Open-Meteo weather API
- Docker och Docker Compose
- GitHub Actions
- React, TypeScript och Vite

## Projektstruktur

```text
.
├── src/                  Backend-kod och backend-README
├── frontend/             Frontend-kod och frontend-README
├── .github/workflows/    CI-pipeline
├── docker-compose.yml    Backend + MySQL
├── Dockerfile            Backend image
└── README.md             Övergripande instruktioner
```

## Förutsättningar

- Java 21
- Docker Desktop
- Git
- Node.js 20+ om frontend ska köras lokalt

## Köra Med Docker Compose

Sätt först miljövariabler för de känsliga värdena.

PowerShell:

```powershell
$env:DB_PASSWORD="password"
$env:JWT_SECRET="REPLACE_WITH_YOUR_OWN_JWT_SECRET"
$env:JWT_EXPIRATION_MS="900000"
docker compose up --build
```

Backend startar på:

```text
http://localhost:8080
```

MySQL exponeras på:

```text
localhost:3306
```

Swagger/OpenAPI nås normalt via:

```text
http://localhost:8080/swagger-ui.html
```

Stoppa containrarna:

```powershell
docker compose down
```

Stoppa containrarna och ta bort databasvolymen:

```powershell
docker compose down -v
```

## Köra Tester

Backendtester körs med:

```powershell
.\mvnw.cmd test
```

På Linux/macOS:

```bash
./mvnw test
```

Testsviten innehåller repository-tester, service-tester, controller-tester och integrationstester.

## CI/CD

GitHub Actions finns i:

```text
.github/workflows/maven.yml
```

Pipelinen kör backendtester vid push och pull request mot `main`.

## Delar

- Backend: se [src/README.md](src/README.md)
- Frontend: se [frontend/README.md](frontend/README.md)

## Status

- Backend: funktionell med JWT, validering, MySQL och tester.
- Väderintegration: backend hämtar väderdata från Open-Meteo via `GET /api/weather?city=...`.
- Databas: MySQL via Docker Compose.
- Frontend: work in progress och ska färdigställas senare.
