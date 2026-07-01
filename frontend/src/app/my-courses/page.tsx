"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { openSchedule } from "@/lib/schedule";
import { StarRating } from "@/components/Stars";
import type { MyPurchase } from "@/lib/types";

/**
 * The logged-in contact's personal page: every course they have bought, listed
 * one under another. Each contact only ever sees its own purchases — the list is
 * fetched for the session's own email.
 */
export default function MyCoursesPage() {
  const [purchases, setPurchases] = useState<MyPurchase[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState<string>("");

  useEffect(() => {
    const session = getSession();
    if (!session) return;
    setName([session.firstName, session.lastName].filter(Boolean).join(" ") || session.email);
    api
      .get<MyPurchase[]>(`/api/public/me/purchases?email=${encodeURIComponent(session.email)}`)
      .then(setPurchases)
      .catch((e) => setError((e as Error).message));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">My courses</h1>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          {name ? `Courses purchased by ${name}` : "The courses you've purchased"}
        </p>
      </div>

      {error && <p className="text-red-600 dark:text-red-400">Could not load your courses: {error}</p>}

      {purchases === null && !error && (
        <p className="text-slate-500 dark:text-slate-400">Loading…</p>
      )}

      {purchases && purchases.length === 0 && (
        <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
          <p className="text-slate-600 dark:text-slate-300">You haven't purchased any course yet.</p>
          <Link
            href="/"
            className="mt-3 inline-block rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2 text-sm font-semibold text-white hover:from-indigo-700 hover:to-violet-700"
          >
            Browse courses
          </Link>
        </div>
      )}

      {purchases && purchases.length > 0 && (
        <ul className="space-y-3">
          {purchases.map((p) => (
            <li
              key={p.courseId}
              className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-colors dark:border-slate-800 dark:bg-slate-900"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="mb-1 flex items-center gap-2">
                    <span className="rounded bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300">
                      {p.categoryLabel ?? p.category}
                    </span>
                    {p.level && (
                      <span className="text-xs text-slate-400 dark:text-slate-500">{p.level}</span>
                    )}
                  </div>
                  <h3 className="font-semibold text-slate-900 dark:text-white">{p.name}</h3>
                  {p.description && (
                    <p className="mt-1 line-clamp-2 text-sm text-slate-600 dark:text-slate-300">
                      {p.description}
                    </p>
                  )}
                  <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500 dark:text-slate-400">
                    {p.durationHours != null && <span>{p.durationHours} h</span>}
                    {p.purchaseDate && <span>Purchased: {p.purchaseDate}</span>}
                    {p.rating != null ? (
                      <span className="flex items-center gap-1">
                        Your review: <StarRating value={p.rating} size={13} />
                      </span>
                    ) : (
                      <span>No review</span>
                    )}
                  </div>
                </div>
              </div>
              <div className="mt-4 flex justify-end border-t border-slate-100 pt-4 dark:border-slate-800">
                <Link
                  href="/schedule"
                  onClick={() => openSchedule()}
                  className="rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2 text-sm font-semibold text-white hover:from-indigo-700 hover:to-violet-700"
                >
                  Schedule a session
                </Link>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
