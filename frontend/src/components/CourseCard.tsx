"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { StarRating, StarInput } from "@/components/Stars";
import type { CourseReviews, PublicCourse } from "@/lib/types";

function formatPrice(p: number | null) {
  if (p == null) return "—";
  return new Intl.NumberFormat("ro-RO", {
    style: "currency",
    currency: "RON",
    maximumFractionDigits: 0,
  }).format(p);
}

/**
 * A single course in the public catalog. The visitor is a logged-in contact, so
 * buying and reviewing use their session identity — no email/name to type.
 *
 * - {@code purchased === false}: a "Buy this course" button that registers the
 *   purchase for the logged-in contact.
 * - {@code purchased === true}: a disabled, greyed-out "Already bought" button,
 *   and the review form unlocks (a course can only be reviewed once bought).
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

  // Review form (no email — taken from the session)
  const [showReview, setShowReview] = useState(false);
  const [rvStars, setRvStars] = useState(0);
  const [rvComment, setRvComment] = useState("");
  const [rvLoading, setRvLoading] = useState(false);
  const [rvError, setRvError] = useState<string | null>(null);
  const [rvDone, setRvDone] = useState(false);

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
      setBuyError("Trebuie să fii autentificat pentru a cumpăra.");
      return;
    }
    setBuyLoading(true);
    setBuyError(null);
    try {
      // The logged-in contact's identity comes from the session — no form.
      await api.post(`/api/public/courses/${course.id}/purchase`, { email: session.email });
      onPurchased(course.id);
      // Surface the review form straight away.
      setShowReview(true);
    } catch (err) {
      setBuyError((err as Error).message);
    } finally {
      setBuyLoading(false);
    }
  }

  async function submitReview(e: React.FormEvent) {
    e.preventDefault();
    const session = getSession();
    if (!session) {
      setRvError("Trebuie să fii autentificat pentru a lăsa o recenzie.");
      return;
    }
    setRvLoading(true);
    setRvError(null);
    try {
      await api.post(`/api/public/courses/${course.id}/reviews`, {
        email: session.email,
        rating: rvStars,
        comment: rvComment.trim() || null,
      });
      setRvDone(true);
      setShowReview(false);
      setRvComment("");
      setRvStars(0);
      await loadReviews();
    } catch (err) {
      setRvError((err as Error).message);
    } finally {
      setRvLoading(false);
    }
  }

  const inputCls =
    "w-full rounded-md border border-slate-300 px-3 py-1.5 text-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100 dark:placeholder:text-slate-500";

  return (
    <article className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-colors dark:border-slate-800 dark:bg-slate-900">
      <div className="mb-2 flex items-center justify-between">
        <span className="rounded bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300">
          {course.categoryLabel ?? course.category}
        </span>
        <span className="text-xs text-slate-400 dark:text-slate-500">{course.level}</span>
      </div>
      <h3 className="font-semibold">{course.name}</h3>

      {/* Average rating */}
      <div className="mt-2 flex items-center gap-2">
        <StarRating value={average} size={16} />
        {count > 0 ? (
          <span className="text-xs text-slate-500 dark:text-slate-400">
            {average.toFixed(1)} · {count} {count === 1 ? "review" : "reviews"}
          </span>
        ) : (
          <span className="text-xs text-slate-400 dark:text-slate-500">No reviews yet</span>
        )}
      </div>

      <div className="mt-4 flex items-center justify-between text-sm">
        <span className="text-slate-500 dark:text-slate-400">{course.durationHours ?? "—"} h</span>
        <span className="font-semibold text-slate-900 dark:text-slate-100">{formatPrice(course.priceIndividual)}</span>
      </div>

      <button
        type="button"
        onClick={toggleExpanded}
        aria-expanded={expanded}
        className="mt-4 flex items-center justify-center gap-1 rounded-md border border-slate-200 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
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
          <div className="mt-3 space-y-4 border-t border-slate-100 pt-3 dark:border-slate-800">
            <p className="text-sm text-slate-600 dark:text-slate-300">
              {course.description ?? "No description available for this course."}
            </p>

            {/* ---- Buy ---- */}
            {purchased ? (
              <button
                type="button"
                disabled
                aria-disabled="true"
                className="w-full cursor-not-allowed rounded-lg bg-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-500 dark:bg-slate-700 dark:text-slate-400"
              >
                Already bought
              </button>
            ) : (
              <button
                type="button"
                onClick={buy}
                disabled={buyLoading}
                className="w-full rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:from-indigo-700 hover:to-violet-700 hover:shadow active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-60"
              >
                {buyLoading ? "Processing…" : "Buy this course"}
              </button>
            )}
            {buyError && <p className="text-sm text-red-600 dark:text-red-400">{buyError}</p>}

            {/* ---- Reviews ---- */}
            <div className="border-t border-slate-100 pt-3 dark:border-slate-800">
              <div className="mb-2 flex items-center justify-between">
                <p className="text-sm font-semibold">Reviews</p>
                {purchased && !showReview && !rvDone && (
                  <button
                    type="button"
                    onClick={() => setShowReview(true)}
                    className="text-sm font-medium text-indigo-600 hover:text-indigo-700 dark:text-indigo-400 dark:hover:text-indigo-300"
                  >
                    Write a review
                  </button>
                )}
              </div>

              {!purchased && (
                <p className="mb-2 text-xs text-slate-400 dark:text-slate-500">
                  Buy this course to leave a review.
                </p>
              )}

              {rvDone && (
                <p className="mb-2 rounded-lg bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300">
                  Thanks for your review!
                </p>
              )}

              {showReview && (
                <form onSubmit={submitReview} className="mb-3 space-y-2 rounded-lg bg-slate-50 p-3 dark:bg-slate-800/50">
                  <StarInput value={rvStars} onChange={setRvStars} />
                  <textarea
                    className={inputCls}
                    rows={3}
                    placeholder="Share your experience (optional)"
                    value={rvComment}
                    onChange={(e) => setRvComment(e.target.value)}
                  />
                  {rvError && <p className="text-sm text-red-600 dark:text-red-400">{rvError}</p>}
                  <div className="flex gap-2">
                    <button
                      type="submit"
                      disabled={rvLoading || rvStars < 1}
                      className="rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition-all hover:from-indigo-700 hover:to-violet-700 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {rvLoading ? "Submitting…" : "Submit review"}
                    </button>
                    <button type="button" onClick={() => setShowReview(false)} className="rounded-lg px-3 py-2 text-sm text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200">
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              {reviewsError && <p className="text-sm text-red-600 dark:text-red-400">Could not load reviews: {reviewsError}</p>}

              {reviews && reviews.length > 0 ? (
                <ul className="space-y-2">
                  {reviews.map((r, i) => (
                    <li key={i} className="rounded-lg border border-slate-200 bg-white p-3 dark:border-slate-800 dark:bg-slate-900">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">{r.author}</span>
                        <StarRating value={r.rating} size={14} />
                      </div>
                      {r.comment && <p className="mt-1 text-sm text-slate-600 dark:text-slate-300">{r.comment}</p>}
                      {r.date && <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">{r.date}</p>}
                    </li>
                  ))}
                </ul>
              ) : (
                reviews && !reviewsError && (
                  <p className="text-sm text-slate-500 dark:text-slate-400">Be the first to review this course.</p>
                )
              )}
            </div>
          </div>
        </div>
      </div>
    </article>
  );
}
