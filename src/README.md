# Backend

Backenddelen är en Spring Boot-applikation för WineRater. Den hanterar autentisering, viner, betyg och väderdata.

## Teknik

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- H2 för tester
- Maven
- Open-Meteo Weather API

## Viktiga Endpoints

```text
GET    /
POST   /auth/register
POST   /auth/login
GET    /auth/username-available?username=<username>
GET    /api/wines
GET    /api/wines/{id}
POST   /api/wines
PUT    /api/wines/{id}
DELETE /api/wines/{id}
POST   /api/ratings
GET    /api/ratings/wine/{wineId}
GET    /api/weather?city=Stockholm
```

## Säkerhet

Publika endpoints:

- `GET /`
- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/username-available`
- `GET /api/wines/**`
- `GET /api/ratings/**`
- `GET /api/weather`

Skyddade endpoints:

- `POST /api/wines` kräver `USER` eller `ADMIN`.
- `POST /api/ratings` kräver `USER` eller `ADMIN`.
- `PUT /api/wines/{id}` kräver `ADMIN`.
- `DELETE /api/wines/{id}` kräver `ADMIN`.

JWT skickas i headern:

```text
Authorization: Bearer <token>
```

## Väderintegration

Backend hämtar väderdata från Open-Meteo.

Flöde:

1. Frontend anropar `GET /api/weather?city=<stad>`.
2. Backend söker upp stadens koordinater via Open-Meteo Geocoding API.
3. Backend hämtar nuvarande väder och tre dagars prognos via Open-Meteo Forecast API.
4. Frontend visar resultatet.

Default-stad är `Toledo` i Spanien om ingen `city` anges.

Exempel:

```text
GET http://localhost:8080/api/weather?city=Stockholm
```

Responsen innehåller stad, land, tid, temperatur, vindhastighet, väderkod, datakälla och tre dagars prognos.

## Miljövariabler

Backend läser databas och JWT från miljövariabler.

Obligatoriska i runtime:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/winerater_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
JWT_SECRET=REPLACE_WITH_YOUR_OWN_JWT_SECRET
```

Valfria:

```env
PORT=8080
JWT_EXPIRATION_MS=3600000
H2_CONSOLE_ENABLED=false
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

Vid Docker Compose sätts dessa via `docker-compose.yml` och `.env` i projektroten.

## Köra Backend Lokalt Utan Compose

Starta MySQL:

```powershell
docker run --name datalagring-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=winerater_db -d mysql:latest
```

Sätt miljövariabler:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/winerater_db"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="password"
$env:JWT_SECRET="REPLACE_WITH_YOUR_OWN_JWT_SECRET"
```

Starta backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend körs på:

```text
http://localhost:8080
```

## Tester

Kör alla backendtester:

```powershell
.\mvnw.cmd clean verify
```

Testsviten innehåller:

- repository-tester
- service-tester
- controller-tester
- integrationstester med Spring Boot och MockMvc

## Docker

Backendens Dockerfile ligger i projektroten:

```text
Dockerfile
```

Hela applikationen kan startas från projektroten:

```powershell
docker compose up --build
```
