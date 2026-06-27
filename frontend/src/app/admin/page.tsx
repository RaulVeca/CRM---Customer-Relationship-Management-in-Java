"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { DashboardStats } from "@/lib/types";

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 p-5 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 text-2xl font-bold text-slate-900 dark:text-slate-100">{value}</p>
    </div>
  );
}

function Bars({ data }: { data: Record<string, number> }) {
  const max = Math.max(1, ...Object.values(data));
  return (
    <div className="space-y-2">
      {Object.entries(data).map(([k, v]) => (
        <div key={k} className="flex items-center gap-3">
          <span className="w-40 shrink-0 text-xs text-slate-500">{k}</span>
          <div className="h-4 flex-1 rounded bg-slate-100">
            <div className="h-4 rounded bg-indigo-500" style={{ width: `${(v / max) * 100}%` }} />
          </div>
          <span className="w-8 text-right text-xs font-medium">{v}</span>
        </div>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<DashboardStats>("/api/stats/dashboard").then(setStats).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="text-red-600">{error}</p>;
  if (!stats) return <p className="text-slate-500">Loading dashboard…</p>;

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total contacts" value={stats.totalContacts} />
        <StatCard label="Hot leads" value={stats.hotLeads} />
        <StatCard label="Active opportunities" value={stats.activeOpportunities} />
        <StatCard
          label="Weighted pipeline"
          value={new Intl.NumberFormat("ro-RO", { style: "currency", currency: "RON", maximumFractionDigits: 0 }).format(stats.weightedPipelineValue)}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 p-5 shadow-sm">
          <h3 className="mb-4 font-semibold">Contacts by status</h3>
          <Bars data={stats.contactsByStatus} />
        </div>
        <div className="rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 p-5 shadow-sm">
          <h3 className="mb-4 font-semibold">Pipeline by stage</h3>
          <Bars data={stats.pipelineByStage} />
        </div>
      </div>
    </div>
  );
}
