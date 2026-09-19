import { useState } from "react";
import { Analytics } from "@vercel/analytics/react";
import "./App.css";

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();

const API = configuredApiUrl?.startsWith("http://") ||
  configuredApiUrl?.startsWith("https://")
  ? configuredApiUrl.replace(/\/+$/, "")
  : "https://propertysetu-backend.onrender.com";

function App() {
  const [step, setStep] = useState("home");
  const [showAbout, setShowAbout] = useState(false);
  const [loginPasswordVisible, setLoginPasswordVisible] = useState(false);
  const [purposeType, setPurposeType] = useState("");
  const [validationErrors, setValidationErrors] = useState([]);
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
  const [applicationSort, setApplicationSort] = useState("NEWEST");
  const [applicationLookup, setApplicationLookup] = useState("");
  const [paymentHistory, setPaymentHistory] = useState([]);
  const [paymentHistoryLoading, setPaymentHistoryLoading] = useState(false);
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

  // Registration-only UI state. These stay in the frontend and do not change the backend API.
  const [registerPasswordError, setRegisterPasswordError] = useState("");
  const [registerPasswordVisible, setRegisterPasswordVisible] = useState(false);
  const [registerConfirmPassword, setRegisterConfirmPassword] = useState("");
  const [registerConfirmPasswordVisible, setRegisterConfirmPasswordVisible] = useState(false);

  const [ownerForm, setOwnerForm] = useState({
    name: "",
    address: "",
    phone: "",
    identityNumber: ""
  });

  const [propertyForm, setPropertyForm] = useState({
    propertyNumber: "",
    propertyType: "",
    area: "",
    description: ""
  });

  const [locationForm, setLocationForm] = useState({
    address: "",
    city: "",
    district: "",
    state: "",
    pincode: ""
  });

  const [purpose, setPurpose] = useState("");
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
    setValidationErrors([]);
  }

  function showValidation(errors) {
    setMessage("");
    setValidationErrors(errors);
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
    setRegisterPasswordError("");
    setRegisterConfirmPassword("");
    setRegisterPasswordVisible(false);
    setRegisterConfirmPasswordVisible(false);
    setStep("home");
    clearMessages();
    sessionStorage.removeItem("property_registration_token");
  }

  async function login(event) {
    event.preventDefault();
    if (busy) return;
    clearMessages();

    const errors = [];
    if (!loginForm.username.trim()) errors.push("Enter your username.");
    if (!loginForm.password) errors.push("Enter your password.");
    if (errors.length) { showValidation(errors); return; }

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
        await loadPaymentHistory(savedApplications || []);

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
            const savedPayment = await request(`/api/payments/application/${current.id}`).catch(() => null);
            setPayment(savedPayment);
            setStep("payment");
            setMessage(savedPayment
              ? `Welcome back, ${data.username}. Your payment is ready to continue.`
              : `Welcome back, ${data.username}. Your application is ready for payment.`);
          } else {
            const savedPayment = await request(`/api/payments/application/${current.id}`).catch(() => null);
            setPayment(savedPayment);
            setStep("paymentComplete");
            setMessage(`Welcome back, ${data.username}. Your application status is ${current.status}.`);
          }
        } else if (existingOwner) {
          setStep("property");
          setMessage(`Welcome back, ${data.username}. Your saved owner profile is ready.`);
        } else {
          setStep("owner");
          setMessage(`Welcome, ${data.username}. Let’s create your owner profile.`);
        }
      }

      if (data.role === "OFFICER" || data.role === "ADMIN") {
        setMessage(`Welcome, ${data.username}. Officer workspace is ready.`);
      }
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

    const errors = [];
    if (!registerForm.username.trim()) errors.push("Enter a username.");
    if (!registerForm.email.trim()) errors.push("Enter your email address.");
    else if (!/^\S+@\S+\.\S+$/.test(registerForm.email.trim())) errors.push("Enter a valid email address.");
    if (!/^\d{10}$/.test(registerForm.phone.trim())) errors.push("Enter a 10-digit mobile number.");
    if (registerForm.password.length < 6) errors.push("Password must be at least 6 characters.");
    if (registerForm.password.length > 100) errors.push("Password cannot exceed 100 characters.");
    if (registerForm.password !== registerConfirmPassword) errors.push("Passwords do not match.");

    if (errors.length) {
      showValidation(errors);
      return;
    }

    setBusy(true);

    try {
      await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(registerForm)
      });

      setUser(null);
      sessionStorage.removeItem("property_registration_token");
      setMessage("Applicant account created successfully. Please sign in to continue.");
      setLoginForm({ username: registerForm.username, password: "" });
      setRegisterForm({ username: "", password: "", email: "", phone: "" });
      setRegisterConfirmPassword("");
      setRegisterPasswordVisible(false);
      setRegisterConfirmPasswordVisible(false);
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

    const errors = [];
    if (!ownerForm.name.trim()) errors.push("Enter the owner's full name.");
    if (!ownerForm.address.trim()) errors.push("Enter the owner's address.");
    if (!/^[0-9]{10}$/.test(ownerForm.phone.trim())) errors.push("Enter a 10-digit mobile number.");
    if (!ownerForm.identityNumber.trim()) errors.push("Enter an identity/reference number.");
    if (errors.length) { showValidation(errors); return; }

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

    const errors = [];
    if (!propertyForm.propertyNumber.trim()) errors.push("Enter a property reference number.");
    if (!propertyForm.propertyType) errors.push("Select a property type.");
    if (!propertyForm.area || Number(propertyForm.area) <= 0) errors.push("Enter an area greater than 0.");
    if (!propertyForm.description.trim()) errors.push("Add a short property description.");
    if (errors.length) { showValidation(errors); return; }

    const savedMatch = findSavedProperty(propertyForm.propertyNumber);
    if (savedMatch) {
      setProperty(savedMatch);
      setMessage("This property reference is already saved in your PropertySetu account. Use the saved record below instead of creating a duplicate.");
      setStep("property");
      return;
    }

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

    const errors = [];
    if (!locationForm.address.trim()) errors.push("Enter the property address.");
    if (!locationForm.city.trim()) errors.push("Enter the city.");
    if (!locationForm.district.trim()) errors.push("Enter the district.");
    if (!locationForm.state.trim()) errors.push("Enter the state.");
    if (!/^[0-9]{6}$/.test(locationForm.pincode.trim())) errors.push("Enter a 6-digit PIN code.");
    if (errors.length) { showValidation(errors); return; }

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

    const finalPurpose = purpose.trim() || purposeType.trim();
    if (!finalPurpose) {
      showValidation(["Select or enter the purpose of this application."]);
      return;
    }

    setBusy(true);

    try {
      const data = await request("/api/applications", {
        method: "POST",
        body: JSON.stringify({
          userId: user.id,
          propertyId: property.id,
          purpose: finalPurpose
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
    } finally {      setBusy(false);
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
      const refreshedApplications = await request("/api/applications");
      setApplicantApplications(refreshedApplications || []);
      await loadPaymentHistory(refreshedApplications || []);
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

  function normalizePropertyReference(value) {
    return String(value || "")
      .trim()
      .toLowerCase()
      .replace(/\s+/g, " ");
  }

  function findSavedProperty(reference) {
    const normalized = normalizePropertyReference(reference);
    if (!normalized) return null;
    return properties.find(item => normalizePropertyReference(item.propertyNumber) === normalized) || null;
  }

  function maskValue(value, visible = 4) {
    const text = String(value || "");
    if (!text) return "Protected";
    if (text.length <= visible) return "••••";
    return `${"•".repeat(Math.max(4, text.length - visible))}${text.slice(-visible)}`;
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

  const visibleApplications = applications
    .filter(item => {
      const matchesStatus = statusFilter === "ALL" || item.status === statusFilter;
      const query = applicationSearch.trim().toLowerCase();
      const matchesSearch = !query
        || item.applicationNumber?.toLowerCase().includes(query)
        || item.applicantUsername?.toLowerCase().includes(query)
        || item.propertyNumber?.toLowerCase().includes(query);
      return matchesStatus && matchesSearch;
    })
    .sort((left, right) => {
      if (applicationSort === "OLDEST") {
        return new Date(left.createdAt || 0) - new Date(right.createdAt || 0);
      }
      if (applicationSort === "STATUS") {
        return String(left.status || "").localeCompare(String(right.status || ""));
      }
      return new Date(right.createdAt || 0) - new Date(left.createdAt || 0);
    });

  const propertyReferenceMatch = findSavedProperty(propertyForm.propertyNumber);

  async function loadAuditHistory(applicationId) {
    try {
      setAuditEvents(await request(`/api/audit/applications/${applicationId}`));
    } catch (err) {
      setAuditEvents([]);
      setError(err.message);
    }
  }

  async function selectApplicantApplication(selected) {
    if (!selected) return;
    setApplication(selected);
    const savedProperty = properties.find(item => item.id === selected.propertyId);
    if (savedProperty) {
      setProperty(savedProperty);
    }
    setPurpose(selected.purpose || "");

    try {
      const [documents, savedPayment] = await Promise.all([
        request(`/api/applications/${selected.id}/documents`).catch(() => []),
        request(`/api/payments/application/${selected.id}`).catch(() => null)
      ]);
      setUploadedDocuments(documents || []);
      setPayment(savedPayment);

      if (selected.status === "DRAFT") {
        setStep("documents");
      } else if (["SUBMITTED", "PAYMENT_PENDING"].includes(selected.status)) {
        setStep("payment");
      } else if (["PAID", "UNDER_VERIFICATION", "VERIFIED", "COMPLETED"].includes(selected.status)) {
        setStep("paymentComplete");
      } else {
        setStep("payment");
      }
    } catch (err) {
      setError(err.message);
    }
  }

  async function refreshApplicantApplications() {
    clearMessages();
    try {
      const data = await request("/api/applications");
      setApplicantApplications(data || []);
      await loadPaymentHistory(data || []);
      const current = application && data.find(item => item.id === application.id);
      const selected = current || (!application
        ? [...(data || [])].sort((left, right) => new Date(right.createdAt || 0) - new Date(left.createdAt || 0))[0]
        : null);
      if (selected) {
        await selectApplicantApplication(selected);
      }
      setMessage("Application status and saved records refreshed.");
    } catch (err) {
      setError(err.message);
    }
  }

  async function loadPaymentHistory(records = applicantApplications) {
    if (!records?.length) {
      setPaymentHistory([]);
      return;
    }

    setPaymentHistoryLoading(true);
    try {
      const results = await Promise.all(
        records.slice(0, 12).map(async record => {
          const paymentRecord = await request(`/api/payments/application/${record.id}`).catch(() => null);
          return paymentRecord ? { ...paymentRecord, application: record } : null;
        })
      );
      setPaymentHistory(results.filter(Boolean));
    } finally {
      setPaymentHistoryLoading(false);
    }
  }

  function applicationProgress() {
    if (step === "paymentComplete") return 7;
    const index = ["owner", "property", "location", "application", "documents", "submit", "payment"].indexOf(step);
    return index < 0 ? 0 : index + 1;
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

  if (step === "home" && !officerMode) {
    return (
      <main className="app public-home">
        <header className="site-nav">
          <button className="brand-button" type="button" onClick={() => setStep("home")} aria-label="PropertySetu home">
            <span className="brand-mark">P</span>
            <span><b>PropertySetu</b><small>Academic project</small></span>
          </button>
          <div className="nav-actions">
            <button className="nav-link" type="button" onClick={() => setShowAbout(previous => !previous)}>{showAbout ? "Hide overview" : "How it works"}</button>
            <button className="nav-ghost" type="button" onClick={() => { clearMessages(); setStep("login"); }}>Applicant sign in</button>
          </div>
        </header>

        <section className="hero-home">
          <div className="hero-home-copy">
            <div className="academic-badge">STUDENT-DEVELOPED · ACADEMIC DEMONSTRATION</div>
            <p className="eyebrow">PROPERTYSETU</p>
            <h1>Property application, guided from start to review.</h1>
            <p className="hero-lead">A property is more than a record. PropertySetu connects applicant information, property details, supporting documents, review and test-mode payment into one clear, traceable journey.</p>
            <div className="hero-actions">
              <button type="button" onClick={() => { clearMessages(); setStep("login"); }}>Explore the workflow <span>→</span></button>
              <button type="button" className="secondary-button" onClick={() => setShowAbout(true)}>See how it works</button>
            </div>
            <div className="trust-strip"><span>✓ JWT authentication</span><span>✓ Role-based access</span><span>✓ Document workflow</span><span>✓ Test-mode payment</span></div>
          </div>
          <div className="hero-home-visual">
            <img src="/propertysetu-hero.svg" alt="Illustration representing a guided property application workflow" />
          </div>
        </section>

        <section className="academic-notice" aria-label="Academic project notice">
          <div className="notice-icon">i</div>
          <div>
            <strong>Academic demonstration</strong>
            <p>PropertySetu is a student-developed academic project. It is <b>not an official Government of Maharashtra, IGR Maharashtra, or government registration portal.</b> No official government registration is performed through this application.</p>
          </div>
        </section>

        <section className="workflow-showcase">
          <div className="section-heading"><p className="eyebrow">ONE CONNECTED WORKFLOW</p><h2>From account creation to application review</h2><p>Each stage builds on the information from the previous stage instead of making the user repeat the same details.</p></div>
          <div className="workflow-grid">
            {[
              ["01", "Applicant", "Create an account and manage your own application records."],
              ["02", "Property", "Capture owner, property and location information."],
              ["03", "Documents", "Organize supporting files by document category."],
              ["04", "Review", "Submit the application and follow its status."],
              ["05", "Payment", "Demonstrate an application-related fee in test mode."],
              ["06", "Officer", "Authorized users review documents and record a decision."]
            ].map(([number, title, description]) => (
              <article className="workflow-card" key={number}><span>{number}</span><h3>{title}</h3><p>{description}</p></article>
            ))}
          </div>
        </section>

        <section className="feature-row">
          <article><b>Authentication</b><span>JWT sessions with role-aware access.</span></article>
          <article><b>CRUD workflow</b><span>Applicant records, properties and application data.</span></article>
          <article><b>Search · Sort · Filter</b><span>Officer-side application discovery and review.</span></article>
          <article><b>Payment Gateway</b><span>Test-mode application payment flow.</span></article>
        </section>

        {showAbout && <section className="public-overview home-overview">
          <div><p className="eyebrow">WHAT THIS PROJECT DEMONSTRATES</p><h2>A realistic full-stack classroom workflow.</h2><p>PropertySetu follows the class-project scope: User → Owner → Property → Location → RegistrationApplication → Document → Verification → Payment. Optional email and forgot-password features are intentionally not presented as working features until corresponding backend endpoints exist.</p></div>
          <div className="overview-grid"><div><b>01</b><span>Prepare</span><small>Applicant and property information</small></div><div><b>02</b><span>Submit</span><small>Documents, acknowledgement and payment</small></div><div><b>03</b><span>Review</span><small>Officer verification and audit history</small></div></div>
        </section>}

        <footer className="public-footer"><span>PropertySetu · Student academic project</span><span>Test-mode payment · No official registration</span></footer>
      </main>
    );
  }

  if (step === "login" && !officerMode) {
    return (
      <main className="app auth-page">
        <header className="site-nav">
          <button className="brand-button" type="button" onClick={() => setStep("home")} aria-label="PropertySetu home">
            <span className="brand-mark">P</span><span><b>PropertySetu</b><small>Academic project</small></span>
          </button>
          <button className="nav-link" type="button" onClick={() => setStep("home")}>← Back to home</button>
        </header>

        <section className="login-intro">
          <div><p className="eyebrow">APPLICANT ACCESS</p><h1>Sign in to your application workspace.</h1><p>Continue your academic PropertySetu workflow or create a new applicant account.</p></div>
          <div className="mini-notice"><strong>Academic demonstration</strong><span>Not an official government registration portal.</span></div>
        </section>

        {message && <div className="message success" role="status">{message}</div>}
        {validationErrors.length > 0 && <div className="validation-summary" role="alert"><strong>There is a problem</strong><ul>{validationErrors.map((item, index) => <li key={index}>{item}</li>)}</ul></div>}
        {error && <div className="message error" role="alert">{error}</div>}

        <section className="card auth-card">
          <form onSubmit={login}>
            <div className="form-section-heading"><span className="step-icon">01</span><div><h2>Applicant sign in</h2><p className="muted">Use the credentials for your PropertySetu account.</p></div></div>
            <label htmlFor="login-username">Username <span>*</span></label>
            <input id="login-username" name="username" autoComplete="username" placeholder="" value={loginForm.username} onChange={e => update(setLoginForm, "username", e.target.value)} required />
            <small className="field-example">Example: rahul.sharma</small>
            <label htmlFor="login-password">Password <span>*</span></label>
            <div className="password-field"><input id="login-password" name="password" autoComplete="current-password" type={loginPasswordVisible ? "text" : "password"} placeholder="" value={loginForm.password} onChange={e => update(setLoginForm, "password", e.target.value)} required /><button type="button" className="password-toggle" onClick={() => setLoginPasswordVisible(previous => !previous)}>{loginPasswordVisible ? "Hide" : "Show"}</button></div>
            <small className="field-example">Enter the password you created for your account.</small>
            <button type="submit" disabled={busy}>{busy ? "Signing in…" : "Sign in"}</button>
          </form>
          <div className="auth-trust-row"><span>🔒 JWT-protected session</span><span>👤 Multi-user access</span><span>✓ Role-based workspace</span></div>
          <div className="auth-divider">New applicant?</div>
          <button className="secondary-button full-width" type="button" onClick={() => {
            clearMessages();
            setRegisterPasswordError("");
            setRegisterConfirmPassword("");
            setRegisterPasswordVisible(false);
            setRegisterConfirmPasswordVisible(false);
            setStep("register");
          }}>Create an applicant account</button>
        </section>

        <div className="public-footer"><span>PropertySetu · Academic demonstration</span><span>Do not use real identity documents in the demo.</span></div>
      </main>
    );
  }

  if (step === "register" && !officerMode) {
    return (
      <main className="app auth-page">
        <header className="site-nav">
          <button className="brand-button" type="button" onClick={() => setStep("home")} aria-label="PropertySetu home">
            <span className="brand-mark">P</span>
            <span><b>PropertySetu</b><small>Academic project</small></span>
          </button>
          <button className="nav-link" type="button" onClick={() => { clearMessages(); setStep("login"); }}>← Back to sign in</button>
        </header>

        <section className="login-intro">
          <div>
            <p className="eyebrow">APPLICANT ONBOARDING</p>
            <h1>Create your PropertySetu applicant account.</h1>
            <p>One account gives you a place to prepare your academic property application, upload demo documents and follow its existing review status.</p>
          </div>
          <div className="mini-notice"><strong>Academic demonstration</strong><span>Use demo information only. This is not an official government registration portal.</span></div>
        </section>

        {message && <div className="message success" role="status">{message}</div>}
        {validationErrors.length > 0 && <div className="validation-summary" role="alert"><strong>There is a problem</strong><ul>{validationErrors.map((item, index) => <li key={index}>{item}</li>)}</ul></div>}
        {error && <div className="message error" role="alert">{error}</div>}

        <section className="card auth-card">
          <form onSubmit={register} aria-busy={busy}>
            <div className="form-section-heading"><span className="step-icon">01</span><div><h2>Applicant account</h2><p className="muted">Create your sign-in details first. The property workflow comes after successful sign-in.</p></div></div>
            <div className="decision-note"><b>Before you begin</b><span>Use an email and mobile number you can recognize. Keep real identity documents out of this academic demo.</span></div>

            <div className="field-grid two">
              <div className="field">
                <label htmlFor="register-username">Username <span>*</span></label>
                <input id="register-username" name="username" autoComplete="username" value={registerForm.username} onChange={e => update(setRegisterForm, "username", e.target.value)} required />
                <small className="field-example">Example: rahul.sharma</small>
              </div>
              <div className="field">
                <label htmlFor="register-email">Email <span>*</span></label>
                <input id="register-email" name="email" type="email" autoComplete="email" value={registerForm.email} onChange={e => update(setRegisterForm, "email", e.target.value)} required />
                <small className="field-example">Example: rahul.sharma@example.com</small>
              </div>
            </div>

            <div className="field-grid two">
              <div className="field">
                <label htmlFor="register-phone">Mobile number <span>*</span></label>
                <input id="register-phone" name="phone" type="tel" inputMode="numeric" autoComplete="tel" maxLength={10} value={registerForm.phone} onChange={e => update(setRegisterForm, "phone", e.target.value.replace(/\D/g, ""))} required />
                <small className="field-example">Example: 9876543210</small>
              </div>
              <div className="field">
                <label htmlFor="register-password">Password <span>*</span></label>
                <div className="password-field">
                  <input id="register-password" name="password" autoComplete="new-password" type={registerPasswordVisible ? "text" : "password"} value={registerForm.password} minLength={6} maxLength={100} onChange={e => { const value = e.target.value; update(setRegisterForm, "password", value); if (value.length > 100) setRegisterPasswordError("Password cannot exceed 100 characters."); else if (value.length > 0 && value.length < 6) setRegisterPasswordError("Password must be at least 6 characters."); else if (registerConfirmPassword && value !== registerConfirmPassword) setRegisterPasswordError("Passwords do not match."); else setRegisterPasswordError(""); }} required />
                  <button type="button" className="password-toggle" onClick={() => setRegisterPasswordVisible(previous => !previous)}>{registerPasswordVisible ? "Hide" : "Show"}</button>
                </div>
                <small className="field-example">Use 6–100 characters · {registerForm.password.length}/100</small>
              </div>
            </div>

            <div className="password-check"><b>Password requirements</b><span className={registerForm.password.length >= 6 ? "valid" : ""}>✓ At least 6 characters</span><span className={registerForm.password.length <= 100 ? "valid" : ""}>✓ Maximum 100 characters</span></div>

            <div className="field">
              <label htmlFor="register-confirm-password">Confirm password <span>*</span></label>
              <div className="password-field">
                <input id="register-confirm-password" name="confirmPassword" autoComplete="new-password" type={registerConfirmPasswordVisible ? "text" : "password"} value={registerConfirmPassword} maxLength={100} onChange={e => { const value = e.target.value; setRegisterConfirmPassword(value); if (value && registerForm.password !== value) setRegisterPasswordError("Passwords do not match."); else if (registerForm.password.length > 0 && registerForm.password.length < 6) setRegisterPasswordError("Password must be at least 6 characters."); else setRegisterPasswordError(""); }} required />
                <button type="button" className="password-toggle" onClick={() => setRegisterConfirmPasswordVisible(previous => !previous)}>{registerConfirmPasswordVisible ? "Hide" : "Show"}</button>
              </div>
              <small className={`field-example ${registerConfirmPassword && registerForm.password === registerConfirmPassword ? "match" : ""}`}>{registerConfirmPassword && registerForm.password === registerConfirmPassword ? "✓ Passwords match" : "Example: re-enter the same password"}</small>
            </div>

            {registerPasswordError && <p className="validation-hint" role="alert">{registerPasswordError}</p>}

            <button type="submit" disabled={busy || registerForm.password.length < 6 || registerForm.password.length > 100 || registerForm.password !== registerConfirmPassword}>
              {busy ? "Creating account…" : "Create applicant account"}
            </button>
            <button type="button" className="secondary-button" onClick={() => { clearMessages(); setRegisterPasswordError(""); setRegisterConfirmPassword(""); setRegisterPasswordVisible(false); setRegisterConfirmPasswordVisible(false); setStep("login"); }} disabled={busy}>Back to sign in</button>
          </form>

          <div className="auth-trust-row"><span>🔒 Protected sign-in session</span><span>👤 Applicant workspace</span><span>✓ Guided workflow</span></div>
        </section>

        <div className="public-footer"><span>PropertySetu · Academic demonstration</span><span>No official registration is performed here.</span></div>
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
            <select value={applicationSort} onChange={e => setApplicationSort(e.target.value)} aria-label="Sort applications">
              <option value="NEWEST">Newest first</option>
              <option value="OLDEST">Oldest first</option>
              <option value="STATUS">Sort by status</option>
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
                    setError(err.message);                  }
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
    <>
      <main className="app">
      <header className="header">
        <div>
          <p className="eyebrow">PROPERTYSETU · ACADEMIC APPLICATION WORKFLOW</p>
          <h1>Complete your application step by step.</h1>
          <p className="subtitle">
            Property details, supporting documents, review and test-mode payment in one guided workspace.
          </p>
        </div>
        <div>
          <div className="status">{user ? `${user.username} · Applicant` : "Applicant workspace"}</div>
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
              <button type="button" className={application?.id === item.id ? "applicant-application active" : "applicant-application"} key={item.id} onClick={() => selectApplicantApplication(item)}>
                <span><b>{item.applicationNumber}</b><small>{item.propertyNumber || "Property record"}</small></span>
                <strong>{item.status}</strong>
              </button>
            ))}
          </div>
        </section>
      )}

      <section className="card application-history-panel">
        <div className="timeline-heading">
          <div>
            <p className="eyebrow">APPLICATION HISTORY</p>
            <h3>Find an application or check a payment</h3>
            <p className="muted">Search the application records available to your signed-in account by registration/application number or property reference.</p>
          </div>
          <span>{applicantApplications.length} record{applicantApplications.length === 1 ? "" : "s"}</span>
        </div>
        <div className="field">
          <label htmlFor="application-lookup">Registration / application number</label>
          <input id="application-lookup" value={applicationLookup} onChange={event => setApplicationLookup(event.target.value)} placeholder="Example: REG-89BF4792" />
          <small className="field-example">Your search is limited to applications accessible to your account.</small>
        </div>
        <div className="applicant-application-list">
          {applicantApplications.filter(item => {
            const query = applicationLookup.trim().toLowerCase();
            return !query || item.applicationNumber?.toLowerCase().includes(query) || item.propertyNumber?.toLowerCase().includes(query);
          }).map(item => (
            <button type="button" className={application?.id === item.id ? "applicant-application active" : "applicant-application"} key={`lookup-${item.id}`} onClick={() => selectApplicantApplication(item)}>
              <span><b>{item.applicationNumber}</b><small>{item.propertyNumber || "Property record"}</small></span>
              <strong>{item.status}</strong>
            </button>
          ))}
        </div>
        {applicantApplications.length > 0 && applicantApplications.filter(item => {
          const query = applicationLookup.trim().toLowerCase();
          return !query || item.applicationNumber?.toLowerCase().includes(query) || item.propertyNumber?.toLowerCase().includes(query);
        }).length === 0 && <p className="muted">No matching application was found in this account.</p>}
      </section>

      <section className="card application-history-panel">
        <div className="timeline-heading">
          <div>
            <p className="eyebrow">PAYMENT HISTORY</p>
            <h3>Recent payment records</h3>
            <p className="muted">Payment information is loaded from the existing PropertySetu payment records.</p>
          </div>
          <button type="button" className="secondary-button" onClick={() => loadPaymentHistory(applicantApplications)} disabled={paymentHistoryLoading}>
            {paymentHistoryLoading ? "Checking…" : "Refresh payments"}
          </button>
        </div>
        {paymentHistoryLoading && <p className="muted">Checking recent application payment records…</p>}
        {!paymentHistoryLoading && paymentHistory.length === 0 && <p className="muted">No payment record is available yet.</p>}
        {!paymentHistoryLoading && paymentHistory.length > 0 && (
          <div className="applicant-application-list">
            {paymentHistory.slice(0, 8).map(item => (
              <button type="button" className="applicant-application" key={`payment-${item.id || item.application?.id}` } onClick={() => selectApplicantApplication(item.application)}>
                <span>
                  <b>{item.application?.applicationNumber || "Application"}</b>
                  <small>₹{Number(item.amount || 0).toFixed(2)} · {item.paymentStatus || "UNKNOWN"}</small>
                </span>
                <strong>{item.signatureVerified ? "VERIFIED" : item.paymentStatus || "PENDING"}</strong>
              </button>
            ))}
          </div>
        )}
      </section>

      {property && (
        <section className="property-identity-card" aria-label="Current property record">
          <div className="property-identity-main">
            <div className="property-id-icon">⌂</div>
            <div>
              <p className="eyebrow">PROPERTYSETU PROPERTY RECORD</p>
              <h3>{property.propertyNumber || "Saved property"}</h3>
              <p className="muted">This is the property record currently connected to your application. Your account controls access to its details.</p>
            </div>
          </div>
          <div className="property-identity-grid">
            <span><small>Type</small><b>{property.propertyType || "Not specified"}</b></span>
            <span><small>Area</small><b>{property.area ? `${property.area} sq. units` : "Not specified"}</b></span>
            <span><small>Owner reference</small><b>{maskValue(owner?.identityNumber, 4)}</b></span>
            <span><small>Application</small><b>{application?.applicationNumber || "Not started"}</b></span>
          </div>
        </section>
      )}

      <section className="progress-shell" aria-label="Application progress">
        <div className="progress-heading"><div><p className="eyebrow">APPLICATION PROGRESS</p><h2>{applicationProgress() === 7 ? "Application complete" : `Step ${applicationProgress()} of 7`}</h2></div><span>{Math.round((applicationProgress() / 7) * 100)}%</span></div>
        <div className="progress-bar"><span style={{ width: `${Math.round((applicationProgress() / 7) * 100)}%` }} /></div>
        <div className="compact-progress">
          {["Owner", "Property", "Location", "Application", "Documents", "Review", "Payment"].map((label, index) => {
            const currentIndex = applicationProgress() - 1;
            return <span key={label} className={index < currentIndex || applicationProgress() === 7 ? "done" : index === currentIndex ? "current" : "upcoming"}><i>{index < currentIndex || applicationProgress() === 7 ? "✓" : index + 1}</i>{label}</span>;
          })}
        </div>
      </section>

      <section className="academic-notice compact-notice"><div className="notice-icon">i</div><div><strong>Academic demonstration</strong><p>This is a student project, not an official government registration portal. Payment is test-mode only.</p></div></section>

      {message && <div className="message success" role="status">{message}</div>}
      {validationErrors.length > 0 && <div className="validation-summary" role="alert"><strong>There is a problem</strong><ul>{validationErrors.map((item, index) => <li key={index}>{item}</li>)}</ul></div>}
      {error && <div className="message error" role="alert">{error}</div>}

      <section className="readiness-card">
        <div>
          <p className="eyebrow">APPLICATION READINESS</p>
          <h3>{applicationProgress()} of 7 sections complete</h3>
          <p className="muted">{applicationProgress() === 7 ? "Your application and test-mode payment are complete." : "Complete each section in order. Your saved records remain available after you sign in again."}</p>
        </div>
        <div className="readiness-steps">
          {["Owner", "Property", "Location", "Application", "Documents", "Review", "Payment"].map((label, index) => {
            const currentIndex = applicationProgress() - 1;
            const complete = applicationProgress() === 7 || index < currentIndex;
            return <span key={label} className={complete ? "ready" : index === currentIndex ? "active" : "future"}><i>{complete ? "✓" : index + 1}</i>{label}</span>;
          })}
        </div>
      </section>

      <section className="card">
        {step === "owner" && (
          <form onSubmit={createOwner}>
            <div className="form-section-heading"><span className="step-icon">01</span><div><h2>Applicant information</h2><p className="muted">Tell us about the owner associated with this application.</p></div></div>
            <div className="field-grid two">
              <div className="field"><label htmlFor="owner-name">Full name <span>*</span></label><input id="owner-name" placeholder="" value={ownerForm.name} onChange={e => update(setOwnerForm, "name", e.target.value)} required /><small className="field-example">Example: Rahul Sharma</small></div>
              <div className="field"><label htmlFor="owner-phone">Mobile number <span>*</span></label><input id="owner-phone" type="tel" inputMode="numeric" maxLength={10} placeholder="" value={ownerForm.phone} onChange={e => update(setOwnerForm, "phone", e.target.value.replace(/\D/g, ""))} required /><small className="field-example">Example: 9876543210</small></div>
            </div>
            <div className="field"><label htmlFor="owner-address">Address <span>*</span></label><textarea id="owner-address" placeholder="" value={ownerForm.address} onChange={e => update(setOwnerForm, "address", e.target.value)} required /><small className="field-example">Example: 42, Lakeview Avenue, Sector 15</small></div>
            <div className="field"><label htmlFor="owner-identity">Identity/reference number <span>*</span></label><input id="owner-identity" placeholder="" value={ownerForm.identityNumber} onChange={e => update(setOwnerForm, "identityNumber", e.target.value)} required /><small className="field-example">Example: ID-2026-001</small></div>
            <div className="form-navigation"><span className="step-helper">Your information is used only for this application workflow.</span><button type="submit" disabled={busy}>{busy ? "Saving applicant…" : "Save & continue"}</button></div>
          </form>
        )}

        {step === "property" && (
          <>
            {owner && <div className="owner-found-panel">
              <div className="owner-found-icon">✓</div>
              <div>
                <p className="eyebrow">OWNER PROFILE FOUND</p>
                <h3>Welcome back — your owner profile is already saved.</h3>
                <p className="muted">We found your existing profile and loaded it for this workflow. You do not need to create another owner record.</p>
                <div className="owner-found-meta"><span><small>Name</small><b>{owner.name || "Protected"}</b></span><span><small>Mobile</small><b>{maskValue(owner.phone, 4)}</b></span><span><small>Identity</small><b>{maskValue(owner.identityNumber, 4)}</b></span></div>
              </div>
            </div>}
            {properties.length > 0 && <div className="saved-record-panel"><div><p className="eyebrow">SAVED PROPERTY RECORDS</p><h2>Continue with a saved property</h2><p className="muted">Choose a property you already saved instead of creating another record.</p></div>{properties.map(item => <button type="button" className="saved-record" key={item.id} onClick={() => { setProperty(item); setMessage(`Using saved property ${item.propertyNumber}.`); setStep("location"); }}><span><b>{item.propertyNumber}</b><small>{item.propertyType} · {item.area} sq. units</small></span><strong>Continue →</strong></button>)}</div>}
            {propertyReferenceMatch && <div className="duplicate-awareness-panel" role="status">
              <div className="duplicate-awareness-icon">✓</div>
              <div>
                <strong>Existing property record detected in your account</strong>
                <span><b>{propertyReferenceMatch.propertyNumber}</b> · {propertyReferenceMatch.propertyType || "Property"} · {propertyReferenceMatch.area || "—"} sq. units</span>
                <small>To keep your records clean, continue with the saved property instead of creating a duplicate.</small>
              </div>
              <button type="button" onClick={() => { setProperty(propertyReferenceMatch); setMessage(`Using saved property ${propertyReferenceMatch.propertyNumber}.`); setStep("location"); }}>Use saved record</button>
            </div>}
            <form onSubmit={createProperty>
              <div className="form-section-heading"><span className="step-icon">02</span><div><h2>Property details</h2><p className="muted">Add the property information for this application.</p></div></div>
              <div className="field-grid two">
                <div className="field"><label htmlFor="property-number">Property reference <span>*</span></label><input id="property-number" placeholder="" value={propertyForm.propertyNumber} onChange={e => update(setPropertyForm, "propertyNumber", e.target.value)} required /><small className="field-example">Example: PROP-2026-001</small>{propertyForm.propertyNumber.trim() && !propertyReferenceMatch && <small className="record-check clear">No matching property reference was found in your saved account records.</small>}{propertyReferenceMatch && <small className="record-check match">✓ Matching saved property found — use the existing record above.</small>}</div>
                <div className="field"><label htmlFor="property-type">Property type <span>*</span></label><select id="property-type" value={propertyForm.propertyType} onChange={e => update(setPropertyForm, "propertyType", e.target.value)} required><option value="">Select property type</option><option value="RESIDENTIAL">Residential</option><option value="COMMERCIAL">Commercial</option><option value="AGRICULTURAL">Agricultural</option></select><small className="field-example">Example: Residential</small></div>
              </div>
              <div className="field-grid two">
                <div className="field"><label htmlFor="property-area">Area <span>*</span></label><input id="property-area" type="number" min="1" step="0.01" placeholder="" value={propertyForm.area} onChange={e => update(setPropertyForm, "area", e.target.value)} required /><small className="field-example">Example: 1200 sq. ft.</small></div>
                <div className="field"><label htmlFor="property-description">Description <span>*</span></label><input id="property-description" placeholder="" value={propertyForm.description} onChange={e => update(setPropertyForm, "description", e.target.value)} required /><small className="field-example">Example: 2 BHK residential apartment with parking.</small></div>
              </div>
              <div className="form-navigation"><button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button><button type="submit" disabled={busy}>{busy ? "Saving property…" : "Save & continue"}</button></div>
            </form>
          </>
        )}

        {step === "location" && (
          <form onSubmit={createLocation}>
            <div className="form-section-heading"><span className="step-icon">03</span><div><h2>Property location</h2><p className="muted">Enter the address where the property is located.</p></div></div>
            <div className="field"><label htmlFor="location-address">Address <span>*</span></label><textarea id="location-address" placeholder="" value={locationForm.address} onChange={e => update(setLocationForm, "address", e.target.value)} required /><small className="field-example">Example: 42, Lakeview Avenue, Sector 15</small></div>
            <div className="field-grid two">
              <div className="field"><label htmlFor="location-city">City <span>*</span></label><input id="location-city" placeholder="" value={locationForm.city} onChange={e => update(setLocationForm, "city", e.target.value)} required /><small className="field-example">Example: Nashik</small></div>
              <div className="field"><label htmlFor="location-district">District <span>*</span></label><input id="location-district" placeholder="" value={locationForm.district} onChange={e => update(setLocationForm, "district", e.target.value)} required /><small className="field-example">Example: Nashik</small></div>
              <div className="field"><label htmlFor="location-state">State <span>*</span></label><input id="location-state" placeholder="" value={locationForm.state} onChange={e => update(setLocationForm, "state", e.target.value)} required /><small className="field-example">Example: Maharashtra</small></div>
              <div className="field"><label htmlFor="location-pincode">PIN code <span>*</span></label><input id="location-pincode" inputMode="numeric" maxLength={6} placeholder="" value={locationForm.pincode} onChange={e => update(setLocationForm, "pincode", e.target.value.replace(/\D/g, ""))} required /><small className="field-example">Example: 422001</small></div>
            </div>
            <div className="address-preview"><span>Address preview</span><strong>{[locationForm.address, locationForm.city, locationForm.district, locationForm.state, locationForm.pincode].filter(Boolean).join(", ") || "Your completed property address will appear here."}</strong>{Object.values(locationForm).every(Boolean) && <small>✓ Address details complete</small>}</div>
            <div className="form-navigation"><button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button><button type="submit" disabled={busy}>{busy ? "Saving location…" : "Save & continue"}</button></div>
          </form>
        )}

        {step === "application" && (
          <form onSubmit={createApplication}>
            <div className="form-section-heading"><span className="step-icon">04</span><div><h2>Application details</h2><p className="muted">Choose the closest purpose for this academic demonstration.</p></div></div>
            <div className="field"><label htmlFor="purpose-type">Application purpose <span>*</span></label><select id="purpose-type" value={purposeType} onChange={e => setPurposeType(e.target.value)} required><option value="">Select a purpose</option><option value="Sale / transfer of property">Sale / transfer of property</option><option value="Gift deed">Gift deed</option><option value="Lease / rent agreement">Lease / rent agreement</option><option value="Mortgage / loan document">Mortgage / loan document</option><option value="Power of attorney">Power of attorney</option><option value="Other registration purpose">Other registration purpose</option></select><small className="field-example">Example: Sale / transfer of property</small></div>
            <div className="field"><label htmlFor="purpose-note">Application note</label><textarea id="purpose-note" placeholder="" value={purpose} onChange={e => setPurpose(e.target.value)} maxLength={500} /><small className="field-example">Example: Application for transfer of a residential property.</small></div>
            <div className="decision-note"><b>Before you continue</b><span>PropertySetu is demonstrating the workflow. Confirm transaction-specific requirements, fees, witnesses and documents with the appropriate official source.</span></div>
            <div className="form-navigation"><button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button><button type="submit" disabled={busy}>{busy ? "Creating application…" : "Create application"}</button></div>
          </form>
        )}

        {step === "documents" && (
          <form onSubmit={uploadDocument}>
            <div className="form-section-heading"><span className="step-icon">05</span><div><h2>Supporting documents</h2><p className="muted">Add clear demo files to the categories used by this workflow.</p></div></div>
            <p className="muted">Use synthetic/demo files only. Identity proof, address proof and property document are three separate uploads; one file cannot satisfy all three categories.</p>
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
            <div className="form-section-heading"><span className="step-icon">06</span><div><h2>Review before submission</h2><p className="muted">Check the summary before sending the application for review.</p></div></div>
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
            <div className="form-section-heading"><span className="step-icon">07</span><div><h2>Test-mode payment</h2><p className="muted">Demonstration payment only — no real money is charged.</p></div></div>
            <p className="muted">Complete the ₹500 test-mode demonstration fee. No real money is charged.</p>
            <div className="summary">
              <p><b>Application:</b> {application?.applicationNumber}</p>
              <p><b>Current status:</b> {application?.status}</p>
              <p><b>Amount:</b> ₹500.00</p>
            </div>
            {!payment && <button onClick={createPayment} disabled={busy}>{busy ? "Creating payment order…" : "Create payment order"}</button>}
            {payment && (
              <>
                <div className="summary">
                  <p><b>Order:</b> {payment.gatewayOrderId}</p>
                  <p><b>Gateway:</b> TEST_MODE</p>
                  <p><b>Payment status:</b> {payment.paymentStatus}</p>
                </div>
                <div className="form-navigation">
                  <button type="button" className="secondary-button" onClick={goToPreviousStep}>Previous</button>
                  <button onClick={completePayment} disabled={busy}>{busy ? "Processing…" : "Complete test payment"}</button>
                </div>
              </>
            )}
          </div>
        )}

        {step === "paymentComplete" && (
          <div className="complete">
            <div className="check">✓</div>
            <h2>Application payment recorded</h2>
            <p>Your test-mode payment has been confirmed. The application is now available for the existing officer-review workflow.</p>
            <p className="application-number">{application?.applicationNumber}</p>
            <span className="badge">PAID</span>
            <div className="summary payment-summary">
              <p><b>Amount:</b> ₹{Number(payment?.amount || 0).toFixed(2)}</p>
              <p><b>Payment status:</b> {payment?.paymentStatus}</p>
              <p><b>Signature verified:</b> {String(payment?.signatureVerified)}</p>
              <p><b>Reference:</b> {application?.applicationNumber}</p>
            </div>
            <div className="decision-note">
              <b>Keep your application reference</b>
              <span>After signing in again, you can use Application History and Payment History above to find this record and confirm its payment status.</span>
            </div>
            <div className="form-navigation">
              <button type="button" className="secondary-button" onClick={refreshApplicantApplications}>Refresh application history</button>
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
      <Analytics />
    </>
  );
}

export default App;