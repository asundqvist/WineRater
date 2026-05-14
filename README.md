# WineRater

WineRater är en fullstack-applikation för att hantera viner, betyg och vingårdsrelaterat väder. Projektet består av en Spring Boot-backend, en React/Vite-frontend och en MySQL-databas.

Applikationen är dockeriserad, kan köras lokalt med Docker Compose och är anpassad för deployment på Railway.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security med JWT
- Spring Data JPA
- MySQL
- React, TypeScript och Vite
- Tailwind CSS
- Open-Meteo Weather API
- Docker och Docker Compose
- GitHub Actions

## Projektstruktur

```text
.
├── src/                  Backend-kod och backend-README
├── frontend/             Frontend-kod och frontend-README
├── .github/workflows/    CI-pipeline
├── docker-compose.yml    Frontend + backend + MySQL
├── Dockerfile            Backend image
├── .env.example          Exempel på lokala miljövariabler
├── documentation/        Reflektion och övrig dokumentation
└── README.md             Övergripande instruktioner
```

## Funktioner

- Registrering och inloggning med JWT.
- Roller för `USER` och `ADMIN`.
- Lista, filtrera, skapa och betygsätta viner.
- Viner kan ha en valfri druva, exempelvis Pinot Noir eller Chardonnay.
- Admin kan uppdatera och radera viner.
- Vädervy för vingårdar och valfri stad.
- Backend hämtar väderdata från Open-Meteo.
- Frontend visar nuvarande väder och tre dagars prognos.

## Förutsättningar

- Docker Desktop
- Git
- Java 21 om backend ska köras utan Docker
- Node.js 20+ om frontend ska köras utan Docker

## Lokal Körning Med Docker Compose

1. Skapa en lokal `.env` från exemplet:

```powershell
Copy-Item .env.example .env
```

2. Starta hela applikationen:

```powershell
docker compose up --build
```

Frontend:

```text
http://localhost:5173
```

Backend:

```text
http://localhost:8080
```

Swagger/OpenAPI:

```text
http://localhost:8080/swagger-ui.html
```

MySQL exponeras lokalt på:

```text
localhost:3306
```

Stoppa containrarna:

```powershell
docker compose down
```

Stoppa containrarna och radera lokal databasvolym:

```powershell
docker compose down -v
```

## Miljövariabler

Lokalt används `.env` av Docker Compose. `.env` ska inte commitas.

Viktiga lokala variabler:

```env
MYSQL_DATABASE=winerater_db
DB_USERNAME=root
DB_PASSWORD=password
BACKEND_DB_URL=jdbc:mysql://db:3306/winerater_db
JWT_SECRET=REPLACE_WITH_YOUR_OWN_JWT_SECRET
JWT_EXPIRATION_MS=3600000
FRONTEND_API_URL=http://localhost:8080/api
FRONTEND_AUTH_URL=http://localhost:8080/auth
```

På Railway sätts motsvarande värden i respektive service. Backend behöver till exempel:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=REPLACE_WITH_YOUR_OWN_JWT_SECRET
JWT_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=https://winerater.up.railway.app
```

Frontend på Railway behöver:

```env
VITE_API_BASE_URL=https://winerater-production.up.railway.app/api
VITE_AUTH_BASE_URL=https://winerater-production.up.railway.app/auth
```

## Tester Och Build

Backendtester:

```powershell
.\mvnw.cmd clean verify
```

Frontend build:

```powershell
cd frontend
npm install
npm run build
```

## CI/CD

GitHub Actions finns i:

```text
.github/workflows/maven.yml
```

Pipelinen kör backendtester, frontend-build och `docker compose build` vid push och pull request mot `main`. Docker-steget verifierar att applikationens images kan byggas från projektets Dockerfiles. Vid push till `main` deployas backend och frontend till Railway efter lyckad build.

Följande GitHub Actions-secrets behövs för deployment:

```text
RAILWAY_TOKEN
RAILWAY_BACKEND_SERVICE
RAILWAY_FRONTEND_SERVICE
```

## Deployment

Applikationen är deployad till Railway med separata services för:

- backend
- frontend
- MySQL

Publika Railway-URL:er:

```text
Frontend: https://winerater.up.railway.app/
Backend: https://winerater-production.up.railway.app/
```

Vid demo ska frontendens Railway-URL öppnas i webbläsaren. Frontend anropar backendens publika Railway-URL, och backend använder Railway MySQL.

## Dokumentation För Delarna

- Backend: [src/README.md](src/README.md)
- Frontend: [frontend/README.md](frontend/README.md)
- Reflektion: [documentation/personal_reflections.md](documentation/personal_reflections.md)
