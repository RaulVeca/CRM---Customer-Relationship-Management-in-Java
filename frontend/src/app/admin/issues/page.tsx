"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { IssueReport } from "@/lib/types";

/** What the confirm window is about to delete: one report, or the whole list. */
type PendingDelete = { kind: "one"; issue: IssueReport } | { kind: "all" };

const OPEN = "OPEN";
const SOLVED = "SOLVED";

export default function AdminIssuesPage() {
  const [issues, setIssues] = useState<IssueReport[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  // Deletions are irreversible, so both actions go through a confirm window.
  const [pending, setPending] = useState<PendingDelete | null>(null);
  const [deleting, setDeleting] = useState(false);
  // Id of the report whose status is currently being flipped, so only that row's
  // toggle is disabled while its request is in flight.
  const [savingId, setSavingId] = useState<number | null>(null);

  useEffect(() => {
    api
      .get<IssueReport[]>("/api/issues")
      .then(setIssues)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  /** Flips a report between OPEN and SOLVED — the same button does both ways. */
  async function toggleStatus(issue: IssueReport) {
    const next = issue.status === SOLVED ? OPEN : SOLVED;
    setSavingId(issue.id);
    setError(null);
    try {
      const updated = await api.patch<IssueReport>(
        `/api/issues/${issue.id}/status?status=${next}`,
      );
      setIssues((prev) => prev.map((it) => (it.id === issue.id ? updated : it)));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSavingId(null);
    }
  }

  async function confirmDelete() {
    if (!pending) return;
    setDeleting(true);
    setError(null);
    try {
      if (pending.kind === "all") {
        await api.del<{ deleted: number }>("/api/issues");
        setIssues([]);
      } else {
        const { id } = pending.issue;
        await api.del<void>(`/api/issues/${id}`);
        setIssues((prev) => prev.filter((it) => it.id !== id));
      }
      setPending(null);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setDeleting(false);
    }
  }

  const openCount = issues.filter((it) => it.status !== SOLVED).length;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-lg font-semibold">Reported issues</h2>
        <button
          onClick={() => setPending({ kind: "all" })}
          disabled={issues.length === 0 || loading}
          className="rounded-full border border-red-300 px-4 py-1.5 text-sm font-semibold text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-red-500/40 dark:text-red-400 dark:hover:bg-red-500/10"
        >
          Delete all
        </button>
      </div>

      {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
      {loading && <p className="text-zinc-500 dark:text-zinc-400">Loading…</p>}

      {!loading && !error && (
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Total reports</p>
            <p className="mt-1 text-2xl font-bold">{issues.length}</p>
          </div>
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Open</p>
            <p className="mt-1 text-2xl font-bold text-brand-600 dark:text-brand-400">{openCount}</p>
          </div>
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Solved</p>
            <p className="mt-1 text-2xl font-bold text-emerald-600 dark:text-emerald-400">
              {issues.length - openCount}
            </p>
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
              <th className="px-4 py-3 text-right">Actions</th>
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
                  <span
                    className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                      it.status === SOLVED
                        ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300"
                        : "bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300"
                    }`}
                  >
                    {it.status}
                  </span>
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-zinc-500 dark:text-zinc-400">{it.date ?? "—"}</td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-3 whitespace-nowrap">
                    <button
                      onClick={() => toggleStatus(it)}
                      disabled={savingId === it.id}
                      className="text-xs font-medium text-brand-600 hover:underline disabled:opacity-50 disabled:hover:no-underline dark:text-brand-400"
                    >
                      {savingId === it.id
                        ? "Saving…"
                        : it.status === SOLVED
                          ? "Reopen"
                          : "Mark solved"}
                    </button>
                    <button
                      onClick={() => setPending({ kind: "one", issue: it })}
                      className="text-xs font-medium text-red-500 hover:underline dark:text-red-400"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!loading && !error && issues.length === 0 && (
        <p className="text-zinc-500 dark:text-zinc-400">No issues reported yet.</p>
      )}

      {/* Confirm window — deleting a report cannot be undone. */}
      {pending && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-900/50 p-4">
          <div className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-6 shadow-xl dark:border-zinc-800 dark:bg-zinc-900">
            <h3 className="text-lg font-bold text-zinc-900 dark:text-white">
              {pending.kind === "all" ? "Delete all reports?" : "Delete this report?"}
            </h3>
            <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              {pending.kind === "all"
                ? `All ${issues.length} reported ${issues.length === 1 ? "issue" : "issues"} will be permanently removed. This cannot be undone.`
                : "The report will be permanently removed. This cannot be undone."}
            </p>

            {pending.kind === "one" && (
              <p className="mt-4 max-h-32 overflow-y-auto whitespace-pre-wrap rounded-xl border border-zinc-200 bg-zinc-50 p-3 text-sm text-zinc-700 dark:border-zinc-800 dark:bg-zinc-800/40 dark:text-zinc-300">
                {pending.issue.message}
              </p>
            )}

            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setPending(null)}
                disabled={deleting}
                className="rounded-lg border border-zinc-300 bg-white px-4 py-2 text-sm font-semibold text-zinc-700 transition hover:bg-zinc-50 disabled:opacity-60 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800"
              >
                Cancel
              </button>
              <button
                onClick={confirmDelete}
                disabled={deleting}
                className="rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-700 disabled:opacity-60"
              >
                {deleting ? "Deleting…" : pending.kind === "all" ? "Delete all" : "Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
