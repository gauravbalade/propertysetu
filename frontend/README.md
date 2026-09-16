# Property Registration System Frontend

React + Vite frontend for the Property Registration and Document Verification System.

## Start

```powershell
npm install
npm run dev
```

Open `http://localhost:5173` after starting the Spring Boot backend on port `8080`.

For another backend URL, copy `.env.example` to `.env` and set:

```text
VITE_API_URL=https://your-backend.example.com
```

## Features

- Applicant registration and JWT login
- Owner, property and location capture
- Application creation and document upload
- Application submission and ₹500 test-mode payment
- Officer dashboard with search and status filters
- Officer/admin-only verification actions
- Responsive layout for demo screens

## Quality commands

```powershell
npm run lint
npm run build
```

Payment is intentionally labelled test mode. Do not present it as live Razorpay unless a real sandbox integration is added and verified.

