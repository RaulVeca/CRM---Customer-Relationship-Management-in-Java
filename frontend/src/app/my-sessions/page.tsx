"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { TrashIcon } from "@/components/icons";
import type { MySession } from "@/lib/types";

const pad = (n: number) => String(n).padStart(2, "0");
const fmtHour = (h: number) => `${pad(h)}:00`;

const MONTHS = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

/** Turns "2026-07-15" into "15 July 2026". */
function fmtDate(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  return `${d} ${MONTHS[m - 1]} ${y}`;
}

/**
 * The contact's personal "my sessions" window: every online session they have
 * booked with a trainer. It only appears once the contact has booked at least
 * one session — an empty list shows nothing to schedule against here.
 *
 * Each session carries a "Cancel" button that deletes the booking. Removing
 * the row frees that hour range again in the trainer's calendar.
 */
export default function MySessionsPage() {
  const [contact, setContact] = useState<{ id: number; email: string } | null>(null);
  const [sessions, setSessions] = useState<MySession[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState<number | null>(null);

  useEffect(() => {
    const session = getSession();
    if (!session) {
      setContact(null);
      return;
    }
    setContact({ id: session.id, email: session.email });
    api
      .get<MySession[]>(`/api/trainers/sessions?contactId=${session.id}`)
      .then(setSessions)
      .catch((e) => setError((e as Error).message));
  }, []);

  async function cancel(id: number) {
    if (!contact) return;
    setCancelling(id);
    setError(null);
    try {
      await api.del<void>(`/api/trainers/sessions/${id}?contactId=${contact.id}`);
      // Drop the cancelled session from the list; the trainer's calendar slot is
      // freed server-side by the delete.
      setSessions((prev) => (prev ? prev.filter((s) => s.id !== id) : prev));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setCancelling(null);
    }
  }

  if (!contact) {
    return (
      <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-8 text-center dark:border-zinc-700 dark:bg-zinc-900">
        <p className="text-zinc-600 dark:text-zinc-300">
          You must be signed in to view your sessions.
        </p>
        <Link
          href="/login"
          className="mt-3 inline-block rounded-lg bg-gradient-to-r from-brand-600 to-brand-600 px-4 py-2 text-sm font-semibold text-white hover:from-brand-700 hover:to-brand-700"
        >
          Log in
        </Link>
      </div>
    );
  }

  if (sessions === null && !error) {
    return (
      <p className="py-10 text-center text-sm text-zinc-500 dark:text-zinc-400">Loading…</p>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900 dark:text-white">My sessions</h1>
        <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
          The online sessions you've scheduled. Cancel a session to free up the slot in the
          trainer's calendar.
        </p>
      </div>

      {error && (
        <div className="rounded-xl border border-red-300 bg-red-50 p-4 text-red-700 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300">
          {error}
        </div>
      )}

      {/* No session booked → just the message, so the window stays always visible. */}
      {sessions && sessions.length === 0 && (
        <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-8 text-center dark:border-zinc-700 dark:bg-zinc-900">
          <p className="text-zinc-600 dark:text-zinc-300">You have no scheduled sessions.</p>
        </div>
      )}

      <ul className="space-y-3">
        {sessions?.map((s) => (
          <li
            key={s.id}
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-zinc-200 bg-white p-5 shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
          >
            <div className="min-w-0">
              <p className="font-semibold text-zinc-900 dark:text-white">{s.trainerName}</p>
              <p className="mt-0.5 text-sm text-zinc-600 dark:text-zinc-300">
                {fmtDate(s.date)} · {fmtHour(s.startHour)}–{fmtHour(s.endHour)}
              </p>
            </div>
            <button
              type="button"
              onClick={() => cancel(s.id)}
              disabled={cancelling === s.id}
              className="inline-flex items-center gap-1.5 rounded-lg border border-red-300 bg-white px-3 py-1.5 text-sm font-semibold text-red-600 transition-colors hover:bg-red-50 disabled:opacity-60 dark:border-red-500/40 dark:bg-zinc-900 dark:text-red-300 dark:hover:bg-red-500/10"
            >
              <TrashIcon className="h-4 w-4" />
              {cancelling === s.id ? "Cancelling…" : "Cancel"}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
