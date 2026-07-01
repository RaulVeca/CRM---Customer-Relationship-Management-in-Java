"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { AUTH_EVENT, getSession, clearSession } from "@/lib/auth";
import type { AuthSession } from "@/lib/types";

/**
 * Header sign-in control. Reflects the browser session: a "Log in" link
 * when signed out, otherwise the account name plus a logout button. Admins land
 * directly in the admin area on login, so there is no separate "Admin" link.
 * Kept client-side so it can read localStorage and react to login / logout
 * events.
 */
export default function AuthNav() {
  const pathname = usePathname();
  const [session, setSessionState] = useState<AuthSession | null>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const sync = () => setSessionState(getSession());
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  function logout() {
    clearSession();
    window.location.assign("/login");
  }

  // Avoid a hydration mismatch: render nothing role-specific until mounted.
  if (!mounted) return <div className="h-8" aria-hidden="true" />;

  if (!session) {
    // On the login screen itself the button would just point back here.
    if (pathname === "/login") return <div className="h-8" aria-hidden="true" />;
    return (
      <Link
        href="/login"
        className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700"
      >
        Log in
      </Link>
    );
  }

  const name = [session.firstName, session.lastName].filter(Boolean).join(" ") || session.email;

  return (
    <div className="flex items-center gap-3">
      <span className="hidden text-sm text-slate-600 dark:text-slate-300 sm:inline">{name}</span>
      <button
        type="button"
        onClick={logout}
        className="rounded-md border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
      >
        Log out
      </button>
    </div>
  );
}
