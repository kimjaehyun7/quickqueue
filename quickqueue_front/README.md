# QuickQueue Frontend

This is a minimal React + Vite frontend for the QuickQueue project described in the attached prompt.

Features:
- User reservation page
- Reservation status page with SSE
- Admin login (Kakao OAuth redirect) and demo dashboard
- API client configurable via `VITE_API_BASE_URL`

Setup

1. Install dependencies

```bash
cd quickqueue_front
npm install
```

2. Run dev server

```bash
npm run dev
```

Environment

 - Set `VITE_API_BASE_URL` to point to the backend (default: `http://16.176.178.31:8080`).

Development

This frontend expects the Spring Boot backend described in `prompt.txt` to be running. Configure the backend base URL with the `VITE_API_BASE_URL` environment variable before running the dev server.

Example (PowerShell):

```powershell
$env:VITE_API_BASE_URL = 'http://16.176.178.31:8080'; npm run dev
```

Example (Unix/macOS):

```bash
VITE_API_BASE_URL=http://16.176.178.31:8080 npm run dev
```

Backend integration notes

 - Ensure the backend allows CORS requests from the frontend origin (http://16.176.178.31:5173) and exposes the SSE endpoint with `Content-Type: text/event-stream`.
 - Admin OAuth: backend should handle OAuth with Kakao and either redirect to `/admin/callback?accessToken=...&publicId=...` for testing, or set HttpOnly cookies and redirect to `/admin/callback`.
 - This frontend sends credentials (cookies) with requests to support cookie-based admin sessions; configure the backend to accept requests with credentials from the frontend origin.

Public reservation page

- The public reservation page is served at `/events/{publicId}`. Admins can open or copy this URL from the dashboard to share on tablets or public devices.



Notes

- This is a minimal scaffold focused on functionality from the prompt. It expects the Spring Boot backend endpoints described in `prompt.txt` to be available.
