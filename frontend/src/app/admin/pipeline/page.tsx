"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import ExportMenu from "@/components/ExportMenu";
import { SparkleIcon } from "@/components/icons";
import type { Opportunity } from "@/lib/types";

const STAGES = [
  "LEAD_QUALIFICATION",
  "NEEDS_ANALYSIS",
  "PROPOSAL_SENT",
  "NEGOTIATION",
  "CONTRACT_REVIEW",
];

export default function PipelinePage() {
  const [opps, setOpps] = useState<Opportunity[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const [aiEnabled, setAiEnabled] = useState(false);
  const [advice, setAdvice] = useState<{ title: string; text: string } | null>(null);
  const [adviceLoading, setAdviceLoading] = useState<number | null>(null);

  useEffect(() => {
    api
      .get<Opportunity[]>("/api/opportunities")
      .then(setOpps)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
    api.get<{ enabled: boolean }>("/api/ai/status").then((s) => setAiEnabled(s.enabled)).catch(() => {});
  }, []);

  async function salesAssist(o: Opportunity) {
    setAdviceLoading(o.id);
    try {
      const res = await api.get<{ advice: string }>(`/api/ai/sales/opportunity/${o.id}`);
      setAdvice({ title: o.title, text: res.advice });
    } catch (e) {
      setAdvice({ title: o.title, text: "Could not generate advice: " + (e as Error).message });
    } finally {
      setAdviceLoading(null);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Sales pipeline</h2>
        <ExportMenu resource="pipeline" />
      </div>
      {error && <p className="text-red-600">{error}</p>}
      {loading && <p className="text-slate-500">Loading…</p>}

      <div className="grid gap-3 md:grid-cols-3 xl:grid-cols-5">
        {STAGES.map((stage) => {
          const inStage = opps.filter((o) => o.stage === stage);
          return (
            <div key={stage} className="rounded-xl bg-slate-100 p-3 dark:bg-slate-800/40">
              <h3 className="mb-2 text-xs font-semibold uppercase text-slate-500">
                {stage.replace(/_/g, " ")} ({inStage.length})
              </h3>
              <div className="space-y-2">
                {inStage.map((o) => (
                  <div key={o.id} className="rounded-lg border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 p-3 shadow-sm">
                    <p className="text-sm font-medium">{o.title}</p>
                    <p className="mt-1 text-xs text-slate-500">
                      {o.estimatedValue != null ? `${o.estimatedValue} RON` : "—"} · {o.probabilityPercent ?? 0}%
                    </p>
                    {aiEnabled && (
                      <button
                        onClick={() => salesAssist(o)}
                        disabled={adviceLoading === o.id}
                        className="mt-2 text-xs font-medium text-violet-600 hover:underline disabled:opacity-50"
                      >
                        {adviceLoading === o.id ? (
                          "Thinking…"
                        ) : (
                          <span className="inline-flex items-center gap-1">
                            <SparkleIcon className="h-3.5 w-3.5" />
                            Sales assistant
                          </span>
                        )}
                      </button>
                    )}
                  </div>
                ))}
                {inStage.length === 0 && <p className="text-xs text-slate-400">Empty</p>}
              </div>
            </div>
          );
        })}
      </div>

      {advice && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
          onClick={() => setAdvice(null)}
        >
          <div
            className="max-h-[80vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl dark:bg-slate-900"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="mb-3 flex items-center justify-between">
              <h3 className="flex items-center gap-1.5 font-semibold">
                <SparkleIcon className="h-4 w-4 text-violet-500" />
                Sales assistant — {advice.title}
              </h3>
              <button onClick={() => setAdvice(null)} className="text-slate-400 hover:text-slate-700">×</button>
            </div>
            <div className="whitespace-pre-wrap text-sm text-slate-700 dark:text-slate-200">{advice.text}</div>
          </div>
        </div>
      )}
    </div>
  );
}
