"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { PublicCompany } from "@/lib/types";

export default function CompaniesPage() {
  const [companies, setCompanies] = useState<PublicCompany[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<PublicCompany[]>("/api/public/companies")
      .then(setCompanies)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Companies we train</h1>
        <p className="text-slate-500 dark:text-slate-400">
          Discover the businesses investing in their teams&apos; professional development.
        </p>
      </div>

      {loading && <p className="text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="text-red-600 dark:text-red-400">{error}</p>}

      <div className="grid items-start gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {companies.map((c) => (
          <article key={c.id} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-colors dark:border-slate-800 dark:bg-slate-900">
            <h3 className="font-semibold">{c.companyName}</h3>
            <p className="mt-1 text-sm text-indigo-600 dark:text-indigo-400">{c.industry ?? "—"}</p>
            <p className="mt-3 text-xs text-slate-400 dark:text-slate-500">
              {c.employeeCount != null ? `${c.employeeCount} employees` : "Team size n/a"}
            </p>
          </article>
        ))}
      </div>
    </div>
  );
}
