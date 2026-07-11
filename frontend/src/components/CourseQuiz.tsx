"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { SparkleIcon } from "@/components/icons";
import type { Category, CourseRecommendation } from "@/lib/types";

const LEVELS = [
  { value: "BEGINNER", label: "Beginner" },
  { value: "INTERMEDIATE", label: "Intermediate" },
  { value: "ADVANCED", label: "Advanced" },
];

/**
 * Public "find your course" quiz for individual visitors. Collects a short
 * self-reported profile and asks Claude (via the backend) for personalised
 * course recommendations. Renders nothing if the AI feature is disabled.
 */
export default function CourseQuiz() {
  const [enabled, setEnabled] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [interests, setInterests] = useState<Set<string>>(new Set());
  const [level, setLevel] = useState("");
  const [goal, setGoal] = useState("");
  const [results, setResults] = useState<CourseRecommendation[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<{ enabled: boolean }>("/api/ai/status").then((s) => setEnabled(s.enabled)).catch(() => {});
    api.get<Category[]>("/api/public/categories").then(setCategories).catch(() => {});
  }, []);

  function toggleInterest(label: string) {
    setInterests((prev) => {
      const next = new Set(prev);
      if (next.has(label)) next.delete(label);
      else next.add(label);
      return next;
    });
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResults(null);
    try {
      const recs = await api.post<CourseRecommendation[]>("/api/ai/recommendations/visitor", {
        interests: [...interests],
        experienceLevel: level || null,
        goal: goal.trim() || null,
      });
      setResults(recs);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  }

  if (!enabled) return null;

  return (
    <section className="rounded-2xl border border-brand-100 bg-brand-50/60 p-6 dark:border-brand-500/20 dark:bg-brand-500/10">
      <div className="flex items-center gap-2">
        <SparkleIcon className="h-5 w-5 text-brand-500 dark:text-brand-400" />
        <h2 className="text-lg font-semibold text-ink dark:text-white">Not sure where to start?</h2>
      </div>
      <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
        Tell us a bit about yourself and our AI advisor will match you with the right courses.
      </p>

      <form onSubmit={submit} className="mt-4 space-y-4">
        <div>
          <p className="mb-2 text-xs font-medium uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
            What interests you?
          </p>
          <div className="flex flex-wrap gap-2">
            {categories.map((c) => {
              const on = interests.has(c.label);
              return (
                <button
                  type="button"
                  key={c.name}
                  onClick={() => toggleInterest(c.label)}
                  aria-pressed={on}
                  className={`rounded-full px-3 py-1 text-sm transition-colors ${
                    on
                      ? "bg-brand-500 text-white"
                      : "bg-white text-zinc-600 ring-1 ring-black/10 hover:bg-canvas dark:bg-zinc-800 dark:text-zinc-300 dark:ring-white/10 dark:hover:bg-zinc-700"
                  }`}
                >
                  {c.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="flex flex-wrap items-end gap-4">
          <div>
            <p className="mb-2 text-xs font-medium uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
              Your level
            </p>
            <div className="flex gap-2">
              {LEVELS.map((l) => (
                <button
                  type="button"
                  key={l.value}
                  onClick={() => setLevel(l.value)}
                  aria-pressed={level === l.value}
                  className={`rounded-full px-3 py-1.5 text-sm transition-colors ${
                    level === l.value
                      ? "bg-brand-500 text-white"
                      : "bg-white text-zinc-600 ring-1 ring-black/10 hover:bg-canvas dark:bg-zinc-800 dark:text-zinc-300 dark:ring-white/10 dark:hover:bg-zinc-700"
                  }`}
                >
                  {l.label}
                </button>
              ))}
            </div>
          </div>

          <div className="min-w-[14rem] flex-1">
            <p className="mb-2 text-xs font-medium uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
              Your goal <span className="font-normal lowercase">(optional)</span>
            </p>
            <input
              value={goal}
              onChange={(e) => setGoal(e.target.value)}
              placeholder="e.g. career change into IT, upskilling at work…"
              className="w-full rounded-full border border-zinc-300 px-3 py-1.5 text-sm outline-none focus:border-brand-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading || interests.size === 0}
          className="rounded-full bg-brand-500 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-600 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-50"
        >
          {loading ? "Claude is matching courses…" : "Get my recommendations"}
        </button>
      </form>

      {error && <p className="mt-4 text-sm text-red-600 dark:text-red-400">Could not get recommendations: {error}</p>}

      {results && (
        <div className="mt-5 border-t border-brand-100 pt-4 dark:border-brand-500/20">
          {results.length === 0 ? (
            <p className="text-sm text-zinc-500 dark:text-zinc-400">
              No strong matches yet — try selecting a few more interests.
            </p>
          ) : (
            <>
              <p className="mb-3 text-sm font-medium text-ink dark:text-white">Recommended for you</p>
              <ul className="space-y-2">
                {results.map((r) => (
                  <li
                    key={r.courseId}
                    className="flex items-start gap-3 rounded-xl border border-black/5 bg-white p-3 shadow-sm dark:border-white/10 dark:bg-zinc-900"
                  >
                    <span className="mt-0.5 shrink-0 rounded-full bg-brand-500 px-2 py-0.5 text-xs font-bold text-white">
                      {r.matchScore}
                    </span>
                    <div>
                      <p className="font-medium text-ink dark:text-white">{r.courseName}</p>
                      <p className="text-sm text-zinc-500 dark:text-zinc-400">{r.reason}</p>
                    </div>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}
    </section>
  );
}
