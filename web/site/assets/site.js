// Forms on the reset-password and delete-account pages. Same-origin API calls only.
(function () {
  "use strict";
  const API = { "Content-Type": "application/json", "X-VajraX-Client": "web" };

  function show(el, text) { if (el) el.textContent = text || ""; }
  function invalid(input, errorEl, text) {
    input.setAttribute("aria-invalid", text ? "true" : "false");
    show(errorEl, text);
    return !text;
  }
  async function problem(response) {
    try { return await response.json(); } catch (e) { return { title: "Something went wrong. Please try again." }; }
  }
  function busy(button, on) {
    button.disabled = on;
    button.setAttribute("aria-busy", on ? "true" : "false");
  }
  function setStatus(el, ok, text) {
    el.className = "status " + (ok ? "ok" : "bad");
    el.textContent = text;
  }

  // ---- Reset password (link from the email: /reset-password#token=…; ?token=… also accepted)
  const reset = document.getElementById("reset-form");
  if (reset) {
    const token = new URLSearchParams(location.hash.slice(1)).get("token") || new URLSearchParams(location.search).get("token");
    // Keep the single-use token out of the address bar and the browser history.
    if (token) history.replaceState(null, "", location.pathname);
    const pw = document.getElementById("new-password");
    const again = document.getElementById("confirm-password");
    const status = document.getElementById("reset-status");
    const submit = document.getElementById("reset-submit");
    if (!token) {
      setStatus(status, false, "This link is incomplete. Open the link from the email again, or ask for a new one in the app.");
      submit.disabled = true;
    }
    reset.addEventListener("submit", async function (event) {
      event.preventDefault();
      const ok1 = invalid(pw, document.getElementById("new-password-error"), pw.value.length < 10 ? "Use at least 10 characters." : "");
      const ok2 = invalid(again, document.getElementById("confirm-password-error"), again.value !== pw.value ? "The two passwords don't match." : "");
      if (!ok1 || !ok2) { (ok1 ? again : pw).focus(); return; }
      busy(submit, true);
      try {
        const r = await fetch("/v1/auth/password/reset", { method: "POST", headers: API, body: JSON.stringify({ token: token, newPassword: pw.value }) });
        if (r.ok) {
          reset.reset();
          setStatus(status, true, "Your password was changed. Sign in with it in the app. Other devices were signed out.");
          submit.disabled = true;
          return;
        }
        const p = await problem(r);
        if (p.errors && p.errors.newPassword) { invalid(pw, document.getElementById("new-password-error"), p.errors.newPassword); pw.focus(); }
        else setStatus(status, false, p.title);
      } catch (e) {
        setStatus(status, false, "You seem to be offline. Check your connection and try again.");
      } finally {
        if (!status.classList.contains("ok")) busy(submit, false);
      }
    });
  }

  // ---- Delete account
  const del = document.getElementById("delete-form");
  if (del) {
    const email = document.getElementById("delete-email");
    const pw = document.getElementById("delete-password");
    const confirm = document.getElementById("delete-confirm");
    const status = document.getElementById("delete-status");
    const submit = document.getElementById("delete-submit");
    del.addEventListener("submit", async function (event) {
      event.preventDefault();
      const ok1 = invalid(email, document.getElementById("delete-email-error"), /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.value.trim()) ? "" : "Enter the email of your account.");
      const ok2 = invalid(pw, document.getElementById("delete-password-error"), pw.value ? "" : "Enter your password.");
      const ok3 = invalid(confirm, document.getElementById("delete-confirm-error"), confirm.checked ? "" : "Tick the box to confirm.");
      if (!ok1) { email.focus(); return; }
      if (!ok2) { pw.focus(); return; }
      if (!ok3) { confirm.focus(); return; }
      busy(submit, true);
      try {
        const login = await fetch("/v1/auth/login", { method: "POST", headers: API, body: JSON.stringify({ email: email.value.trim(), password: pw.value, deviceName: "Website (delete account)" }) });
        if (!login.ok) { setStatus(status, false, (await problem(login)).title); return; }
        const session = await login.json();
        const r = await fetch("/v1/account", { method: "DELETE", headers: Object.assign({ Authorization: "Bearer " + session.accessToken }, API), body: JSON.stringify({ password: pw.value }) });
        if (r.ok) {
          del.reset();
          setStatus(status, true, "Your account and everything stored with it were deleted.");
          submit.disabled = true;
          return;
        }
        setStatus(status, false, (await problem(r)).title);
      } catch (e) {
        setStatus(status, false, "You seem to be offline. Check your connection and try again.");
      } finally {
        if (!status.classList.contains("ok")) busy(submit, false);
      }
    });
  }
})();
