const API_BASE = window.location.origin.includes("8080")
  ? window.location.origin
  : "http://127.0.0.1:8080";
const TOKEN_KEY = "apihub_token";

const $ = (id) => document.getElementById(id);
const loginPage = $("loginPage");
const dashboardPage = $("dashboardPage");
const toastEl = $("toast");
const loginError = $("loginError");
const apiResult = $("apiResult");

$("apiBaseLabel").textContent = "API 网关 " + API_BASE;

let currentUser = null;

/* ---------- 页面切换 ---------- */
function showLoginPage() {
  loginPage.classList.remove("hidden");
  dashboardPage.classList.add("hidden");
  document.title = "登录 - ApiHub";
}

function showDashboard() {
  loginPage.classList.add("hidden");
  dashboardPage.classList.remove("hidden");
  document.title = "控制台 - ApiHub";
}

function showLoginError(msg) {
  if (!msg) {
    loginError.classList.add("hidden");
    loginError.textContent = "";
    return;
  }
  loginError.textContent = msg;
  loginError.classList.remove("hidden");
}

function showToast(msg) {
  toastEl.textContent = msg;
  toastEl.classList.remove("hidden");
  setTimeout(() => toastEl.classList.add("hidden"), 2400);
}

function token() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

/* ---------- 登录页 Tab ---------- */
document.querySelectorAll(".tab").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");
    const tab = btn.dataset.tab;
    $("loginForm").classList.toggle("hidden", tab !== "login");
    $("registerForm").classList.toggle("hidden", tab !== "register");
    showLoginError("");
  });
});

/* ---------- 控制台导航 ---------- */
document.querySelectorAll(".nav-item").forEach((btn) => {
  btn.addEventListener("click", () => {
    switchView(btn.dataset.view);
  });
});

function switchView(view) {
  document.querySelectorAll(".nav-item").forEach((b) => {
    b.classList.toggle("active", b.dataset.view === view);
  });
  document.querySelectorAll(".view").forEach((v) => v.classList.add("hidden"));
  $("view-" + view).classList.remove("hidden");
}

/* ---------- API 请求（登录接口不写入结果区） ---------- */
async function apiRequest(path, options = {}, { showInPanel = false } = {}) {
  const headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
  if (token() && !headers.Authorization) {
    headers.Authorization = "Bearer " + token();
  }
  const res = await fetch(API_BASE + path, Object.assign({}, options, { headers }));
  const text = await res.text();
  let data;
  try {
    data = JSON.parse(text);
  } catch (e) {
    throw new Error(text || "服务器响应异常");
  }
  if (showInPanel && apiResult) {
    apiResult.textContent = JSON.stringify(data, null, 2);
  }
  if (data.code !== 0) {
    throw new Error(data.msg || "请求失败");
  }
  return data;
}

/* ---------- 用户渲染 ---------- */
function renderUser(user) {
  currentUser = user;
  $("welcome").textContent = "你好，" + (user.nickname || user.username);
  $("userMeta").textContent = user.username + " · " + (user.roles || []).join(", ");
  $("statUsername").textContent = user.username;
  $("statUserId").textContent = user.userId;
  $("statRoles").textContent = (user.roles || []).join(", ");
}

function enterDashboard(user) {
  renderUser(user);
  showDashboard();
  switchView("overview");
}

function logout() {
  localStorage.removeItem(TOKEN_KEY);
  currentUser = null;
  showLoginPage();
  $("loginForm").reset();
  showLoginError("");
  if (apiResult) apiResult.textContent = "暂无调用记录";
  $("ifaceTableBody").innerHTML = '<tr><td colspan="6" class="empty">点击「刷新列表」加载数据</td></tr>';
  $("statIfaceCount").textContent = "-";
  showToast("已退出登录");
}

/* ---------- 接口列表表格 ---------- */
function renderInterfaceTable(list) {
  const tbody = $("ifaceTableBody");
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty">暂无接口数据</td></tr>';
    $("statIfaceCount").textContent = "0";
    return;
  }
  $("statIfaceCount").textContent = String(list.length);
  tbody.innerHTML = list.map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${escapeHtml(item.name)}</td>
      <td><code>${escapeHtml(item.path)}</code></td>
      <td>${escapeHtml(item.method)}</td>
      <td>${escapeHtml(item.category || "-")}</td>
      <td><span class="badge ${item.status === 1 ? "on" : "off"}">${item.status === 1 ? "上线" : "下线"}</span></td>
    </tr>
  `).join("");
}

function escapeHtml(str) {
  return String(str ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

async function loadInterfaces() {
  const data = await apiRequest("/api/admin/interfaces");
  renderInterfaceTable(data.data);
  return data;
}

/* ---------- 事件绑定 ---------- */
$("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  showLoginError("");
  const form = new FormData(e.target);
  const btn = e.target.querySelector("button[type=submit]");
  btn.disabled = true;
  btn.textContent = "登录中...";
  try {
    const data = await apiRequest("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({
        username: form.get("username"),
        password: form.get("password"),
      }),
    });
    localStorage.setItem(TOKEN_KEY, data.data.token);
    enterDashboard(data.data);
    showToast("登录成功，已进入控制台");
    try {
      await loadInterfaces();
    } catch (_) { /* 接口列表加载失败不阻断登录 */ }
  } catch (err) {
    showLoginError(err.message);
  } finally {
    btn.disabled = false;
    btn.textContent = "登 录";
  }
});

$("registerForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  showLoginError("");
  const form = new FormData(e.target);
  const btn = e.target.querySelector("button[type=submit]");
  btn.disabled = true;
  btn.textContent = "注册中...";
  try {
    await apiRequest("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({
        username: form.get("username"),
        nickname: form.get("nickname"),
        password: form.get("password"),
      }),
    });
    showToast("注册成功，请登录");
    document.querySelector('.tab[data-tab="login"]').click();
    $("loginForm").querySelector('[name=username]').value = form.get("username");
  } catch (err) {
    showLoginError(err.message);
  } finally {
    btn.disabled = false;
    btn.textContent = "注 册";
  }
});

$("logoutBtn").addEventListener("click", logout);

$("refreshMeBtn").addEventListener("click", async () => {
  try {
    const data = await apiRequest("/api/auth/me");
    renderUser(data.data);
    showToast("用户信息已刷新");
  } catch (err) {
    showToast(err.message);
    if (String(err.message).includes("未认证")) logout();
  }
});

$("loadIfacesBtn").addEventListener("click", async () => {
  try {
    await loadInterfaces();
    switchView("interfaces");
    showToast("接口列表已更新");
  } catch (err) {
    showToast(err.message);
  }
});

$("quickLoadIfacesBtn").addEventListener("click", () => $("loadIfacesBtn").click());

$("weatherBtn").addEventListener("click", async () => {
  const city = encodeURIComponent($("weatherCity").value || "北京");
  try {
    await apiRequest("/api/open/weather?city=" + city, {}, { showInPanel: true });
    switchView("open");
    showToast("天气接口调用成功");
  } catch (err) {
    showToast(err.message);
  }
});

$("quickWeatherBtn").addEventListener("click", () => $("weatherBtn").click());

$("clearResultBtn").addEventListener("click", () => {
  apiResult.textContent = "暂无调用记录";
});

/* ---------- 启动：已登录则直接进入控制台 ---------- */
(async function boot() {
  if (!token()) {
    showLoginPage();
    return;
  }
  try {
    const data = await apiRequest("/api/auth/me");
    enterDashboard(data.data);
    try {
      await loadInterfaces();
    } catch (_) { /* ignore */ }
  } catch (e) {
    logout();
  }
})();
