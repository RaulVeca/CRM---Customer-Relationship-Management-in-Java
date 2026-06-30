"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AUTH_EVENT, getSession } from "@/lib/auth";
import type { AuthSession } from "@/lib/types";

/**
 * Public navigation (Courses / Companies). Only contacts ("Cont utilizator")
 * have the public site, so these links show only for a USER session — admins
 * live in the admin area and signed-out visitors are bounced to the login
 * screen, so neither sees them.
 */
export default function PrimaryNav() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const sync = () => setSession(getSession());
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  if (!mounted || session?.role !== "USER") return null;

  return (
    <>
      <Link
        href="/"
        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
      >
        Courses
      </Link>
      <Link
        href="/companies"
        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
      >
        Companies
      </Link>
      <Link
        href="/my-courses"
        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
      >
        My courses
      </Link>
      <Link
        href="/schedule"
        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
      >
        Ședințe online
      </Link>
    </>
  );
}
