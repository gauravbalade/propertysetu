import { useState } from "react";
import "./App.css";

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();

const API = configuredApiUrl?.startsWith("http://") ||
  configuredApiUrl?.startsWith("https://")
  ? configuredApiUrl.replace(/\/+$/, "")
  : "https://propertysetu-backend.onrender.com";

function App() {
  const [step, setStep] = useState("login");
  const [showAbout, setShowAbout] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [user, setUser] = useState(null);
  const [owner, setOwner] = useState(null);
  const [property, setProperty] = useState(null);
  const [application, setApplication] = useState(null);
  const [payment, setPayment] = useState(null);
  const [properties, setProperties] = useState([]);
  const [officerMode, setOfficerMode] = useState(false);
  const [applications, setApplications] = useState([]);
  const [applicantApplications, setApplicantApplications] = useState([]);
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [selectedDocuments, setSelectedDocuments] = useState([]);
  const [auditEvents, setAuditEvents] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [applicationSearch, setApplicationSearch] = useState("");
  const [remarks, setRemarks] = useState(
    "Payment confirmed and all submitted documents were checked successfully."
  );

  const [loginForm, setLoginForm] = useState({
    username: "",
    password: ""
  });

  const [registerForm, setRegisterForm] = useState({
    username: "",
    password: "",
    email: "",
    phone: ""
  });

  const [ownerForm, setOwnerForm] = useState({
    name: "Gaurav Bhimrao Balade",
    address: "Pune, Maharashtra",
    phone: "9876543211",
    identityNumber: "DEMO-ID-002"
  });

  const [propertyForm, setPropertyForm] = useState({
    propertyNumber: "PROP-2001",
    propertyType: "RESIDENTIAL",
    area: "1200",
    description: "Demo residential property"
  });

  const [locationForm, setLocationForm] = useState({
    address: "Pune-Satara Road",
    city: "Pune",
    district: "Pune",
    state: "Maharashtra",
    pincode: "411037"
  });

  const [purpose, setPurpose] = useState("Property registration application");
  const [documentType, setDocumentType] = useState("IDENTITY_PROOF");
  const [file, setFile] = useState(null);
  const [uploadedDocuments, setUploadedDocuments] = useState([]);
  const [submissionAcknowledged, setSubmissionAcknowledged] = useState(false);
  const [busy, setBusy] = useState(false);

  const requiredDocumentTypes = ["IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT"];
  const hasRequiredDocuments = requiredDocumentTypes.every(type =>
    uploadedDocuments.some(document => document.documentType === type)
  );

  async function request(url, options = {}) {
    const token = user?.token || sessionStorage.getItem("property_registration_token");
    const isFormData = options.body instanceof FormData;
    const headers = {
      ...(isFormData ? {} : { "Content-Type": "application/json" }),
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    const response = await fetch(API + url, {
      ...options,
      headers: {
        ...headers,
        ...options.headers
      }
    });

    if (response.status === 204) {
      return null;
    }

    const data = await response.json().catch(() => ({}));

    if (response.status === 401 || response.status === 403) {
      sessionStorage.removeItem("property_registration_token");
      throw new Error("Your session expired or you are not allowed to do that. Please log in again.");
    }

    if (!response.ok) {
      throw new Error(data.message || data.error || "Request failed. Please check your connection and try again.");
    }

    return data;
  }

  async function openProtectedDocument(applicationId, documentId) {
    try {
      const token = user?.token || sessionStorage.getItem("property_registration_token");
      const response = await fetch(`${API}/api/applications/${applicationId}/documents/${documentId}/download`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      });
      if (!response.ok) {
        throw new Error("Unable to open the protected document");
      }
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      window.open(url, "_blank", "noopener,noreferrer");
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
    } catch (err) {
      setError(err.message);
    }
  }

  function clearMessages() {
    setMessage("");
    setError("");
  }

  function logout() {
    setBusy(false);
    setUser(null);
    setOfficerMode(false);
    setOwner(null);
    setProperty(null);
    setApplication(null);
    setPayment(null);
    setProperties([]);
    setApplications([]);
    setApplicantApplications([]);
    setSelectedApplication(null);
    setSelectedDocuments([]);
    setAuditEvents([]);
    setUploadedDocuments([]);
    setSubmissionAcknowledged(false);
    setFile(null);
    setStep("login");
    clearMessages();
    sessionStorage.removeItem("property_registration_token");
  }

  async function login(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(loginForm)
      });

      setUser(data);
      sessionStorage.setItem("property_registration_token", data.token);
      if (data.role === "OFFICER" || data.role === "ADMIN") {
        const applicationsData = await request("/api/applications");
        setApplications(applicationsData);
        setOfficerMode(true);
      } else {
        const savedApplications = await request("/api/applications");
        const savedProperties = await request("/api/properties/me").catch(() => []);
        const existingOwner = await request("/api/owners/me").catch(() => null);
        setApplicantApplications(savedApplications || []);
        setProperties(savedProperties || []);

        if (existingOwner) {
          setOwner(existingOwner);
          setOwnerForm({
            name: existingOwner.name || "",
            address: existingOwner.address || "",
            phone: existingOwner.phone || "",
            identityNumber: existingOwner.identityNumber || ""
          });
        }

        const current = [...(savedApplications || [])]
          .filter(item => item.status !== "REJECTED")
          .sort((left, right) => new Date(right.createdAt || 0) - new Date(left.createdAt || 0))[0];
        const currentProperty = current
          ? (savedProperties || []).find(item => item.id === current.propertyId)
          : null;

        if (current && currentProperty) {
          setApplication(current);
          setProperty(currentProperty);
          setPurpose(current.purpose || "");
          if (current.status === "DRAFT") {
            const documents = await request(`/api/applications/${current.id}/documents`).catch(() => []);
            setUploadedDocuments(documents || []);
            setStep("documents");
            setMessage(`Welcome back, ${data.username}. Your application is ready to continue.`);
          } else if (["SUBMITTED", "PAYMENT_PENDING"].includes(current.status)) {
            setStep("payment");
            setMessage(`Welcome back, ${data.username}. Your application is ready for payment.`);
          } else {
            setStep("paymentComplete");
            setMessage(`Welcome back, ${data.username}. Your application status is ${current.status}.`);
          }
        } else if (existingOwner) {
          setStep("property");
          setMessage(`Welcome back, ${data.username}. Your saved owner profile is ready.`);
        } else {
          setStep("owner");
        }
      }

      setMessage(`Welcome, ${data.username}.`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function register(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(registerForm)
      });

      setUser(null);
      sessionStorage.removeItem("property_registration_token");
      setMessage("Applicant registered successfully. Please log in to continue.");
      setLoginForm({ username: registerForm.username, password: registerForm.password });
      setStep("login");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function createOwner(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request("/api/owners", {
        method: "POST",
        body: JSON.stringify({
          userId: user.id,
          ...ownerForm
        })
      });

      setOwner(data);
      setMessage("Owner profile created successfully.");
      setStep("property");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function createProperty(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request("/api/properties", {
        method: "POST",
        body: JSON.stringify({
          ownerId: owner.id,
          propertyNumber: propertyForm.propertyNumber,
          propertyType: propertyForm.propertyType,
          area: Number(propertyForm.area),
          description: propertyForm.description
        })
      });

      setProperty(data);
      setProperties(previous => [...previous, data]);
      setMessage("Property created successfully.");
      setStep("location");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function createLocation(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const existingLocation = await request(`/api/locations/property/${property.id}`).catch(() => null);
      if (!existingLocation) {
        await request("/api/locations", {
          method: "POST",
          body: JSON.stringify({
            propertyId: property.id,
            ...locationForm
          })
        });
      } else {
        setLocationForm({
          address: existingLocation.address || "",
          city: existingLocation.city || "",
          district: existingLocation.district || "",
          state: existingLocation.state || "",
          pincode: existingLocation.pincode || ""
        });
      }

      setMessage(existingLocation ? "Saved property location loaded." : "Property location saved successfully.");
      setStep("application");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function createApplication(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request("/api/applications", {
        method: "POST",
        body: JSON.stringify({
          userId: user.id,
          propertyId: property.id,
          purpose
        })
      });

      setApplication(data);
      setApplicantApplications(previous => {
        const withoutCurrent = previous.filter(item => item.id !== data.id);
        return [data, ...withoutCurrent];
      });
      const documents = await request(`/api/applications/${data.id}/documents`).catch(() => []);
      setUploadedDocuments(documents || []);
      setMessage("Registration application ready.");
      setStep("documents");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function uploadDocument(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();
    setBusy(true);

    if (!file) {
      setError("Please choose a document first.");
      setBusy(false);
      return;
    }

    const formData = new FormData();
    formData.append("documentType", documentType);
    formData.append("file", file);

    try {
      const uploaded = await request(`/api/applications/${application.id}/documents`, {
        method: "POST",
        body: formData
      });

      setUploadedDocuments(previous => [
        ...previous.filter(document => document.documentType !== uploaded.documentType),
        uploaded
      ]);
      setFile(null);
      setMessage(`${documentType.replaceAll("_", " ")} uploaded successfully.`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function submitApplication() {
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request(
        `/api/applications/${application.id}/submit`,
        { method: "POST" }
      );

      setApplication(data);
      setMessage("Application submitted successfully.");
      setStep("payment");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function createPayment() {
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request("/api/payments/order", {
        method: "POST",
        body: JSON.stringify({
          applicationId: application.id,
          amount: 500
        })
      });

      setPayment(data);
      setMessage("Test payment order created successfully.");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function completePayment() {
    if (busy) return;
    clearMessages();
    setBusy(true);

    try {
      const data = await request("/api/payments/verify", {
        method: "POST",
        body: JSON.stringify({
          paymentId: payment.id,
          paymentReference: `TEST_PAYMENT_${payment.id}`,
          successful: true
        })
      });

      setPayment(data);
      setApplication(data.application);
      setApplicantApplications(await request("/api/applications"));
      setMessage("Payment completed successfully.");
      setStep("paymentComplete");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  function update(setter, field, value) {
    setter(previous => ({ ...previous, [field]: value }));
  }

  function goToPreviousStep() {
    const previousSteps = {
      property: "owner",
      location: "property",
      application: "location",
      documents: "application",
      submit: "documents",
      payment: "submit"
    };

    const previousStep = previousSteps[step];
    if (previousStep) {
      clearMessages();
      setStep(previousStep);
    }
  }

  async function openOfficerDashboard() {
    clearMessages();
    try {
      const data = await request("/api/applications");
      setApplications(data);
      setSelectedApplication(null);
      setOfficerMode(true);
    } catch (err) {
      setError(err.message);
    }
  }

  const visibleApplications = applications.filter(item => {
    const matchesStatus = statusFilter === "ALL" || item.status === statusFilter;
    const query = applicationSearch.trim().toLowerCase();
    const matchesSearch = !query
      || item.applicationNumber?.toLowerCase().includes(query)
      || item.applicantUsername?.toLowerCase().includes(query)
      || item.propertyNumber?.toLowerCase().includes(query);
    return matchesStatus && matchesSearch;
  });

  async function loadAuditHistory(applicationId) {
    try {
      setAuditEvents(await request(`/api/audit/applications/${applicationId}`));
    } catch (err) {
      setAuditEvents([]);
      setError(err.message);
    }
  }

  async function refreshApplicantApplications() {
    clearMessages();
    try {
      const data = await request("/api/applications");
      setApplicantApplications(data);
      const current = application && data.find(item => item.id === application.id);
      if (current) {
        setApplication(current);
      } else if (!application && data.length > 0) {
        const latest = [...data].sort((left, right) => new Date(right.createdAt || 0) - new Date(left.createdAt || 0))[0];
        setApplication(latest);
      }
      setMessage("Application status refreshed.");
    } catch (err) {
      setError(err.message);
    }
  }

  async function verifySelected(status) {
    if (!selectedApplication) {
      setError("Select an application first.");
      return;
    }

    clearMessages();

    try {
      const data = await request("/api/verifications", {
        method: "POST",
        body: JSON.stringify({
          applicationId: selectedApplication.id,
          verifiedByUserId: user.id,
          status,
          remarks
        })
      });

      setMessage(`Application ${status.toLowerCase()} successfully.`);
      setSelectedApplication(data.application);
      await loadAuditHistory(data.application.id);
      const refreshed = await request("/api/applications");
      setApplications(refreshed);
    } catch (err) {
      setError(err.message);
    }
  }

  if (step === "login" && !officerMode) {
    return (
      <main className="app auth-page">
        <header className="header auth-header">
          <div>
            <p className="eyebrow">PROPERTYSETU · PROPERTY REGISTRATION</p>
            <h1>Register property with confidence.</h1>
            <p className="subtitle">A simple digital journey for applicants and a clear review desk for officers.</p>
          </div>
          <div className="header-actions">
            <span className="status">Secure access</span>
            <button className="link-button" onClick={() => setShowAbout(previous => !previous)}>
              {showAbout ? "Hide overview" : "How it works"}
            </button>
          </div>
        </header>

        <section className="trust-hero" aria-label="PropertySetu trust promise">
          <div className="trust-hero-copy">
            <p className="eyebrow">A BETTER PROPERTY JOURNEY</p>
            <h2>Turn paperwork into a clear, trackable application.</h2>
            <p>PropertySetu brings property details, protected documents and accountable review into one guided workspace.</p>
            <div className="hero-pill-row"><span>✓ Guided steps</span><span>✓ Protected files</span><span>✓ Clear status</span></div>
          </div>
          <img src="/propertysetu-hero.svg" alt="A modern home protected by a verification shield" />
        </section>

        {showAbout && (
          <section className="public-overview">
            <div>
              <p className="eyebrow">ONE CLEAR WORKFLOW</p>
              <h2>From property details to verified registration.</h2>
              <p>Applicants submit owner information, property details, documents and a test-mode fee. Authorized officers then review the paid application and record a verification decision.</p>
            </div>
            <div className="overview-grid">
              <div><b>01</b><span>Capture details</span><small>Owner, property and location</small></div>
              <div><b>02</b><span>Submit securely</span><small>Documents and application status</small></div>
              <div><b>03</b><span>Verify fairly</span><small>Officer review and final decision</small></div>
            </div>
            <div className="overview-checklist">
              <h3>Prepare before you begin</h3>
              <p>Keep clear scans ready. The exact legal list depends on your transaction and registering office.</p>
              <div className="checklist-tags"><span>Identity proof</span><span>Address proof</span><span>Sale/title paper</span><span>Tax or fee proof</span></div>
              <small>Accepted demo files: PDF, JPG, JPEG and PNG. Exact requirements vary by transaction and office.</small>
              <a className="official-link" href="https://igrmaharashtra.gov.in/Home/checklist" target="_blank" rel="noreferrer">Check the official Maharashtra checklist ↗</a>
            </div>
            <p className="overview-note">Demo payment is simulated test mode and does not charge real money. Uploading a file does not mean legal approval.</p>
          </section>
        )}

        {message && <div className="message success">{message}</div>}
        {error && <div className="message error">{error}</div>}

        <section className="card auth-card">
          <form onSubmit={login}>
            <h2>Welcome back</h2>
            <p className="muted">Use your account to continue your application or open the officer workspace.</p>
            <input
              id="login-username"
              name="username"
              autoComplete="username"
              placeholder="Username"
              value={loginForm.username}
              onChange={e => update(setLoginForm, "username", e.target.value)}
              required
            />
            <input
              id="login-password"
              name="password"
              autoComplete="current-password"
              type="password"
              placeholder="Password"
              value={loginForm.password}
              onChange={e => update(setLoginForm, "password", e.target.value)}
              required
            />
            <button type="submit" disabled={busy}>{busy ? "Signing in…" : "Login"}</button>
          </form>
          <div className="auth-trust-row"><span>🔒 JWT-protected sessions</span><span>📄 Guided document workflow</span><span>✓ Officer review</span></div>
          <div className="auth-divider">New applicant?</div>
          <button className="secondary-button" onClick={() => {
            clearMessages();
            setStep("register");
          }}>Create applicant account</button>
        </section>
      </main>
    );
  }

  if (officerMode) {
    return (
      <main className="app">
        <header className="header">
          <div>
          <p className="eyebrow">PROPERTYSETU · ACCOUNTABLE REVIEW</p>
          <h1>Officer verification workspace</h1>
            <p className="subtitle">Review paid applications and complete verification.</p>
          </div>
          <div>
            <div className="status">{user ? `${user.username} · Officer` : "Officer Portal"}</div>
            <button className="link-button" onClick={logout}>Log out</button>
          </div>
        </header>

        {message && <div className="message success">{message}</div>}
        {error && <div className="message error">{error}</div>}

        <section className="dashboard-grid">
          <div className="card application-list">
            <h2>Applications</h2>
            <button className="secondary-button" onClick={openOfficerDashboard}>Refresh applications</button>
            <input
              placeholder="Search application, applicant or property"
              value={applicationSearch}
              onChange={e => setApplicationSearch(e.target.value)}
            />
            <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
              <option value="ALL">All statuses</option>
              <option value="PAID">Paid</option>
              <option value="UNDER_VERIFICATION">Under verification</option>
              <option value="SUBMITTED">Submitted</option>
              <option value="COMPLETED">Completed</option>
              <option value="REJECTED">Rejected</option>
            </select>
            {visibleApplications.length === 0 && <p className="muted">No matching applications found.</p>}
            {visibleApplications.map(item => (
              <button
                className={selectedApplication?.id === item.id ? "application-row selected" : "application-row"}
                key={item.id}
                onClick={async () => {
                  setSelectedApplication(item);
                  await loadAuditHistory(item.id);
                  try {
                    setSelectedDocuments(await request(`/api/applications/${item.id}/documents`));
                  } catch (err) {
                    setError(err.message);
                  }
                }}
              >
                <strong>{item.applicationNumber}</strong>
                <span>{item.applicantUsername || "Applicant"}</span>
                <span className="badge">{item.status}</span>
              </button>
            ))}
          </div>

          <div className="card">
            <h2>Review application</h2>
            {!selectedApplication && <p className="muted">Select an application from the list.</p>}
            {selectedApplication && (
              <>
                <div className="summary">
                  <p><b>Application:</b> {selectedApplication.applicationNumber}</p>
                  <p><b>Applicant:</b> {selectedApplication.applicantUsername}</p>
                  <p><b>Property:</b> {selectedApplication.propertyNumber}</p>
                  <p><b>Status:</b> {selectedApplication.status}</p>
                  <p><b>Purpose:</b> {selectedApplication.purpose}</p>
                  <p><b>Created:</b> {selectedApplication.createdAt ? new Date(selectedApplication.createdAt).toLocaleString("en-IN") : "-"}</p>
                </div>
                <div className="officer-documents">
                  <h3>Document review</h3>
                  <p className="muted">Verify every required document before completing this application.</p>
                  {selectedDocuments.length === 0 && <p className="muted">No documents uploaded.</p>}
                  {selectedDocuments.map(document => (
                    <div className="officer-document-row" key={document.id}>
                      <span><b>{document.documentType.replaceAll("_", " ")}</b><small>{document.fileName}</small><button type="button" className="document-link" onClick={() => openProtectedDocument(selectedApplication.id, document.id)}>Open protected file</button></span>
                      <select value={document.status} onChange={async event => {
                        try {
                          const updated = await request(`/api/applications/${selectedApplication.id}/documents/${document.id}/review?status=${event.target.value}`, { method: "POST" });
                          setSelectedDocuments(previous => previous.map(item => item.id === updated.id ? updated : item));
                          setMessage("Document review status updated.");
                        } catch (err) {
                          setError(err.message);
                        }
                      }}>
                        <option value="UPLOADED">Uploaded</option>
                        <option value="UNDER_REVIEW">Under review</option>
                        <option value="VERIFIED">Verified</option>
                        <option value="REJECTED">Rejected</option>
                      </select>
                    </div>
                  ))}
                </div>
                <div className="audit-panel">
                  <div className="audit-heading">
                    <h3>Accountability history</h3>
                    <span>{auditEvents.length} event{auditEvents.length === 1 ? "" : "s"}</span>
                  </div>
                  {auditEvents.length === 0 ? (
                    <p className="muted">No recorded history for this application.</p>
                  ) : (
                    <div className="audit-list">
                      {auditEvents.map(event => (
                        <div className="audit-event" key={event.id}>
                          <div>
                            <strong>{event.action.replaceAll("_", " ")}</strong>
                            <small>{event.actorUsername || "System"} · {event.actorRole || "SYSTEM"}</small>
                          </div>
                          <time>{event.occurredAt ? new Date(event.occurredAt).toLocaleString("en-IN") : "-"}</time>
                          {event.details && <p>{event.details}</p>}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                {selectedApplication.status !== "COMPLETED" && (
                  <textarea value={remarks} onChange={e => setRemarks(e.target.value)} />
                )}
                {selectedApplication.status === "COMPLETED" ? (
                  <div className="message success">
                    This application is already completed and does not need further verification.
                  </div>
                ) : (
                  <div className="action-row">
                    <button onClick={() => verifySelected("VERIFIED")} disabled={!selectedDocuments.length || !["IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT"].every(type => selectedDocuments.some(document => document.documentType === type && document.status === "VERIFIED"))}>Verify application</button>
                    <button className="reject-button" onClick={() => verifySelected("REJECTED")}>Reject</button>
                  </div>
                )}
              </>
            )}
          </div>
        </section>

      </main>
    );
  }

  return (
    <main className="app">
      <header className="header">
        <div>
          <p className="eyebrow">PROPERTYSETU · TRUSTED PROPERTY WORKFLOW</p>
          <h1>Register your property with clarity.</h1>
          <p className="subtitle">
            Submit property details, documents and track verification.
          </p>
        </div>
        <div>
          <div className="status">{user ? `${user.username} · Applicant` : "Applicant Portal"}</div>
          <button className="link-button" onClick={logout}>Log out</button>
        </div>
      </header>

      <section className="applicant-status-bar">
        <div>
          <p className="eyebrow">APPLICATION CONTROL CENTRE</p>
          <h2>{application?.applicationNumber || "Start a guided application"}</h2>
          <p className="muted">{application ? "Your application remains visible as it moves through preparation, payment and officer review." : "Complete the guided steps below. You can return to this workspace while preparing your submission."}</p>
        </div>
        <div className="status-control">
          <span className="badge">{application?.status || "DRAFT"}</span>
          <button type="button" className="secondary-button" onClick={refreshApplicantApplications}>Refresh status</button>
        </div>
      </section>

      {applicantApplications.length > 0 && (
        <section className="application-timeline-card">
          <div className="timeline-heading">
            <div><h3>My applications</h3><p className="muted">A transparent view of your submitted records.</p></div>
            <span>{applicantApplications.length} record{applicantApplications.length === 1 ? "" : "s"}</span>
          </div>
          <div className="applicant-application-list">
            {applicantApplications.map(item => (
              <button type="button" className={application?.id === item.id ? "applicant-application active" : "applicant-application"} key={item.id} onClick={() => setApplication(item)}>
                <span><b>{item.applicationNumber}</b><small>{item.propertyNumber || "Property record"}</small></span>
                <strong>{item.status}</strong>
              </button>
            ))}
          </div>
        </section>
      )}

      <section className="progress">
        {["owner", "property", "location", "application", "documents", "submit", "payment"].map(
          (item, index) => (
            <div className={step === item ? "progress-item active" : "progress-item"} key={item}>
              <span>{index + 1}</span>
              {item}
            </div>
          )
        )}
      </section>

      {message && <div className="message success">{message}</div>}
      {error && <div className="message error">{error}</div>}

      <section className="card">
        {step === "register" && (
          <form onSubmit={register}>
            <h2>Create applicant account</h2>
            <p className="muted">Create an account, then log in to begin registration.</p>

            <input id="register-username" name="username" autoComplete="username" placeholder="Username" value={registerForm.username}
              onChange={e => update(setRegisterForm, "username", e.target.value)} required />
            <input id="register-password" name="password" autoComplete="new-password" type="password" placeholder="Password" value={registerForm.password}
              onChange={e => update(setRegisterForm, "password", e.target.value)} required />
            <input id="register-email" name="email" type="email" autoComplete="email" placeholder="Email" value={registerForm.email}
              onChange={e => update(setRegisterForm, "email", e.target.value)} required />
            <input id="register-phone" name="phone" type="tel" autoComplete="tel" placeholder="Phone" value={registerForm.phone}
              onChange={e => update(setRegisterForm, "phone", e.target.value)} required />

            <button type="submit" disabled={busy}>{busy ? "Creating account…" : "Create account"}</button>
            <button type="button" className="secondary-button" onClick={() => setStep("login")}>Back to login</button>
          </form>
        )}

        {step === "owner" && (
          <form onSubmit={createOwner}>
            <h2>Owner profile</h2>
            <input placeholder="Full name" value={ownerForm.name}
              onChange={e => update(setOwnerForm, "name", e.target.value)} />
            <input placeholder="Address" value={ownerForm.address}
              onChange={e => update(setOwnerForm, "address", e.target.value)} />
            <input placeholder="Phone" value={ownerForm.phone}
              onChange={e => update(setOwnerForm, "phone", e.target.value)} />
            <input placeholder="Identity number" value={ownerForm.identityNumber}
              onChange={e => update(setOwnerForm, "identityNumber", e.target.value)} />
            <div className="form-navigation">
              <button type="submit" disabled={busy}>{busy ? "Saving…" : "Save owner profile"}</button>
            </div>
          </form>
        )}

        {step === "property" && (
          <>
          {properties.length > 0 && (
            <div className="saved-record-panel">
              <h2>Continue with a saved property</h2>
              <p className="muted">Select an existing property to avoid creating a duplicate record.</p>
              {properties.map(item => (
                <button type="button" className="saved-record" key={item.id} onClick={() => {
                  setProperty(item);
                  setMessage(`Using saved property ${item.propertyNumber}.`);
                  setStep("location");
                }}>
                  <span><b>{item.propertyNumber}</b><small>{item.propertyType} · {item.area} sq. units</small></span>
                  <strong>Continue</strong>
                </button>
              ))}
            </div>
          )}
          <form onSubmit={createProperty}>
            <h2>{properties.length ? "Or add another property" : "Property details"}</h2>
            <input placeholder="Property number" value={propertyForm.propertyNumber}
              onChange={e => update(setPropertyForm, "propertyNumber", e.target.value)} />
            <select value={propertyForm.propertyType}
              onChange={e => update(setPropertyForm, "propertyType", e.target.value)}>
              <option>RESIDENTIAL</option>
              <option>COMMERCIAL</option>
              <option>AGRICULTURAL</option>
            </select>
            <input type="number" placeholder="Area" value={propertyForm.area}
              onChange={e => update(setPropertyForm, "area", e.target.value)} />
            <textarea placeholder="Description" value={propertyForm.description}
              onChange={e => update(setPropertyForm, "description", e.target.value)} />
            <div className="form-navigation">
              <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
              <button type="submit" disabled={busy}>{busy ? "Saving…" : "Save property"}</button>
            </div>
          </form>
          </>
        )}

        {step === "location" && (
          <form onSubmit={createLocation}>
            <h2>Property location</h2>
            {Object.keys(locationForm).map(field => (
              <input key={field} placeholder={field} value={locationForm[field]}
                onChange={e => update(setLocationForm, field, e.target.value)} />
            ))}
            <div className="form-navigation">
              <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
              <button type="submit" disabled={busy}>{busy ? "Saving…" : "Save location"}</button>
            </div>
          </form>
        )}

        {step === "application" && (
          <form onSubmit={createApplication}>
            <h2>Choose your registration purpose</h2>
            <p className="muted">This academic workflow demonstrates preparation. Select the closest purpose and confirm the exact deed-specific requirements with the official registering office.</p>
            <select value={purpose} onChange={e => setPurpose(e.target.value)}>
              <option>Sale / transfer of property</option>
              <option>Gift deed</option>
              <option>Lease / rent agreement</option>
              <option>Mortgage / loan document</option>
              <option>Power of attorney</option>
              <option>Other registration purpose</option>
            </select>
            <textarea value={purpose}
              onChange={e => setPurpose(e.target.value)}
              placeholder="Add a short purpose or transaction note" />
            <div className="decision-note"><b>Before continuing</b><span>Confirm stamp duty, fees, witnesses, identity requirements and office-specific documents from the official source.</span></div>
            <div className="form-navigation">
              <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
              <button type="submit" disabled={busy}>{busy ? "Creating…" : "Create guided application"}</button>
            </div>
          </form>
        )}

        {step === "documents" && (
          <form onSubmit={uploadDocument}>
            <h2>Document checklist</h2>
            <p className="muted">Upload one clear synthetic demo file for each required category. Identity proof, address proof and property document are three separate uploads; one file cannot satisfy all three categories.</p>
            <div className="document-guide">
              <strong>Core workflow checklist</strong>
              <span>1. Identity proof · 2. Address proof · 3. Property document</span>
              <small>Additional documents may apply: witnesses, stamp-duty proof, tax receipts, NOC, 7/12/property card, POA or construction papers.</small>
            </div>
            <select value={documentType} onChange={e => setDocumentType(e.target.value)}>
              <option value="IDENTITY_PROOF">Identity proof</option>
              <option value="ADDRESS_PROOF">Address proof</option>
              <option value="PROPERTY_DOCUMENT">Property document</option>
              <option value="TITLE_DOCUMENT">Title / previous deed</option>
              <option value="TAX_RECEIPT">Property tax receipt</option>
              <option value="STAMP_DUTY_PROOF">Stamp duty / fee proof</option>
              <option value="NOC_OR_APPROVAL">NOC or approval</option>
              <option value="OTHER">Other supporting document</option>
            </select>
            <input type="file" accept=".pdf,.jpg,.jpeg,.png"
              onChange={e => setFile(e.target.files[0])} />
            {file && <div className="file-selected">Selected: {file.name} · {(file.size / 1024 / 1024).toFixed(2)} MB</div>}
            <button type="submit" disabled={busy}>{busy ? "Uploading…" : "Upload document for review"}</button>
            <p className="document-progress"><b>{uploadedDocuments.length}/3 required categories complete</b> · You may replace a file by uploading again under the same category.</p>
            <div className="checklist-table">
              {["IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT"].map(requiredType => {
                const uploaded = uploadedDocuments.some(document => document.documentType === requiredType);
                return <div key={requiredType} className={uploaded ? "checklist-row uploaded" : "checklist-row"}>
                  <span><b>{requiredType.replaceAll("_", " ")}</b><small>Required for demo submission</small></span>
                  <strong>{uploaded ? "Uploaded" : "Required"}</strong>
                </div>;
              })}
            </div>
            <div className="form-navigation">
              <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
              <button type="button" className="secondary-button" onClick={() => setStep("submit")} disabled={!hasRequiredDocuments}>Continue to review</button>
            </div>
            {!hasRequiredDocuments && <p className="validation-hint">Upload all three required documents to continue: identity proof, address proof and property document.</p>}
          </form>
        )}

        {step === "submit" && (
          <div>
            <h2>Review and submit</h2>
            <div className="summary">
              <p><b>Application:</b> {application?.applicationNumber}</p>
              <p><b>Property:</b> {property?.propertyNumber}</p>
              <p><b>Status:</b> {application?.status}</p>
            </div>
            <p className="muted">Required demo documents: identity proof, address proof and property document. Additional documents may be required for your transaction.</p>
            <label className="acknowledgement"><input type="checkbox" checked={submissionAcknowledged} onChange={event => setSubmissionAcknowledged(event.target.checked)} /> <span>I understand this is an academic preparation workflow, not legal approval. I will confirm the final requirements with the registering office.</span></label>
            <div className="form-navigation">
              <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
              <button onClick={submitApplication} disabled={!submissionAcknowledged || !hasRequiredDocuments || busy}>Submit application for review</button>
            </div>
          </div>
        )}

        {step === "payment" && (
          <div>
            <h2>Registration payment</h2>
            <p className="muted">Complete the ₹500 test-mode demonstration fee. No real money is charged.</p>
            <div className="summary">
              <p><b>Application:</b> {application?.applicationNumber}</p>
              <p><b>Current status:</b> {application?.status}</p>
              <p><b>Amount:</b> ₹500.00</p>
            </div>
            {!payment && <button onClick={createPayment}>Create payment order</button>}
            {payment && (
              <>
                <div className="summary">
                  <p><b>Order:</b> {payment.gatewayOrderId}</p>
                  <p><b>Gateway:</b> TEST_MODE</p>
                  <p><b>Payment status:</b> {payment.paymentStatus}</p>
                </div>
                <div className="form-navigation">
                  <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
                  <button onClick={completePayment}>Complete test payment</button>
                </div>
              </>
            )}
          </div>
        )}

        {step === "paymentComplete" && (
          <div className="complete">
            <div className="check">✓</div>
            <h2>Payment successful</h2>
            <p>Your application is ready for officer verification.</p>
            <p className="application-number">{application?.applicationNumber}</p>
            <span className="badge">PAID</span>
            <div className="summary payment-summary">
              <p><b>Amount:</b> ₹{Number(payment?.amount || 0).toFixed(2)}</p>
              <p><b>Payment status:</b> {payment?.paymentStatus}</p>
              <p><b>Signature verified:</b> {String(payment?.signatureVerified)}</p>
            </div>
          </div>
        )}

        {step === "complete" && (
          <div className="complete">
            <div className="check">✓</div>
            <h2>Application submitted</h2>
            <p>Your application is now ready for payment.</p>
            <p className="application-number">{application?.applicationNumber}</p>
            <span className="badge">SUBMITTED</span>
          </div>
        )}
      </section>
    </main>
  );
}

export default App;