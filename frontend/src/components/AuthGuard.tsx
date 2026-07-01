"use client";

import { useEffect, useState } from "react";
import { usePathname } from "next/navigation";
import { AUTH_EVENT, getSession } from "@/lib/auth";
import type { AuthSession } from "@/lib/types";

/**
 * Client-side access gate for the whole site. The login screen is the first
 * thing an unauthenticated visitor sees, and the two roles are kept apart:
 *
 * - signed out  → may only see {@code /login}
 * - USER (contact) → the public site (Courses / My courses); never the admin area
 * - ADMIN (trainer) → only the admin area; lands straight on {@code /admin/contacts}
 *
 * Anything outside a role's allowed area is redirected, and protected content is
 * never rendered while a redirect is pending (no flash).
 */
function decideRedirect(session: AuthSession | null, pathname: string): string | null {
  const onLogin = pathname === "/login";
  const onRegister = pathname === "/register";
  const onAdmin = pathname === "/admin" || pathname.startsWith("/admin/");

  if (!session) {
    // Signed out: the login screen and the registration screen are the only
    // pages allowed; everything else bounces to login.
    return onLogin || onRegister ? null : "/login";
  }
  if (session.role === "ADMIN") {
    return onAdmin ? null : "/admin/contacts";
  }
  // USER (contact): public site only, never the admin area, login or register.
  if (onAdmin) return "/";
  if (onLogin || onRegister) return "/";
  return null;
}

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  // undefined = session not read yet (first client render); null = signed out.
  const [session, setSession] = useState<AuthSession | null | undefined>(undefined);

  useEffect(() => {
    const sync = () => setSession(getSession());
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  const redirect = session === undefined ? null : decideRedirect(session, pathname);

  useEffect(() => {
    if (redirect) window.location.assign(redirect);
  }, [redirect]);

  if (session === undefined || redirect) {
    return (
      <p className="py-10 text-center text-sm text-slate-500 dark:text-slate-400">
        Loading…
      </p>
    );
  }

  return <>{children}</>;
}
