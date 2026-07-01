// Lightweight browser-side session for the email login. The session is just the
// identity returned by /api/auth/login/* kept in localStorage; the two roles
// (USER / ADMIN) are produced by two separate endpoints, so a contact can never
// obtain an ADMIN session and vice-versa.

import type { AuthSession } from "./types";
import { closeSchedule } from "./schedule";

const SESSION_KEY = "ctrAuth";

/** Fired on the window whenever the stored session changes (login / logout). */
export const AUTH_EVENT = "ctr-auth-change";

export function getSession(): AuthSession | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? (JSON.parse(raw) as AuthSession) : null;
  } catch {
    return null;
  }
}

export function setSession(session: AuthSession): void {
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    // A fresh sign-in must never inherit a booking window left open by a
    // previous account — it only reopens by pressing "Schedule a session"
    // next to a course this contact has actually bought.
    closeSchedule();
    window.dispatchEvent(new Event(AUTH_EVENT));
  } catch {
    /* localStorage unavailable — session simply won't persist */
  }
}

export function clearSession(): void {
  try {
    localStorage.removeItem(SESSION_KEY);
    // Drop any open booking window so the next account starts clean.
    closeSchedule();
    window.dispatchEvent(new Event(AUTH_EVENT));
  } catch {
    /* ignore storage errors */
  }
}
