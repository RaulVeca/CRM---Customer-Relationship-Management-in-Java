"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import CourseQuiz from "@/components/CourseQuiz";
import type { Category, PublicCourse } from "@/lib/types";

function formatPrice(p: number | null) {
  if (p == null) return "—";
  return new Intl.NumberFormat("ro-RO", { style: "currency", currency: "RON", maximumFractionDigits: 0 }).format(p);
}

export default function Home() {
  const [courses, setCourses] = useState<PublicCourse[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [active, setActive] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const [toast, setToast] = useState(false);
  const toastTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  function toggleExpanded(id: number) {
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function notifyComingSoon() {
    setToast(true);
    if (toastTimer.current) clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToast(false), 2000);
  }

  useEffect(() => {
    api.get<Category[]>("/api/public/categories").then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    const path = active ? `/api/public/courses?category=${active}` : "/api/public/courses";
    api
      .get<PublicCourse[]>(path)
      .then((data) => {
        setCourses(data);
        setError(null);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [active]);

  return (
    <div className="space-y-8">
      <section className="rounded-2xl bg-gradient-to-br from-indigo-600 to-violet-600 px-8 py-12 text-white">
        <h1 className="text-3xl font-bold">Level up your IT career</h1>
        <p className="mt-2 max-w-xl text-indigo-100">
          Browse professional training courses in programming, AI, data science and more —
          delivered to individuals and corporate teams.
        </p>
      </section>

      <CourseQuiz />

      <section>
        <div className="mb-4 flex flex-wrap gap-2">
          <button
            onClick={() => setActive(null)}
            className={`rounded-full px-3 py-1 text-sm ${active === null ? "bg-indigo-600 text-white" : "bg-white text-slate-600 ring-1 ring-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:ring-slate-700"}`}
          >
            All
          </button>
          {categories.map((c) => (
            <button
              key={c.name}
              onClick={() => setActive(c.name)}
              className={`rounded-full px-3 py-1 text-sm ${active === c.name ? "bg-indigo-600 text-white" : "bg-white text-slate-600 ring-1 ring-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:ring-slate-700"}`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {loading && <p className="text-slate-500 dark:text-slate-400">Loading courses…</p>}
        {error && <p className="text-red-600 dark:text-red-400">Could not load courses: {error}</p>}

        <div className="grid items-start gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((c) => (
            <article key={c.id} className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-colors dark:border-slate-800 dark:bg-slate-900">
              <div className="mb-2 flex items-center justify-between">
                <span className="rounded bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300">
                  {c.categoryLabel ?? c.category}
                </span>
                <span className="text-xs text-slate-400 dark:text-slate-500">{c.level}</span>
              </div>
              <h3 className="font-semibold">{c.name}</h3>
              <div className="mt-4 flex items-center justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400">{c.durationHours ?? "—"} h</span>
                <span className="font-semibold text-slate-900 dark:text-slate-100">{formatPrice(c.priceIndividual)}</span>
              </div>

              <button
                type="button"
                onClick={() => toggleExpanded(c.id)}
                aria-expanded={expanded.has(c.id)}
                className="mt-4 flex items-center justify-center gap-1 rounded-md border border-slate-200 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
              >
                More
                <svg
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  className={`h-4 w-4 transition-transform ${expanded.has(c.id) ? "rotate-180" : ""}`}
                >
                  <path
                    fillRule="evenodd"
                    d="M5.23 7.21a.75.75 0 011.06.02L10 11.06l3.71-3.83a.75.75 0 111.08 1.04l-4.25 4.39a.75.75 0 01-1.08 0L5.21 8.27a.75.75 0 01.02-1.06z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>

              <div
                className={`grid transition-[grid-template-rows] duration-300 ease-in-out ${
                  expanded.has(c.id) ? "grid-rows-[1fr]" : "grid-rows-[0fr]"
                }`}
              >
                <div className="overflow-hidden">
                  <div className="mt-3 border-t border-slate-100 pt-3 dark:border-slate-800">
                    <p className="text-sm text-slate-600 dark:text-slate-300">
                      {c.description ?? "No description available for this course."}
                    </p>
                    <button
                      type="button"
                      tabIndex={expanded.has(c.id) ? 0 : -1}
                      onClick={notifyComingSoon}
                      className="mt-4 w-full rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:from-indigo-700 hover:to-violet-700 hover:shadow active:scale-[0.99]"
                    >
                      Buy this course
                    </button>
                  </div>
                </div>
              </div>
            </article>
          ))}
        </div>
        {!loading && courses.length === 0 && !error && (
          <p className="text-slate-500 dark:text-slate-400">No courses in this category yet.</p>
        )}
      </section>

      <div
        role="status"
        aria-live="polite"
        className={`pointer-events-none fixed inset-x-0 bottom-8 z-50 flex justify-center px-4 transition-opacity duration-700 ${
          toast ? "opacity-100" : "opacity-0"
        }`}
      >
        <div className="rounded-lg bg-slate-900 px-5 py-3 text-center text-sm font-medium text-white shadow-lg ring-1 ring-black/10 dark:bg-slate-800 dark:ring-white/10">
          This feature is not available yet — the application is still under development.
        </div>
      </div>
    </div>
  );
}
