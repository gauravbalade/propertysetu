# PropertySetu — Public Academic Demo Deployment

## Target

Publish the source code on GitHub and deploy a free/low-cost academic demonstration with:

- React/Vite frontend on a static host such as Vercel or Netlify.
- Spring Boot backend on a service that supports Java containers.
- Managed MySQL database.
- HTTPS frontend and backend URLs.
- Synthetic documents only.
- Test-mode payment only.

The exact free-tier availability and limits can change. Check the provider dashboard before committing to a provider.

## Required environment variables

Configure these in the backend hosting dashboard, never in GitHub:

```text
DB_URL=jdbc:mysql://managed-host:3306/property_registration_db?useSSL=true&serverTimezone=UTC
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=long-random-production-secret
JWT_ISSUER=propertysetu-production
DDL_AUTO=validate
SHOW_SQL=false
FORMAT_SQL=false
MAX_FILE_SIZE=5MB
MAX_REQUEST_SIZE=6MB
UPLOAD_DIR=/app/uploads
FRONTEND_ORIGIN=https://the-final-frontend-domain
SERVER_PORT=8080
```

Configure this in the frontend hosting dashboard:

```text
VITE_API_URL=https://the-final-backend-domain
```

Redeploy after changing environment variables.

## Deployment order

1. Create and test the GitHub repository.
2. Provision MySQL and record its private connection variables.
3. Deploy the backend and configure all backend variables.
4. Generate the backend HTTPS domain.
5. Configure the frontend `VITE_API_URL` with that backend domain.
6. Deploy the frontend and generate its HTTPS domain.
7. Replace `FRONTEND_ORIGIN` with the final frontend domain.
8. Redeploy the backend.
9. Register a new applicant from an incognito browser.
10. Test the complete applicant and officer workflow.

## Public safety rules

- Do not publish the officer password.
- Do not upload real identity, address, or property documents.
- Do not commit `.env`, secrets, target folders, node_modules, or generated uploads.
- Do not claim government integration, legal approval, live payment, or permanent document storage.
- Use a new production JWT secret, not the local development fallback.
- Keep CORS restricted to the final frontend origin.
- Use a persistent private storage service before treating uploaded files as durable records.

## Free-tier reality

A free deployment can be suitable for a portfolio demonstration, but it may sleep, have limited storage, lose local filesystem uploads, have database limits, or require a paid persistent-volume plan. A public demo link is therefore not equivalent to production readiness.

## Final acceptance test

- Frontend opens with HTTPS.
- Registration and login work.
- Applicant can create an application.
- Three synthetic required documents can be uploaded.
- Test-mode payment completes.
- Officer-only actions reject applicant accounts.
- Applicant cannot access another applicant's records.
- Protected document download requires authentication and ownership/officer access.
- CORS rejects unapproved frontend origins.
- No secrets or private documents appear in the GitHub repository.
