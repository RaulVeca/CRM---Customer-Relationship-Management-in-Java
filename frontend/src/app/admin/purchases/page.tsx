"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { StarRating } from "@/components/Stars";
import type { Purchase } from "@/lib/types";

export default function AdminPurchasesPage() {
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<Purchase[]>("/api/enrollments/history")
      .then(setPurchases)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Purchase history</h2>

      {error && <p className="text-red-600">{error}</p>}
      {loading && <p className="text-zinc-500">Loading…</p>}

      {!loading && !error && (
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Total orders</p>
            <p className="mt-1 text-2xl font-bold">{purchases.length}</p>
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
        <table className="w-full text-sm">
          <thead className="bg-zinc-50 text-left text-xs uppercase text-zinc-500 dark:bg-zinc-800/50 dark:text-zinc-400">
            <tr>
              <th className="px-4 py-3">Student</th>
              <th className="px-4 py-3">Course</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Date</th>
              <th className="px-4 py-3">Rating</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
            {purchases.map((p) => (
              <tr key={p.enrollmentId} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                <td className="px-4 py-3">
                  <div className="font-medium">{p.studentName}</div>
                  {p.studentEmail && <div className="text-xs text-zinc-400 dark:text-zinc-500">{p.studentEmail}</div>}
                </td>
                <td className="px-4 py-3">
                  <div className="font-medium">{p.courseName}</div>
                  {p.courseCode && <div className="font-mono text-xs text-zinc-400 dark:text-zinc-500">{p.courseCode}</div>}
                </td>
                <td className="px-4 py-3">
                  {p.status ? (
                    <span className="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300">
                      {p.status}
                    </span>
                  ) : (
                    <span className="text-zinc-400 dark:text-zinc-500">—</span>
                  )}
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-zinc-500 dark:text-zinc-400">{p.date ?? "—"}</td>
                <td className="px-4 py-3">
                  {p.rating ? <StarRating value={p.rating} size={14} /> : <span className="text-zinc-400 dark:text-zinc-500">—</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!loading && !error && purchases.length === 0 && (
        <p className="text-zinc-500 dark:text-zinc-400">No purchases yet.</p>
      )}
    </div>
  );
}
