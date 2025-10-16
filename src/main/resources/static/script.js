// ---------- Config ----------
const API_BASE = ""; // mismo origen
const ENDPOINT = `${API_BASE}/api/properties`;

// ---------- State ----------
let editId = null;
const state = {
  page: 0,
  size: 5,
  sort: "id,desc",
  totalPages: 1,
  totalElements: 0,
};

// ---------- DOM helpers ----------
const $ = (sel) => document.querySelector(sel);

function showToast(msg, timeout = 2000) {
  const t = $("#toast");
  t.textContent = msg;
  t.classList.remove("hidden");
  setTimeout(() => t.classList.add("hidden"), timeout);
}

// ---------- HTTP helper ----------
async function http(method, url, body) {
  const init = {
    method,
    headers: { "Content-Type": "application/json", "Accept": "application/json" }
  };
  if (body) init.body = JSON.stringify(body);

  const res = await fetch(url, init);

  if (res.status === 204) return method === "GET" ? [] : null;
  if (res.ok) {
    const text = await res.text();
    try { return text ? JSON.parse(text) : null; }
    catch { return text; }
  }

  let errMsg = `${res.status} ${res.statusText}`;
  try {
    const j = await res.json();
    if (j?.message) errMsg += ` - ${j.message}`;
    else if (j?.errors) errMsg += ` - ${JSON.stringify(j.errors)}`;
    else errMsg += ` - ${JSON.stringify(j)}`;
  } catch { }
  throw new Error(errMsg);
}

// ---------- Normalize ----------
function normalizeList(resp) {
  if (Array.isArray(resp)) return resp;
  if (resp && Array.isArray(resp.content)) return resp.content; // Page<>
  if (resp && resp._embedded && Array.isArray(resp._embedded.properties)) return resp._embedded.properties;
  return [];
}

// ---------- Render ----------
function renderRows(items) {
  const tbody = $("#props-tbody");
  tbody.innerHTML = "";
  for (const p of items) {
    const tr = document.createElement("tr");

    const tdA = document.createElement("td"); tdA.textContent = p.address;
    const tdP = document.createElement("td"); tdP.textContent = p.price;
    const tdS = document.createElement("td"); tdS.textContent = p.size;
    const tdD = document.createElement("td"); tdD.textContent = p.description;

    const tdActions = document.createElement("td");
    const btnEdit = document.createElement("button");
    btnEdit.className = "link";
    btnEdit.textContent = "Edit";
    btnEdit.addEventListener("click", () => startEdit(p));

    const btnDel = document.createElement("button");
    btnDel.className = "link danger";
    btnDel.textContent = "Delete";
    btnDel.addEventListener("click", () => deleteProperty(p.id));

    tdActions.append(btnEdit, document.createTextNode(" | "), btnDel);

    tr.append(tdA, tdP, tdS, tdD, tdActions);
    tbody.append(tr);
  }
}

function renderPager() {
  const el = $("#pager");
  el.innerHTML = "";
  if (state.totalPages <= 1) return;

  const makeBtn = (label, disabled, onClick) => {
    const b = document.createElement("button");
    b.textContent = label;
    if (disabled) b.disabled = true;
    b.addEventListener("click", onClick);
    return b;
  };

  // Prev
  el.appendChild(makeBtn("«", state.page === 0, () => gotoPage(state.page - 1)));

  // Ventana de páginas (máx 7 botones)
  const MAX = 7;
  const start = Math.max(0, Math.min(state.page - 2, Math.max(0, state.totalPages - MAX)));
  const end = Math.min(state.totalPages - 1, start + MAX - 1);

  for (let p = start; p <= end; p++) {
    const b = makeBtn(String(p + 1), false, () => gotoPage(p));
    if (p === state.page) b.classList.add("active");
    el.appendChild(b);
  }

  // Next
  el.appendChild(makeBtn("»", state.page >= state.totalPages - 1, () => gotoPage(state.page + 1)));
}

// ---------- CRUD ----------
function getFormData() {
  return {
    address: $("#address").value.trim(),
    price: Number($("#price").value),
    size: Number($("#size").value),
    description: $("#description").value.trim(),
  };
}

function setFormData(p) {
  $("#address").value = p?.address ?? "";
  $("#price").value = p?.price ?? "";
  $("#size").value = p?.size ?? "";
  $("#description").value = p?.description ?? "";
}

function validateForm(data) {
  const { address, price, size, description } = data;
  if (!address || !description) return "Address and Description are required.";
  if (Number.isNaN(price) || Number.isNaN(size)) return "Price and Size must be numbers.";
  if (price <= 0) return "Price must be greater than 0.";
  if (size <= 0) return "Size must be greater than 0.";
  return null;
}

async function createProperty(data) {
  await http("POST", ENDPOINT, data);
  showToast("Property created");
}

async function updateProperty(id, data) {
  await http("PUT", `${ENDPOINT}/${id}`, data);
  showToast("Property updated");
}

async function deleteProperty(id) {
  if (!confirm("Delete this property?")) return;
  await http("DELETE", `${ENDPOINT}/${id}`);
  showToast("Property deleted");
  // recarga y si la página quedó vacía, ve a la anterior
  await loadPage(state.page);
}

// ---------- Edit mode ----------
function startEdit(p) {
  editId = p.id;
  setFormData(p);
  $("#form-title").textContent = "Edit Property";
  $("#submit-btn").textContent = "Save Changes";
  $("#cancel-edit-btn").classList.remove("hidden");
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetFormToCreate() {
  editId = null;
  setFormData({ address: "", price: "", size: "", description: "" });
  $("#form-title").textContent = "Add New Property";
  $("#submit-btn").textContent = "Add Property";
  $("#cancel-edit-btn").classList.add("hidden");
}

// ---------- Paging ----------
async function gotoPage(p) {
  if (p < 0) p = 0;
  if (p > state.totalPages - 1) p = state.totalPages - 1;
  await loadPage(p);
}

function getFilters() {
  // q viene del input grande "search"
  const q = $("#search").value.trim();

  const address = $("#f-address").value.trim();
  const minPriceV = $("#f-min-price").value;
  const maxPriceV = $("#f-max-price").value;
  const minSizeV = $("#f-min-size").value;
  const maxSizeV = $("#f-max-size").value;

  const filters = {};
  if (q) filters.q = q;
  if (address) filters.address = address;
  if (minPriceV !== "") filters.minPrice = minPriceV; // Spring bindea BigDecimal
  if (maxPriceV !== "") filters.maxPrice = maxPriceV;
  if (minSizeV !== "") filters.minSize = minSizeV;  // Spring bindea Double
  if (maxSizeV !== "") filters.maxSize = maxSizeV;

  return filters;
}

async function loadPage(p = 0) {
  state.page = p;

  const params = new URLSearchParams({
    page: state.page,
    size: state.size,
    sort: state.sort
  });

  const filters = getFilters();
  Object.entries(filters).forEach(([k, v]) => params.set(k, v));

  const url = `${ENDPOINT}?${params.toString()}`;
  try {
    const raw = await http("GET", url);
    state.totalPages = raw?.totalPages ?? 1;
    state.totalElements = raw?.totalElements ?? 0;

    renderRows(normalizeList(raw));
    renderPager();
  } catch (e) {
    console.error(e);
    showToast("Error loading properties");
  }
}

document.addEventListener("DOMContentLoaded", () => {
  // ... submit create/update y cancel edit igual ...

  $("#search").addEventListener("input", () => loadPage(0));
  $("#apply-filters").addEventListener("click", () => loadPage(0));
  $("#clear-filters").addEventListener("click", () => {
    $("#f-address").value = "";
    $("#f-min-price").value = "";
    $("#f-max-price").value = "";
    $("#f-min-size").value = "";
    $("#f-max-size").value = "";
    loadPage(0);
  });

  loadPage(0);
});

// ---------- Events ----------
document.addEventListener("DOMContentLoaded", () => {
  $("#property-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = getFormData();
    const err = validateForm(data);
    if (err) { showToast(err); return; }

    try {
      if (editId == null) {
        await createProperty(data);
        // ve a la primera página para ver lo nuevo si estás ordenando por id desc
        await loadPage(0);
      } else {
        await updateProperty(editId, data);
        await loadPage(state.page);
      }
      resetFormToCreate();
    } catch (e2) {
      console.error(e2);
      showToast(e2.message || "Save failed");
    }
  });

  $("#cancel-edit-btn").addEventListener("click", resetFormToCreate);
  $("#search").addEventListener("input", () => loadPage(state.page));

  // carga inicial
  loadPage(0);
});
