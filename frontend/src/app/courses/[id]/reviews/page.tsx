"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { api } from "@/lib/api";
import { StarRating } from "@/components/Stars";
import { getReviewsTarget, type ReviewsTarget } from "@/lib/reviewsAccess";
import type { CourseReviews } from "@/lib/types";

/**
 * Dedicated, review-only page for a single course. This route is intentionally
 * NOT linkable: it is reachable only by pressing "See Reviews" on the course's
 * card (which grants one-time access via {@link getReviewsTarget}), and the
 * grant is revoked the moment the visitor leaves — so typing or refreshing the
 * URL, or coming back later, bounces straight back to the catalog.
 *
 * Only reviews with at least one star are shown; anything rated 0 (an enrolment
 * that was never really reviewed) is ignored entirely.
 */
export default function CourseReviewsPage() {
  const router = useRouter();
  const params = useParams<{ id: string }>();
  const courseId = Number(params.id);

  // "checking" until the access grant is read; then either "granted" (render the
  // reviews) or "denied" (redirect to the catalog).
  const [access, setAccess] = useState<"checking" | "granted" | "denied">("checking");
  const [target, setTarget] = useState<ReviewsTarget | null>(null);
  const [data, setData] = useState<CourseReviews | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Access check. Read-only (the grant is cleared on leave by PrimaryNav), so a
  // strict-mode double-mount simply re-reads the same flag without breaking.
  useEffect(() => {
    const t = getReviewsTarget();
    if (t && t.id === courseId) {
      setTarget(t);
      setAccess("granted");
    } else {
      setAccess("denied");
      router.replace("/");
    }
  }, [courseId, router]);

  useEffect(() => {
    if (access !== "granted") return;
    api
      .get<CourseReviews>(`/api/public/courses/${courseId}/reviews`)
      .then(setData)
      .catch((e) => setError((e as Error).message));
  }, [access, courseId]);

  if (access !== "granted") {
    return (
      <p className="py-10 text-center text-sm text-zinc-500 dark:text-zinc-400">
        Loading…
      </p>
    );
  }

  // A review only counts if it carries at least one star; drop everything else.
  const reviews = (data?.reviews ?? []).filter((r) => r.rating >= 1);
  // Average over the shown reviews only, so it stays consistent with the count
  // (the backend average also folds in 0-star, ignored entries).
  const average =
    reviews.length === 0
      ? 0
      : Math.round((reviews.reduce((s, r) => s + r.rating, 0) / reviews.length) * 10) / 10;

  return (
    <div className="space-y-6">
      <div>
        <Link
          href="/"
          className="text-sm font-medium text-brand-600 transition hover:text-brand-700 dark:text-brand-400 dark:hover:text-brand-300"
        >
          ← Back to courses
        </Link>
        <h1 className="mt-3 text-2xl font-bold text-zinc-900 dark:text-white">
          Reviews — {target?.name ?? "Course"}
        </h1>
        {data && (
          <div className="mt-2 flex items-center gap-2">
            {reviews.length > 0 ? (
              <>
                <StarRating value={average} size={18} />
                <span className="text-sm text-zinc-500 dark:text-zinc-400">
                  {average.toFixed(1)} · {reviews.length}{" "}
                  {reviews.length === 1 ? "review" : "reviews"}
                </span>
              </>
            ) : (
              <span className="text-sm text-zinc-400 dark:text-zinc-500">
                No reviews yet
              </span>
            )}
          </div>
        )}
      </div>

      {error && (
        <p className="text-red-600 dark:text-red-400">Could not load reviews: {error}</p>
      )}

      {!data && !error && (
        <p className="text-zinc-500 dark:text-zinc-400">Loading reviews…</p>
      )}

      {data && reviews.length > 0 && (
        <ul className="space-y-3">
          {reviews.map((r, i) => (
            <li
              key={i}
              className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-center justify-between gap-3">
                <span className="font-medium text-zinc-900 dark:text-white">{r.author}</span>
                <StarRating value={r.rating} size={15} />
              </div>
              {r.comment && (
                <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">{r.comment}</p>
              )}
              {r.date && (
                <p className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">{r.date}</p>
              )}
            </li>
          ))}
        </ul>
      )}

      {data && reviews.length === 0 && !error && (
        <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-8 text-center dark:border-zinc-700 dark:bg-zinc-900">
          <p className="text-zinc-600 dark:text-zinc-300">
            This course has no reviews yet.
          </p>
        </div>
      )}
    </div>
  );
}
