"use client";

import { useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api";
import { StarRating } from "@/components/Stars";
import type { Purchase } from "@/lib/types";

function formatPrice(p: number | null) {
  if (p == null) return "—";
  return new Intl.NumberFormat("ro-RO", {
    style: "currency",
    currency: "RON",
    maximumFractionDigits: 0,
  }).format(p);
}

const PAYMENT_BADGE: Record<string, string> = {
  PAID: "bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300",
  PARTIAL: "bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300",
  UNPAID: "bg-slate-100 text-slate-600 dark:bg-slate-700/50 dark:text-slate-300",
  REFUNDED: "bg-rose-50 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300",
  OVERDUE: "bg-red-50 text-red-700 dark:bg-red-500/15 dark:text-red-300",
};

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

  const { paidRevenue, paidCount } = useMemo(() => {
    let revenue = 0;
    let count = 0;
    for (const p of purchases) {
      if (p.paymentStatus === "PAID") {
        revenue += p.amount ?? 0;
        count += 1;
      }
    }
    return { paidRevenue: revenue, paidCount: count };
  }, [purchases]);

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
          <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Paid orders</p>
            <p className="mt-1 text-2xl font-bold">{paidCount}</p>
          </div>
          <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Revenue (paid)</p>
            <p className="mt-1 text-2xl font-bold">{formatPrice(paidRevenue)}</p>
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500 dark:bg-slate-800/50 dark:text-slate-400">
            <tr>
              <th className="px-4 py-3">Student</th>
              <th className="px-4 py-3">Course</th>
              <th className="px-4 py-3 text-right">Amount</th>
              <th className="px-4 py-3">Payment</th>
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
                <td className="px-4 py-3 text-right font-semibold">{formatPrice(p.amount)}</td>
                <td className="px-4 py-3">
                  {p.paymentStatus && (
                    <span className={`rounded px-2 py-0.5 text-xs font-medium ${PAYMENT_BADGE[p.paymentStatus] ?? PAYMENT_BADGE.UNPAID}`}>
                      {p.paymentStatus}
                    </span>
                  )}
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
