"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { StarInput } from "@/components/Stars";

/**
 * Per-course "Write a review" control for the My Courses page. A contact can
 * only reach this for a course they've enrolled in, so the review is always
 * allowed by the backend (which stores it on the enrollment). Re-submitting
 * updates the existing review, so the button reads "Edit review" once a rating
 * exists.
 *
 * The parent owns the course's rating; {@code onSaved} lets it refresh the
 * displayed stars after a successful submit.
 */
export default function CourseReviewButton({
  courseId,
  currentRating,
  onSaved,
}: {
  courseId: number;
  currentRating: number | null;
  onSaved: (rating: number) => void;
}) {
  // A valid review is 1–5 stars; the backend reports 0 for a course that was
  // enrolled but never reviewed, so treat only >= 1 as an existing review.
  const hasReview = currentRating != null && currentRating >= 1;

  const [open, setOpen] = useState(false);
  const [stars, setStars] = useState(hasReview ? currentRating : 0);
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inputCls =
    "w-full rounded-lg border border-zinc-300 px-3 py-1.5 text-sm outline-none transition focus:border-brand-500 focus:ring-1 focus:ring-brand-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500";

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const session = getSession();
    if (!session) {
      setError("You must be signed in to leave a review.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await api.post(`/api/public/courses/${courseId}/reviews`, {
        email: session.email,
        rating: stars,
        comment: comment.trim() || null,
      });
      onSaved(stars);
      setComment("");
      setOpen(false);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  }

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => {
          setStars(hasReview ? currentRating : 0);
          setOpen(true);
        }}
        className="rounded-lg border border-brand-200 px-4 py-2 text-sm font-semibold text-brand-700 transition hover:border-brand-300 hover:bg-brand-50 dark:border-brand-500/30 dark:text-brand-300 dark:hover:bg-brand-500/10"
      >
        {hasReview ? "Edit review" : "Write a review"}
      </button>
    );
  }

  return (
    <form
      onSubmit={submit}
      className="w-full space-y-2 rounded-xl bg-canvas p-3 text-left dark:bg-zinc-800/50"
    >
      <StarInput value={stars} onChange={setStars} />
      <textarea
        className={inputCls}
        rows={3}
        placeholder="Share your experience (optional)"
        value={comment}
        onChange={(e) => setComment(e.target.value)}
      />
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={loading || stars < 1}
          className="rounded-full bg-brand-500 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {loading ? "Submitting…" : "Submit review"}
        </button>
        <button
          type="button"
          onClick={() => setOpen(false)}
          className="rounded-full px-3 py-2 text-sm text-zinc-500 hover:text-zinc-700 dark:text-zinc-400 dark:hover:text-zinc-200"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
