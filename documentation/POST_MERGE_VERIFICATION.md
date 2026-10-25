# Post-Merge Verification Checklist

Use this checklist after pulling the latest `main` branch. It is deliberately a **manual verification record**, not a claim that the steps have already been run.

## 1. Confirm the code version

- [ ] Open the repository and confirm the latest changes are on `main`.
- [ ] For local source, use the default branch ZIP or run `git pull origin main` in your existing clone.
- [ ] Read `documentation/DEMO_AND_RUN_GUIDE.md` before starting services.

## 2. Check service availability (PowerShell)

With the backend running locally, run:

```powershell
try {
  $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -TimeoutSec 10
  "Backend health endpoint: HTTP $([int]$response.StatusCode)"
  $response.Content
} catch {
  "Backend health check failed: $($_.Exception.Message)"
}
```

With the frontend running, run:

```powershell
try {
  $response = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 10
  "Frontend: HTTP $([int]$response.StatusCode)"
} catch {
  "Frontend check failed: $($_.Exception.Message)"
}
```

A successful HTTP response only confirms that the endpoint/page responded. It does **not** prove database writes, authentication, document upload, payment, or officer review work end to end.

## 3. Use synthetic demo data only

- [ ] Use a fictional applicant, fictional property/address, and test documents.
- [ ] Register or sign in using a configured local/demo account.
- [ ] Create a property/application and confirm it appears in the UI after refresh.
- [ ] If document upload is enabled, upload a harmless synthetic PDF/image and confirm its displayed status.
- [ ] If an officer account is configured, review the application and verify the resulting status/history.
- [ ] If the reset-password flow is configured, complete one reset and confirm the same OTP is rejected when reused.
- [ ] Keep Razorpay in **Test Mode**. Use only test credentials and test payment details; never enter real card details for a classroom demo.
- [ ] If a step cannot be performed in the current environment, mark it “not tested” rather than describing it as successful.

## 4. Record actual outcomes

| Check | Result (Pass / Fail / Not tested) | Notes / date |
|---|---|---|
| Frontend page opens |  |  |
| Backend health endpoint responds |  |  |
| Database-backed read works |  |  |
| Registration/application persists after refresh |  |  |
| Authentication and role restrictions |  |  |
| Document upload and retrieval |  |  |
| Password-reset OTP cannot be reused |  |  |
| Razorpay Test Mode flow |  |  |
| Officer review/status history |  |  |

## 5. Demo safety and scope

- Do not use real identity documents, real OTPs, real payment credentials, or private user data in screenshots or presentations.
- A successful build/CI run is not equivalent to a live end-to-end test.
- PropertySetu is an academic demonstration, not an official government registration service and does not itself establish legal title or grant legal approval.
