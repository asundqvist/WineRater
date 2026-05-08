# Backend

Backenddelen är en Spring Boot-applikation för WineRater. Den innehåller REST-endpoints för autentisering, viner och betyg.

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

## Viktiga Endpoints

```text
POST   /auth/register
POST   /auth/login
GET    /api/wines
GET    /api/wines/{id}
POST   /api/wines
PUT    /api/wines/{id}
DELETE /api/wines/{id}
POST   /api/ratings
GET    /api/ratings/wine/{wineId}
GET    /
GET    /api/weather?city=Toledo
```

## Säkerhet

Publika endpoints:

- `POST /auth/register`
- `POST /auth/login`
- `GET /api/wines/**`
- `GET /api/ratings/**`
- `GET /api/weather`
- `GET /`

## Väderintegration

Backend hämtar väderdata från Open-Meteo.

Flödet är:

1. `GET /api/weather?city=Stockholm`
2. Backend söker upp stadens koordinater via Open-Meteo Geocoding API.
3. Backend hämtar nuvarande väder och tre dagars prognos via Open-Meteo Forecast API.
4. Frontend visar resultatet.

Default stad är `Toledo` i Spanien om ingen `city` anges.
För defaultstaden skickar backend med landskod `ES` till Open-Meteo.
Stadssökningen normaliseras i backend, så `Stockholm`, `stockholm` och `STOCKHOLM` behandlas likadant.

Exempel:

```text
GET http://localhost:8080/api/weather?city=Stockholm
```

Responsen innehåller stad, land, tid, temperatur, vindhastighet, väderkod, datakälla och tre dagars prognos med min-/maxtemperatur.

Skyddade endpoints:

- `POST /api/wines` kräver `USER` eller `ADMIN`
- `POST /api/ratings` kräver `USER` eller `ADMIN`
- `PUT /api/wines/{id}` kräver `ADMIN`
- `DELETE /api/wines/{id}` kräver `ADMIN`

JWT skickas i headern:

```text
Authorization: Bearer <token>
```

## Miljövariabler

Backend läser känsliga värden från miljövariabler.

Obligatoriska:

```text
DB_PASSWORD
JWT_SECRET
```

Valfria:

```text
DB_URL              default: jdbc:mysql://localhost:3306/winerater_db
DB_USERNAME         default: root
JWT_EXPIRATION_MS   default: 900000
H2_CONSOLE_ENABLED  default: false
```

PowerShell-exempel:

```powershell
$env:DB_PASSWORD="password"
$env:JWT_SECRET="REPLACE_WITH_YOUR_OWN_JWT_SECRET"
.\mvnw.cmd spring-boot:run
```

## Köra Backend Lokalt

Starta först MySQL, exempelvis med Docker:

```powershell
docker run --name datalagring-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=winerater_db -d mysql:latest
```

Starta sedan backend:

```powershell
$env:DB_PASSWORD="password"
$env:JWT_SECRET="REPLACE_WITH_YOUR_OWN_JWT_SECRET"
.\mvnw.cmd spring-boot:run
```

Backend körs på:

```text
http://localhost:8080
```

## Tester

Kör alla backendtester:

```powershell
.\mvnw.cmd test
```

Tester finns för:

- repositories
- services
- controllers
- integration genom Spring Boot och MockMvc

## Docker

Backendens Dockerfile ligger i projektroten:

```text
Dockerfile
```

Hela backend + databas kan startas via projektets root:

```powershell
docker compose up --build
```
