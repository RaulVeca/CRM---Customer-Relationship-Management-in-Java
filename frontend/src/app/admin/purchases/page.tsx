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
      {loading && <p className="text-slate-500">Loading…</p>}

      {!loading && !error && (
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Total orders</p>
            <p className="mt-1 text-2xl font-bold">{purchases.length}</p>
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500 dark:bg-slate-800/50 dark:text-slate-400">
            <tr>
              <th className="px-4 py-3">Student</th>
              <th className="px-4 py-3">Course</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Date</th>
              <th className="px-4 py-3">Rating</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {purchases.map((p) => (
              <tr key={p.enrollmentId} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                <td className="px-4 py-3">
                  <div className="font-medium">{p.studentName}</div>
                  {p.studentEmail && <div className="text-xs text-slate-400 dark:text-slate-500">{p.studentEmail}</div>}
                </td>
                <td className="px-4 py-3">
                  <div className="font-medium">{p.courseName}</div>
                  {p.courseCode && <div className="font-mono text-xs text-slate-400 dark:text-slate-500">{p.courseCode}</div>}
                </td>
                <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{p.status ?? "—"}</td>
                <td className="px-4 py-3 whitespace-nowrap text-slate-500 dark:text-slate-400">{p.date ?? "—"}</td>
                <td className="px-4 py-3">
                  {p.rating ? <StarRating value={p.rating} size={14} /> : <span className="text-slate-400 dark:text-slate-500">—</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!loading && !error && purchases.length === 0 && (
        <p className="text-slate-500 dark:text-slate-400">No purchases yet.</p>
      )}
    </div>
  );
}
