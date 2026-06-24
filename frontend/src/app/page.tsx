"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
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

      <section>
        <div className="mb-4 flex flex-wrap gap-2">
          <button
            onClick={() => setActive(null)}
            className={`rounded-full px-3 py-1 text-sm ${active === null ? "bg-indigo-600 text-white" : "bg-white text-slate-600 ring-1 ring-slate-200"}`}
          >
            All
          </button>
          {categories.map((c) => (
            <button
              key={c.name}
              onClick={() => setActive(c.name)}
              className={`rounded-full px-3 py-1 text-sm ${active === c.name ? "bg-indigo-600 text-white" : "bg-white text-slate-600 ring-1 ring-slate-200"}`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {loading && <p className="text-slate-500">Loading courses…</p>}
        {error && <p className="text-red-600">Could not load courses: {error}</p>}

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((c) => (
            <article key={c.id} className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
              <div className="mb-2 flex items-center justify-between">
                <span className="rounded bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700">
                  {c.categoryLabel ?? c.category}
                </span>
                <span className="text-xs text-slate-400">{c.level}</span>
              </div>
              <h3 className="font-semibold">{c.name}</h3>
              <p className="mt-1 flex-1 text-sm text-slate-500">{c.description}</p>
              <div className="mt-4 flex items-center justify-between text-sm">
                <span className="text-slate-500">{c.durationHours ?? "—"} h</span>
                <span className="font-semibold text-slate-900">{formatPrice(c.priceIndividual)}</span>
              </div>
            </article>
          ))}
        </div>
        {!loading && courses.length === 0 && !error && (
          <p className="text-slate-500">No courses in this category yet.</p>
        )}
      </section>
    </div>
  );
}
