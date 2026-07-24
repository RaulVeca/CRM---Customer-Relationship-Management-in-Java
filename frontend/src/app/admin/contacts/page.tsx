"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import ExportMenu from "@/components/ExportMenu";
import type { Contact } from "@/lib/types";

const STATUS_COLORS: Record<string, string> = {
  NEW: "bg-zinc-100 text-zinc-700",
  CONTACTED: "bg-blue-100 text-blue-700",
  INTERESTED: "bg-amber-100 text-amber-700",
  QUALIFIED: "bg-emerald-100 text-emerald-700",
  ENROLLED: "bg-brand-100 text-brand-700",
  LOST: "bg-red-100 text-red-700",
};

export default function ContactsPage() {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  function load(query: string) {
    setLoading(true);
    const path = query.trim() ? `/api/contacts?q=${encodeURIComponent(query)}` : "/api/contacts?size=100";
    api
      .get<Contact[]>(path)
      .then((d) => {
        // Show individuals before companies (values stay unchanged).
        const rank = (c: Contact) => (c.contactType === "INDIVIDUAL" ? 0 : 1);
        const ordered = [...d].sort((a, b) => rank(a) - rank(b));
        setContacts(ordered);
        setError(null);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(() => { load(""); }, []);

  function name(c: Contact) {
    if (c.contactType === "CORPORATE") return c.companyName ?? "(company)";
    return [c.firstName, c.lastName].filter(Boolean).join(" ") || "(no name)";
  }

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between gap-2">
        <form
          onSubmit={(e) => { e.preventDefault(); load(q); }}
          className="flex gap-2"
        >
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search by name or email…"
            className="w-72 rounded-md border border-zinc-300 px-3 py-1.5 text-sm dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500"
          />
          <button className="rounded-md bg-brand-600 px-3 py-1.5 text-sm font-medium text-white">Search</button>
        </form>
        <ExportMenu resource="contacts" />
      </div>

      {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
      {loading && <p className="text-zinc-500 dark:text-zinc-400">Loading…</p>}

      <div className="overflow-hidden rounded-xl border border-zinc-200 bg-white shadow-sm transition-colors dark:border-zinc-800 dark:bg-zinc-900">
        <table className="w-full text-sm">
          <thead className="bg-[#2c3763] text-left text-xs font-semibold uppercase tracking-wide text-slate-100">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Type</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3 text-right">Score</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
            {contacts.map((c) => (
              <tr key={c.id} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                <td className="px-4 py-3 font-medium">{name(c)}</td>
                <td className="px-4 py-3 text-zinc-500 dark:text-zinc-400">{c.contactType}</td>
                <td className="px-4 py-3 text-zinc-500 dark:text-zinc-400">{c.email}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[c.leadStatus ?? ""] ?? "bg-zinc-100"}`}>
                    {c.leadStatus}
                  </span>
                </td>
                <td className="px-4 py-3 text-right font-semibold">{c.leadScore ?? "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && contacts.length === 0 && <p className="p-4 text-zinc-500 dark:text-zinc-400">No contacts found.</p>}
      </div>
    </div>
  );
}
