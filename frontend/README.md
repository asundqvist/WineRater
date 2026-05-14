# Frontend

Frontenddelen är en React/Vite-applikation för WineRater. Den används för att registrera/logga in användare, visa viner, skapa betyg, hantera adminfunktioner och visa väderdata för vingårdar/städer.

## Teknik

- React
- TypeScript
- Vite
- Tailwind CSS
- Axios
- Nginx för produktionsimage

## Funktioner

- Registrering och inloggning.
- Sessionbaserad auth-lagring med `sessionStorage`.
- Lista och filtrera viner efter typ, land och druva.
- Skapa vin som inloggad användare.
- Lägga betyg på vin.
- Admin kan uppdatera och radera vin.
- Vädervy med Open-Meteo-data via backend.
- Sök väder för valfri stad.
- Visa nuvarande väder och tre dagars prognos.

## Förutsättningar

- Node.js 20+
- npm
- Backend körd på `http://localhost:8080` vid lokal utveckling

## Miljövariabler

Frontend behöver veta var backend finns.

Lokal utveckling:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AUTH_BASE_URL=http://localhost:8080/auth
```

Railway:

```env
VITE_API_BASE_URL=https://winerater-production.up.railway.app/api
VITE_AUTH_BASE_URL=https://winerater-production.up.railway.app/auth
```

Eftersom Vite bygger in `VITE_*`-variabler vid build behöver frontend byggas om efter ändring av dessa värden.

## Installera Beroenden

```powershell
npm install
```

## Köra Frontend Lokalt

```powershell
npm run dev
```

Vite startar normalt på:

```text
http://localhost:5173
```

## Bygga Frontend

```powershell
npm run build
```

## Förhandsgranska Build

```powershell
npm run preview
```

## Docker

Frontendens Dockerfile ligger i:

```text
frontend/Dockerfile
```

Produktionsimagen bygger React-appen och serverar `dist` via Nginx på port `80`.

Bygg bara frontend-imagen från projektroten:

```powershell
docker compose build frontend
docker compose up -d frontend
```

Tvinga ny build utan cache:

```powershell
docker compose build --no-cache frontend
docker compose up -d frontend
```

## Railway

Frontend körs som separat Railway-service.

Rekommenderad konfiguration:

```text
Root Directory: frontend
Dockerfile: Dockerfile
Public domain target port: 80
```

Backendens `CORS_ALLOWED_ORIGINS` måste innehålla frontendens publika Railway-URL.
