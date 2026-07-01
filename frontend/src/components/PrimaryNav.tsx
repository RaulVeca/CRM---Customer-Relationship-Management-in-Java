"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { api } from "@/lib/api";
import { AUTH_EVENT, getSession } from "@/lib/auth";
import { SCHEDULE_EVENT, closeSchedule, isScheduleOpen } from "@/lib/schedule";
import type { AuthSession, MyPurchase } from "@/lib/types";

/**
 * Public navigation (Courses / My courses). Only contacts ("Cont utilizator")
 * have the public site, so these links show only for a USER session — admins
 * live in the admin area and signed-out visitors are bounced to the login
 * screen, so neither sees them.
 *
 * The "Online sessions" entry has a hard prerequisite: the contact must actually
 * own at least one course. Ownership is checked against the server (not just the
 * local flag), so a contact with no purchases can never see the booking window —
 * and any stale flag left over from another account is dropped on sight.
 *
 * The window opens on "Schedule a session" and closes again as soon as the
 * contact leaves the schedule page — whether or not a session was booked — so a
 * contact who opens the window and walks away does not keep the link around.
 *
 * "My sessions" is always available to a contact; the page itself shows a
 * "You have no scheduled sessions" message when there is nothing booked yet, so
 * the link needs no prerequisite and stays visible without a refresh after booking.
 */
export default function PrimaryNav() {
  const pathname = usePathname();
  const [session, setSession] = useState<AuthSession | null>(null);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [hasPurchases, setHasPurchases] = useState(false);
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

  // Verify course ownership for the current USER against the server. A contact
  // with no purchases can never book a session, so we also clear any stale
  // booking-window flag it may have inherited.
  const email = session?.role === "USER" ? session.email : null;
  useEffect(() => {
    if (!email) {
      setHasPurchases(false);
      return;
    }
    let cancelled = false;
    api
      .get<MyPurchase[]>(`/api/public/me/purchases?email=${encodeURIComponent(email)}`)
      .then((list) => {
        if (cancelled) return;
        const owns = list.length > 0;
        setHasPurchases(owns);
        if (!owns) closeSchedule();
      })
      .catch(() => {
        if (!cancelled) setHasPurchases(false);
      });
    return () => {
      cancelled = true;
    };
  }, [email]);

  // Close the booking window the moment the contact navigates away from the
  // schedule page. PrimaryNav stays mounted across route changes, so watching
  // the pathname here is strict-mode safe (a page-unmount cleanup would misfire
  // under React's dev double-mount). Leaving /schedule without booking therefore
  // hides "Online sessions" again, exactly like a successful booking does.
  const prevPath = useRef(pathname);
  useEffect(() => {
    if (prevPath.current === "/schedule" && pathname !== "/schedule") {
      closeSchedule();
    }
    prevPath.current = pathname;
  }, [pathname]);

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
      {scheduleOpen && hasPurchases && (
        <Link
          href="/schedule"
          className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
        >
          Online sessions
        </Link>
      )}
      <Link
        href="/my-sessions"
        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white"
      >
        My sessions
      </Link>
    </>
  );
}
