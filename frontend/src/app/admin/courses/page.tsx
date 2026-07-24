"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Course } from "@/lib/types";

export default function AdminCoursesPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<Course[]>("/api/courses")
      .then(setCourses)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold tracking-tight">Course catalog</h2>
      {error && <p className="text-red-600">{error}</p>}
      {loading && <p className="text-zinc-500">Loading…</p>}

      <div className="overflow-hidden rounded-xl border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900 shadow-sm">
        <table className="w-full text-sm">
          <thead className="bg-[#2c3763] text-left text-xs font-semibold uppercase tracking-wide text-slate-100">
            <tr>
              <th className="px-4 py-3">Code</th>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Category</th>
              <th className="px-4 py-3">Level</th>
              <th className="px-4 py-3 text-right">Hours</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
            {courses.map((c) => (
              <tr key={c.id} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                <td className="px-4 py-3 font-mono text-xs">{c.code}</td>
                <td className="px-4 py-3 font-medium">{c.name}</td>
                <td className="px-4 py-3 text-zinc-500">{c.category}</td>
                <td className="px-4 py-3 text-zinc-500">{c.level}</td>
                <td className="px-4 py-3 text-right">{c.durationHours ?? "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
