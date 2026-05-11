# Frontend

Frontenddelen är en React/Vite-applikation för WineRater.

Denna del är fortfarande work in progress och ska färdigställas senare. Backend och databas kan användas och testas separat under tiden.

## Teknik

- React
- TypeScript
- Vite
- Tailwind CSS
- Axios

## Förutsättningar

- Node.js 20+
- npm
- Backend körd på `http://localhost:8080`

## Miljövariabler

Frontend läser backendens API-adress från:

```text
VITE_API_BASE_URL
VITE_AUTH_BASE_URL
```

För lokal körning kan värdet vara:

```text
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AUTH_BASE_URL=http://localhost:8080/auth
```

När frontend senare körs via Docker Compose kan värdet i stället peka på backend-servicen, exempelvis:

```text
VITE_API_BASE_URL=http://backend:8080/api
VITE_AUTH_BASE_URL=http://backend:8080/auth
```

Se `.env.example` för exempel.

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

## Status

Frontend är inte färdig och ska justeras senare. Den här README-filen beskriver hur frontenddelen är tänkt att köras när arbetet fortsätter.

## Planerad Vineyard/Väder-Sida

Frontend ska senare få en separat sida eller flik, exempelvis `Vineyard`.

Frontend ansvarar för:

- lista några kända vingårdar
- visa vilken stad/region vingården ligger i
- ha ett inputfält där användaren kan söka på valfri stad
- anropa backend via `GET /api/weather?city=<stad>`
- visa nuvarande väder: stad, land, tid, temperatur, vindhastighet och väderkod
- visa tre dagars prognos med min-/maxtemperatur och väderkod
- visa tydligt att väderdata kommer från Open-Meteo

Backend ansvarar för själva kopplingen till vädertjänsten.
