# Deployment Plan

## Recommended separation

- Frontend: Vercel, Netlify or another static-hosting provider
- Backend: Render, Railway, Fly.io or a VM
- Database: managed MySQL
- Files: object storage such as S3-compatible storage

## Required backend environment variables

```text
DB_USERNAME=<managed database user>
DB_PASSWORD=<managed database password>
JWT_SECRET=<random secret with at least 32 characters>
UPLOAD_DIR=<persistent upload path or object-storage mount>
FRONTEND_ORIGIN=https://<frontend-domain>
SERVER_PORT=8080
```

Update the datasource URL for the managed MySQL host and enable TLS as required by the provider.

## Required frontend environment variable

```text
VITE_API_URL=https://<backend-domain>
```

Run the frontend build with `npm run build` and publish the `dist` directory.

## Release checklist

- [ ] Use HTTPS for frontend and backend.
- [ ] Use a new random JWT secret; never use the local fallback.
- [ ] Configure a least-privilege database user.
- [ ] Replace `ddl-auto=update` with migrations.
- [ ] Configure persistent/object storage for documents.
- [ ] Restrict CORS to the exact frontend domain.
- [ ] Configure backups, logs, monitoring and alerting.
- [ ] Add rate limiting and account lockout protection.
- [ ] Run a security review of ownership and document-download authorization.
- [ ] Integrate a real payment provider only after obtaining sandbox credentials.
- [ ] Remove demo credentials from public documentation if the environment is public.

No public deployment is claimed until these steps are completed and the deployed URLs are verified.
