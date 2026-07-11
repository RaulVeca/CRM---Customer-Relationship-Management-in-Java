"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { StarRating } from "@/components/Stars";
import type { CourseReviews, PublicCourse } from "@/lib/types";

/**
 * A single course in the public catalog. The visitor is a logged-in contact, so
 * buying uses their session identity — no email/name to type. Reviews are shown
 * read-only here; writing a review lives on the My Courses page, scoped to the
 * courses the contact has actually enrolled in.
 *
 * - {@code purchased === false}: a "Buy this course" button that registers the
 *   purchase for the logged-in contact.
 * - {@code purchased === true}: a disabled, greyed-out "Already bought" button.
 */
export default function CourseCard({
  course,
  purchased,
  onPurchased,
}: {
  course: PublicCourse;
  purchased: boolean;
  onPurchased: (courseId: number) => void;
}) {
  const [expanded, setExpanded] = useState(false);

  const [average, setAverage] = useState(course.averageRating);
  const [count, setCount] = useState(course.reviewCount);
  const [reviews, setReviews] = useState<CourseReviews["reviews"] | null>(null);
  const [reviewsError, setReviewsError] = useState<string | null>(null);

  // Buy
  const [buyLoading, setBuyLoading] = useState(false);
  const [buyError, setBuyError] = useState<string | null>(null);

  function toggleExpanded() {
    const next = !expanded;
    setExpanded(next);
    if (next && reviews === null) {
      // First open = a click-through; track it for the admin CTR metric.
      api.post(`/api/public/courses/${course.id}/click`, null).catch(() => {});
      void loadReviews();
    }
  }

  async function loadReviews() {
    try {
      const data = await api.get<CourseReviews>(`/api/public/courses/${course.id}/reviews`);
      setReviews(data.reviews);
      setAverage(data.average);
      setCount(data.count);
      setReviewsError(null);
    } catch (err) {
      setReviewsError((err as Error).message);
    }
  }

  async function buy() {
    const session = getSession();
    if (!session) {
      setBuyError("You must be signed in to buy.");
      return;
    }
    setBuyLoading(true);
    setBuyError(null);
    try {
      // The logged-in user's identity comes from the session — no form. The name
      // is sent too so that, when the buyer isn't a contact yet (e.g. an
      // employee), the contact auto-created at purchase has a proper name.
      await api.post(`/api/public/courses/${course.id}/purchase`, {
        email: session.email,
        firstName: session.firstName,
        lastName: session.lastName,
      });
      onPurchased(course.id);
    } catch (err) {
      setBuyError((err as Error).message);
    } finally {
      setBuyLoading(false);
    }
  }

  return (
    <article className="flex flex-col rounded-2xl border border-black/5 bg-white p-5 shadow-sm transition hover:shadow-lg hover:shadow-brand-500/5 dark:border-white/10 dark:bg-zinc-900">
      <div className="mb-2 flex items-center justify-between">
        <span className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
          {course.categoryLabel ?? course.category}
        </span>
        <span className="text-xs text-zinc-400 dark:text-zinc-500">{course.level}</span>
      </div>
      <h3 className="font-semibold text-ink dark:text-white">{course.name}</h3>

      {/* Average rating */}
      <div className="mt-2 flex items-center gap-2">
        <StarRating value={average} size={16} />
        {count > 0 ? (
          <span className="text-xs text-zinc-500 dark:text-zinc-400">
            {average.toFixed(1)} · {count} {count === 1 ? "review" : "reviews"}
          </span>
        ) : (
          <span className="text-xs text-zinc-400 dark:text-zinc-500">No reviews yet</span>
        )}
      </div>

      <div className="mt-4 flex items-center text-sm">
        <span className="text-zinc-500 dark:text-zinc-400">{course.durationHours ?? "—"} h</span>
      </div>

      <button
        type="button"
        onClick={toggleExpanded}
        aria-expanded={expanded}
        className="mt-4 flex items-center justify-center gap-1 rounded-full border border-black/10 py-1.5 text-sm font-medium text-zinc-600 transition hover:border-black/20 hover:bg-canvas dark:border-white/10 dark:text-zinc-300 dark:hover:bg-zinc-800"
      >
        More
        <svg viewBox="0 0 20 20" fill="currentColor" className={`h-4 w-4 transition-transform ${expanded ? "rotate-180" : ""}`}>
          <path
            fillRule="evenodd"
            d="M5.23 7.21a.75.75 0 011.06.02L10 11.06l3.71-3.83a.75.75 0 111.08 1.04l-4.25 4.39a.75.75 0 01-1.08 0L5.21 8.27a.75.75 0 01.02-1.06z"
            clipRule="evenodd"
          />
        </svg>
      </button>

      <div className={`grid transition-[grid-template-rows] duration-300 ease-in-out ${expanded ? "grid-rows-[1fr]" : "grid-rows-[0fr]"}`}>
        <div className="overflow-hidden">
          <div className="mt-3 space-y-4 border-t border-black/5 pt-3 dark:border-white/10">
            <p className="text-sm text-zinc-600 dark:text-zinc-300">
              {course.description ?? "No description available for this course."}
            </p>

            {/* ---- Buy ---- */}
            {purchased ? (
              <button
                type="button"
                disabled
                aria-disabled="true"
                className="w-full cursor-not-allowed rounded-full bg-zinc-200 px-4 py-2.5 text-sm font-semibold text-zinc-500 dark:bg-zinc-700 dark:text-zinc-400"
              >
                Already bought
              </button>
            ) : (
              <button
                type="button"
                onClick={buy}
                disabled={buyLoading}
                className="w-full rounded-full bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-600 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-60"
              >
                {buyLoading ? "Processing…" : "Buy this course"}
              </button>
            )}
            {buyError && <p className="text-sm text-red-600 dark:text-red-400">{buyError}</p>}

            {/* ---- Reviews (read-only; writing lives on My Courses) ---- */}
            <div className="border-t border-black/5 pt-3 dark:border-white/10">
              <p className="mb-2 text-sm font-semibold text-ink dark:text-white">Reviews</p>

              {reviewsError && <p className="text-sm text-red-600 dark:text-red-400">Could not load reviews: {reviewsError}</p>}

              {reviews && reviews.length > 0 ? (
                <ul className="space-y-2">
                  {reviews.map((r, i) => (
                    <li key={i} className="rounded-xl border border-black/5 bg-white p-3 dark:border-white/10 dark:bg-zinc-900">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium text-ink dark:text-white">{r.author}</span>
                        <StarRating value={r.rating} size={14} />
                      </div>
                      {r.comment && <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-300">{r.comment}</p>}
                      {r.date && <p className="mt-1 text-xs text-zinc-400 dark:text-zinc-500">{r.date}</p>}
                    </li>
                  ))}
                </ul>
              ) : (
                reviews && !reviewsError && (
                  <p className="text-sm text-zinc-500 dark:text-zinc-400">Be the first to review this course.</p>
                )
              )}
            </div>
          </div>
        </div>
      </div>
    </article>
  );
}
