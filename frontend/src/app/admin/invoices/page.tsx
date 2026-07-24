"use client";

import { useEffect, useState } from "react";
import { api, API_BASE } from "@/lib/api";
import type { Invoice } from "@/lib/types";

const fmtUSD = (n: number) =>
  Number.isInteger(n) ? `$${n}` : `$${n.toFixed(2)}`;

export default function AdminInvoicesPage() {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<Invoice[]>("/api/invoices")
      .then(setInvoices)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const totalRevenue = invoices.reduce((sum, i) => sum + i.total, 0);
  const discounted = invoices.filter((i) => i.discountRate > 0).length;

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold tracking-tight">Invoices</h2>
      <p className="text-sm text-zinc-500 dark:text-zinc-400">
        Generated automatically for every online trainer session booked. Priced at the
        hourly rate, with the −60% employee discount applied where it applies. Paid on
        booking.
      </p>

      {error && <p className="text-red-600">{error}</p>}
      {loading && <p className="text-zinc-500">Loading…</p>}

      {!loading && !error && (
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Invoices</p>
            <p className="mt-1 text-2xl font-bold">{invoices.length}</p>
          </div>
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Total collected</p>
            <p className="mt-1 text-2xl font-bold">{fmtUSD(totalRevenue)}</p>
          </div>
          <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">With employee discount</p>
            <p className="mt-1 text-2xl font-bold">{discounted}</p>
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
        <table className="w-full text-sm">
          <thead className="bg-[#2c3763] text-left text-xs font-semibold uppercase tracking-wide text-slate-100">
            <tr>
              <th className="px-4 py-3">Invoice</th>
              <th className="px-4 py-3">Client</th>
              <th className="px-4 py-3">Session</th>
              <th className="px-4 py-3 text-right">Hours</th>
              <th className="px-4 py-3 text-right">Subtotal</th>
              <th className="px-4 py-3 text-right">Discount</th>
              <th className="px-4 py-3 text-right">Total</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Date</th>
              <th className="px-4 py-3 text-right">PDF</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
            {invoices.map((inv) => (
              <tr key={inv.id} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                <td className="px-4 py-3 font-mono text-xs">{inv.invoiceNumber}</td>
                <td className="px-4 py-3 text-zinc-500 dark:text-zinc-400">
                  {inv.clientEmail ?? "—"}
                </td>
                <td className="px-4 py-3 text-zinc-500 dark:text-zinc-400">
                  #{inv.sessionId ?? "—"}
                </td>
                <td className="px-4 py-3 text-right">{inv.hours}h</td>
                <td className="px-4 py-3 whitespace-nowrap text-right">{fmtUSD(inv.subtotal)}</td>
                <td className="px-4 py-3 whitespace-nowrap text-right">
                  {inv.discountRate > 0 ? (
                    <span className="font-medium text-emerald-600 dark:text-emerald-400">
                      −{Math.round(inv.discountRate * 100)}% (−{fmtUSD(inv.discountAmount)})
                    </span>
                  ) : (
                    <span className="text-zinc-400 dark:text-zinc-500">—</span>
                  )}
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-right font-semibold">{fmtUSD(inv.total)}</td>
                <td className="px-4 py-3">
                  <span className="inline-flex rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300">
                    {inv.status}
                  </span>
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-zinc-500 dark:text-zinc-400">
                  {inv.issueDate ?? "—"}
                </td>
                <td className="px-4 py-3 text-right">
                  <a
                    href={`${API_BASE}/api/invoices/${inv.id}/pdf`}
                    className="inline-flex items-center gap-1 rounded-md border border-zinc-300 px-2.5 py-1 text-xs font-medium text-zinc-600 transition-colors hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-800"
                  >
                    <svg viewBox="0 0 20 20" fill="currentColor" className="h-3.5 w-3.5">
                      <path
                        fillRule="evenodd"
                        d="M10 3a.75.75 0 01.75.75v6.19l1.72-1.72a.75.75 0 111.06 1.06l-3 3a.75.75 0 01-1.06 0l-3-3a.75.75 0 111.06-1.06l1.72 1.72V3.75A.75.75 0 0110 3zM3.5 13.75a.75.75 0 01.75.75v1.25c0 .138.112.25.25.25h11a.25.25 0 00.25-.25V14.5a.75.75 0 011.5 0v1.25A1.75 1.75 0 0115.5 17.5h-11a1.75 1.75 0 01-1.75-1.75V14.5a.75.75 0 01.75-.75z"
                        clipRule="evenodd"
                      />
                    </svg>
                    Download PDF
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!loading && !error && invoices.length === 0 && (
        <p className="text-zinc-500 dark:text-zinc-400">No invoices yet.</p>
      )}
    </div>
  );
}
