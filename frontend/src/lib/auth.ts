// Lightweight browser-side session for the email login. The session is just the
// identity returned by /api/auth/login/* kept in localStorage; the two roles
// (USER / ADMIN) are produced by two separate endpoints, so a contact can never
// obtain an ADMIN session and vice-versa.

import type { AuthSession } from "./types";

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
    window.dispatchEvent(new Event(AUTH_EVENT));
  } catch {
    /* localStorage unavailable — session simply won't persist */
  }
}

export function clearSession(): void {
  try {
    localStorage.removeItem(SESSION_KEY);
    window.dispatchEvent(new Event(AUTH_EVENT));
  } catch {
    /* ignore storage errors */
  }
}
