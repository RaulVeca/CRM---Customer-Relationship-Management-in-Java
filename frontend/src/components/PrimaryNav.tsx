"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AUTH_EVENT, getSession } from "@/lib/auth";
import { SCHEDULE_EVENT, isScheduleOpen } from "@/lib/schedule";
import type { AuthSession } from "@/lib/types";

/**
 * Public navigation (Courses / My courses). Only contacts ("Cont utilizator")
 * have the public site, so these links show only for a USER session — admins
 * live in the admin area and signed-out visitors are bounced to the login
 * screen, so neither sees them.
 */
export default function PrimaryNav() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const sync = () => {
      setSession(getSession());
      setScheduleOpen(isScheduleOpen());
    };
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    window.addEventListener(SCHEDULE_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_EVENT, sync);
      window.removeEventListener(SCHEDULE_EVENT, sync);
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
        href="/my-courses"
        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
      >
        My courses
      </Link>
      {scheduleOpen && (
        <Link
          href="/schedule"
          className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
        >
          Ședințe online
        </Link>
      )}
    </>
  );
}
