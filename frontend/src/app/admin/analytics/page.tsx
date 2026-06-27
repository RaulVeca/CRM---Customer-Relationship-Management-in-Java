"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Analytics } from "@/lib/types";

type Tone = "good" | "warn" | "bad" | "neutral";

const TONE_TEXT: Record<Tone, string> = {
  good: "text-emerald-600 dark:text-emerald-400",
  warn: "text-amber-600 dark:text-amber-400",
  bad: "text-red-600 dark:text-red-400",
  neutral: "text-slate-500 dark:text-slate-400",
};

function pct(n: number) {
  return `${n}%`;
}

function MetricCard({
  label,
  value,
  detail,
  insight,
  tone,
}: {
  label: string;
  value: string;
  detail: string;
  insight: string;
  tone: Tone;
}) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">{label}</p>
      <p className="mt-1 text-3xl font-bold text-slate-900 dark:text-slate-100">{value}</p>
      <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">{detail}</p>
      <p className={`mt-3 text-sm font-medium ${TONE_TEXT[tone]}`}>{insight}</p>
    </div>
  );
}

function Bars({ data }: { data: Record<string, number> }) {
  const entries = Object.entries(data);
  if (entries.length === 0) return <p className="text-sm text-slate-400 dark:text-slate-500">No data.</p>;
  const max = Math.max(1, ...Object.values(data));
  return (
    <div className="space-y-2">
      {entries.map(([k, v]) => (
        <div key={k} className="flex items-center gap-3">
          <span className="w-40 shrink-0 truncate text-xs text-slate-500 dark:text-slate-400" title={k}>{k}</span>
          <div className="h-4 flex-1 rounded bg-slate-100 dark:bg-slate-800">
            <div className="h-4 rounded bg-indigo-500" style={{ width: `${(v / max) * 100}%` }} />
          </div>
          <span className="w-8 text-right text-xs font-medium">{v}</span>
        </div>
      ))}
    </div>
  );
}

function Panel({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h3 className="mb-4 font-semibold">{title}</h3>
      {children}
    </div>
  );
}

export default function AdminAnalyticsPage() {
  const [data, setData] = useState<Analytics | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Analytics>("/api/stats/analytics").then(setData).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="text-red-600">{error}</p>;
  if (!data) return <p className="text-slate-500">Loading analytics…</p>;

  const { demographics, churn, ctr } = data;

  const leadTone: Tone = churn.leadChurnRate >= 40 ? "bad" : churn.leadChurnRate >= 20 ? "warn" : "good";
  const dropTone: Tone = churn.enrollmentDropoutRate >= 20 ? "bad" : churn.enrollmentDropoutRate >= 10 ? "warn" : "good";
  const lossTone: Tone = churn.opportunityLossRate >= 50 ? "bad" : churn.opportunityLossRate >= 30 ? "warn" : "good";
  const ctrTone: Tone = ctr.totalImpressions === 0 ? "neutral" : ctr.overallCtr >= 30 ? "good" : ctr.overallCtr >= 15 ? "warn" : "bad";

  // Actionable: courses shown a lot but rarely clicked.
  const underperforming = ctr.courses
    .filter((c) => c.impressions >= 5 && c.ctr < 20)
    .sort((a, b) => b.impressions - a.impressions)
    .slice(0, 3);

  return (
    <div className="space-y-6">
      <h2 className="text-lg font-semibold">Analytics</h2>

      {/* Headline metrics */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          label="Lead churn rate"
          value={pct(churn.leadChurnRate)}
          detail={`${churn.lostLeads} lost vs ${churn.enrolledLeads} enrolled`}
          tone={leadTone}
          insight={
            leadTone === "good"
              ? "Healthy — most qualified leads convert."
              : "Many leads are lost — tighten follow-up & qualification."
          }
        />
        <MetricCard
          label="Enrollment drop-out"
          value={pct(churn.enrollmentDropoutRate)}
          detail={`${churn.droppedEnrollments} of ${churn.totalEnrollments} enrollments`}
          tone={dropTone}
          insight={
            dropTone === "good"
              ? "Low drop-out — students stay engaged."
              : "Students dropping/cancelling — review delivery & onboarding."
          }
        />
        <MetricCard
          label="Opportunity loss rate"
          value={pct(churn.opportunityLossRate)}
          detail={`${churn.lostOpportunities} lost vs ${churn.wonOpportunities} won`}
          tone={lossTone}
          insight={
            lossTone === "good"
              ? "Winning most deals."
              : "Losing many B2B deals — revisit pricing & proposals."
          }
        />
        <MetricCard
          label="Catalog click-through rate"
          value={ctr.totalImpressions === 0 ? "—" : pct(ctr.overallCtr)}
          detail={`${ctr.totalClicks} clicks / ${ctr.totalImpressions} views`}
          tone={ctrTone}
          insight={
            ctr.totalImpressions === 0
              ? "No catalog views tracked yet."
              : ctrTone === "good"
                ? "Strong interest in the catalog."
                : "Low engagement — improve course titles, pricing & descriptions."
          }
        />
      </div>

      {/* Click-through rate per course */}
      <Panel title="Click-through rate by course">
        {ctr.courses.length === 0 ? (
          <p className="text-sm text-slate-400 dark:text-slate-500">
            No catalog activity tracked yet. Visit the public site and open a few courses to populate this.
          </p>
        ) : (
          <>
            {underperforming.length > 0 && (
              <div className="mb-4 rounded-lg bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:bg-amber-500/10 dark:text-amber-300">
                <span className="font-semibold">Needs attention:</span>{" "}
                {underperforming.map((c) => c.courseName).join(", ")} — shown often but rarely opened.
              </div>
            )}
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="text-left text-xs uppercase text-slate-500 dark:text-slate-400">
                  <tr>
                    <th className="py-2">Course</th>
                    <th className="py-2 text-right">Views</th>
                    <th className="py-2 text-right">Clicks</th>
                    <th className="py-2 text-right">CTR</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {ctr.courses.map((c) => (
                    <tr key={c.courseId}>
                      <td className="py-2 font-medium">{c.courseName}</td>
                      <td className="py-2 text-right">{c.impressions}</td>
                      <td className="py-2 text-right">{c.clicks}</td>
                      <td className="py-2 text-right font-semibold">{pct(c.ctr)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </Panel>

      {/* Demographic data */}
      <div>
        <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
          Demographic data · {demographics.totalContacts} contacts
        </h3>
        <div className="grid gap-6 lg:grid-cols-2">
          <Panel title="By contact type">
            <Bars data={demographics.byType} />
          </Panel>
          <Panel title="By experience level">
            <Bars data={demographics.byExperience} />
          </Panel>
          <Panel title="By lead source">
            <Bars data={demographics.byLeadSource} />
          </Panel>
          <Panel title="By county (top)">
            <Bars data={demographics.byCounty} />
          </Panel>
          <Panel title="By industry (top)">
            <Bars data={demographics.byIndustry} />
          </Panel>
        </div>
      </div>
    </div>
  );
}
