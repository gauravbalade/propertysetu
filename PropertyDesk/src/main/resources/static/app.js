const API = "/api/v1";
const state = { owners: [], properties: [], locations: [], visits: [] };
const byId = (id) => document.getElementById(id);
const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#39;" }[char]));
function notice(message, type = "success") {
  const box = byId("notice");
  box.textContent = message;
  box.className = "notice " + type;
  box.hidden = false;
  window.clearTimeout(notice.timer);
  notice.timer = window.setTimeout(() => { box.hidden = true; }, 5500);
}
async function request(path, options = {}) {
  const response = await fetch(API + path, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options.headers || {}) }
  });
  let body;
  try { body = await response.json(); } catch { body = { message: "The server returned an unreadable response." }; }
  if (!response.ok) throw new Error(body.message || ("Request failed (" + response.status + ")"));
  return body.data;
}
function dateToday() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}
function badge(status) {
  const safe = escapeHtml(status || "UNKNOWN");
  return '<span class="pill pill-' + safe.toLowerCase().replace(/[^a-z_]/g, "") + '">' + safe.replaceAll("_", " ") + '</span>';
}
function fillSelect(id, rows, labelFor, emptyLabel) {
  const select = byId(id);
  const oldValue = select.value;
  select.innerHTML = '<option value="">' + escapeHtml(emptyLabel) + '</option>' + rows.map(row =>
    '<option value="' + row.id + '">' + escapeHtml(labelFor(row)) + '</option>').join("");
  if (rows.some(row => String(row.id) === oldValue)) select.value = oldValue;
}
function renderOwners() {
  byId("ownerCount").textContent = state.owners.length;
  byId("ownersTable").innerHTML = state.owners.length ? state.owners.map(owner =>
    '<tr><td>#' + owner.id + '</td><td><div class="cell-title">' + escapeHtml(owner.fullName) + '</div></td><td>' + escapeHtml(owner.email) + '</td><td>' + escapeHtml(owner.phone) + '</td></tr>'
  ).join("") : '<tr><td colspan="4" class="empty">No owners yet. Add the first owner above.</td></tr>';
  fillSelect("ownerSelect", state.owners, owner => owner.fullName + " · #" + owner.id, "Choose an owner");
}
function renderProperties() {
  byId("propertyCount").textContent = state.properties.length;
  byId("registeredCount").textContent = state.properties.filter(p => p.status === "REGISTERED").length;
  byId("propertiesTable").innerHTML = state.properties.length ? state.properties.map(property => {
    const owner = property.owner || {};
    return '<tr><td>#' + property.id + '</td><td><div class="cell-title">' + escapeHtml(property.title) + '</div><div class="cell-sub">Property record</div></td><td>' + escapeHtml(property.propertyType) + '<div class="cell-sub">' + escapeHtml(property.areaSqFt) + ' sq. ft.</div></td><td>' + escapeHtml(owner.fullName || ("Owner #" + (owner.id || "?"))) + '</td><td>' + badge(property.status) + '</td><td><select class="inline-select property-status" data-id="' + property.id + '" aria-label="Update property status"><option ' + (property.status==="AVAILABLE"?"selected":"") + '>AVAILABLE</option><option ' + (property.status==="UNDER_REVIEW"?"selected":"") + '>UNDER_REVIEW</option><option ' + (property.status==="REGISTERED"?"selected":"") + '>REGISTERED</option></select></td></tr>';
  }).join("") : '<tr><td colspan="6" class="empty">No property records yet. Register one above.</td></tr>';
  fillSelect("locationPropertySelect", state.properties.filter(p => !state.locations.some(l => l.property && l.property.id === p.id)), p => p.title + " · #" + p.id, "Choose a property");
  fillSelect("visitPropertySelect", state.properties, p => p.title + " · #" + p.id, "Choose a property");
}
function renderLocations() {
  byId("locationsTable").innerHTML = state.locations.length ? state.locations.map(location =>
    '<tr><td><span class="cell-title">' + escapeHtml(location.property?.title || ("Property #" + (location.property?.id || "?"))) + '</span></td><td>' + escapeHtml(location.address) + '</td><td>' + escapeHtml(location.city) + '</td><td>' + escapeHtml(location.district) + '</td><td>' + escapeHtml(location.state) + '</td><td>' + escapeHtml(location.pincode) + '</td></tr>'
  ).join("") : '<tr><td colspan="6" class="empty">No locations added yet.</td></tr>';
}
function renderVisits() {
  byId("visitCount").textContent = state.visits.length;
  byId("visitsTable").innerHTML = state.visits.length ? state.visits.map(visit =>
    '<tr><td>#' + visit.id + '</td><td><span class="cell-title">' + escapeHtml(visit.property?.title || ("Property #" + (visit.property?.id || "?"))) + '</span></td><td>' + escapeHtml(visit.visitDate) + '</td><td>' + escapeHtml(visit.note || "—") + '</td><td>' + badge(visit.status) + '</td><td><select class="inline-select visit-status" data-id="' + visit.id + '" aria-label="Update visit status"><option ' + (visit.status==="REQUESTED"?"selected":"") + '>REQUESTED</option><option ' + (visit.status==="APPROVED"?"selected":"") + '>APPROVED</option><option ' + (visit.status==="COMPLETED"?"selected":"") + '>COMPLETED</option><option ' + (visit.status==="CANCELLED"?"selected":"") + '>CANCELLED</option></select></td></tr>'
  ).join("") : '<tr><td colspan="6" class="empty">No visit requests yet.</td></tr>';
}
async function loadDashboard() {
  byId("refreshButton").disabled = true;
  try {
    const [owners, properties, locations, visits] = await Promise.all([
      request("/owners"), request("/properties"), request("/locations"), request("/visits")
    ]);
    state.owners = owners || [];
    state.properties = properties || [];
    state.locations = locations || [];
    state.visits = visits || [];
    renderOwners(); renderProperties(); renderLocations(); renderVisits();
    notice("Workspace data refreshed.", "info");
  } catch (error) {
    notice(error.message + " Make sure Spring Boot and MySQL are running.", "error");
  } finally {
    byId("refreshButton").disabled = false;
  }
}
function formObject(form) { return Object.fromEntries(new FormData(form).entries()); }
byId("ownerForm").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  try {
    await request("/owners", { method:"POST", body:JSON.stringify(formObject(form)) });
    form.reset(); await loadDashboard(); notice("Owner added successfully.");
  } catch (error) { notice(error.message, "error"); }
});
byId("propertyForm").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = formObject(form);
  const payload = { title:values.title, propertyType:values.propertyType, areaSqFt:Number(values.areaSqFt), status:"AVAILABLE", owner:{id:Number(values.ownerId)} };
  try {
    await request("/properties", { method:"POST", body:JSON.stringify(payload) });
    form.reset(); await loadDashboard(); notice("Property registered successfully.");
  } catch (error) { notice(error.message, "error"); }
});
byId("locationForm").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = formObject(form);
  const payload = { property:{id:Number(values.propertyId)}, address:values.address, city:values.city, district:values.district, state:values.state, pincode:values.pincode };
  try {
    await request("/locations", { method:"POST", body:JSON.stringify(payload) });
    form.reset(); await loadDashboard(); notice("Property location saved.");
  } catch (error) { notice(error.message, "error"); }
});
byId("visitForm").addEventListener("submit", async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = formObject(form);
  const payload = { property:{id:Number(values.propertyId)}, visitDate:values.visitDate, note:values.note };
  try {
    await request("/visits", { method:"POST", body:JSON.stringify(payload) });
    form.reset(); byId("visitDate").min = dateToday(); await loadDashboard(); notice("Visit request created.");
  } catch (error) { notice(error.message, "error"); }
});
document.addEventListener("change", async event => {
  const target = event.target;
  if (target.matches(".property-status")) {
    try {
      await request("/properties/" + target.dataset.id + "/status?status=" + encodeURIComponent(target.value), { method:"PATCH" });
      await loadDashboard(); notice("Property status updated.");
    } catch (error) { notice(error.message, "error"); await loadDashboard(); }
  }
  if (target.matches(".visit-status")) {
    try {
      await request("/visits/" + target.dataset.id + "/status?status=" + encodeURIComponent(target.value), { method:"PATCH" });
      await loadDashboard(); notice("Visit request status updated.");
    } catch (error) { notice(error.message, "error"); await loadDashboard(); }
  }
});
byId("refreshButton").addEventListener("click", loadDashboard);
byId("visitDate").min = dateToday();
loadDashboard();