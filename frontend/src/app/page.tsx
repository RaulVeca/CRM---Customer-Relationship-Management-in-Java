"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import CourseQuiz from "@/components/CourseQuiz";
import CourseCard from "@/components/CourseCard";
import type { Category, MyPurchase, PublicCourse } from "@/lib/types";

const SEEN_KEY = "ctrSeenCourses";

/**
 * Count a catalog impression only the first time this browser sees a course.
 * Re-entering Courses, refreshing or switching category no longer inflates the
 * view count — the same visitor is only counted once per course.
 */
function trackImpressions(courseIds: number[]) {
  if (courseIds.length === 0) return;
  let seen: number[] = [];
  try {
    seen = JSON.parse(localStorage.getItem(SEEN_KEY) ?? "[]");
  } catch {
    seen = [];
  }
  const seenSet = new Set(seen);
  const fresh = courseIds.filter((id) => !seenSet.has(id));
  if (fresh.length === 0) return;

  api.post("/api/public/metrics/impressions", { courseIds: fresh }).catch(() => {});
  try {
    localStorage.setItem(SEEN_KEY, JSON.stringify([...seenSet, ...fresh]));
  } catch {
    /* localStorage unavailable — skip persistence */
  }
}

export default function Home() {
  const [courses, setCourses] = useState<PublicCourse[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [active, setActive] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // Course ids the logged-in contact has already bought (personal to them).
  const [purchasedIds, setPurchasedIds] = useState<Set<number>>(new Set());
  // Greeting name for a signed-in contact (client portal only). Null when signed
  // out or for any non-USER session, so the greeting stays exclusive to contacts.
  const [greetingName, setGreetingName] = useState<string | null>(null);

  useEffect(() => {
    api.get<Category[]>("/api/public/categories").then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    const session = getSession();
    if (session?.role !== "USER") return;
    const name = [session.firstName, session.lastName].filter(Boolean).join(" ") || session.email;
    setGreetingName(name);
  }, []);

  useEffect(() => {
    const session = getSession();
    if (!session) return;
    api
      .get<MyPurchase[]>(`/api/public/me/purchases?email=${encodeURIComponent(session.email)}`)
      .then((data) => setPurchasedIds(new Set(data.map((p) => p.courseId))))
      .catch(() => {});
  }, []);

  function markPurchased(courseId: number) {
    setPurchasedIds((prev) => {
      const next = new Set(prev);
      next.add(courseId);
      return next;
    });
  }

  useEffect(() => {
    setLoading(true);
    const path = active ? `/api/public/courses?category=${active}` : "/api/public/courses";
    api
      .get<PublicCourse[]>(path)
      .then((data) => {
        setCourses(data);
        setError(null);
        // Track catalog impressions (feeds the admin click-through-rate metric),
        // counting each course only once per browser.
        trackImpressions(data.map((c) => c.id));
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [active]);

  return (
    <div className="space-y-8">
      {greetingName && (
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
          Hello, {greetingName}!
        </h2>
      )}
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
            <CourseCard
              key={c.id}
              course={c}
              purchased={purchasedIds.has(c.id)}
              onPurchased={markPurchased}
            />
          ))}
        </div>
        {!loading && courses.length === 0 && !error && (
          <p className="text-slate-500 dark:text-slate-400">No courses in this category yet.</p>
        )}
      </section>
    </div>
  );
}
