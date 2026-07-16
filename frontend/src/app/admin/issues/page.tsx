"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { IssueReport } from "@/lib/types";

export default function AdminIssuesPage() {
  const [issues, setIssues] = useState<IssueReport[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<IssueReport[]>("/api/issues")
      .then(setIssues)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Reported issues</h2>

      {error && <p className="text-red-600">{error}</p>}
      {loading && <p className="text-zinc-500">Loading…</p>}

      {!loading && !error && (
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Total reports</p>
            <p className="mt-1 text-2xl font-bold">{issues.length}</p>
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
        <table className="w-full text-sm">
          <thead className="bg-zinc-50 text-left text-xs uppercase text-zinc-500 dark:bg-zinc-800/50 dark:text-zinc-400">
            <tr>
              <th className="px-4 py-3">Reporter</th>
              <th className="px-4 py-3">Issue</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Reported</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
            {issues.map((it) => (
              <tr key={it.id} className="align-top hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                <td className="px-4 py-3">
                  <div className="font-medium">{it.reporterName ?? "Anonymous"}</div>
                  {it.reporterEmail && (
                    <div className="text-xs text-zinc-400 dark:text-zinc-500">{it.reporterEmail}</div>
                  )}
                </td>
                <td className="px-4 py-3 text-zinc-700 dark:text-zinc-300">
                  <p className="max-w-xl whitespace-pre-wrap">{it.message}</p>
                </td>
                <td className="px-4 py-3">
                  <span className="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/10 dark:text-brand-300">
                    {it.status}
                  </span>
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-zinc-500 dark:text-zinc-400">{it.date ?? "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!loading && !error && issues.length === 0 && (
        <p className="text-zinc-500 dark:text-zinc-400">No issues reported yet.</p>
      )}
    </div>
  );
}
