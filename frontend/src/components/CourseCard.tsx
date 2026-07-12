"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { StarRating } from "@/components/Stars";
import { CheckIcon } from "@/components/icons";
import { buildCourseDetails } from "@/lib/courseDetails";
import { openReviews } from "@/lib/reviewsAccess";
import type { PublicCourse } from "@/lib/types";

/**
 * A single course in the public catalog. The visitor is a logged-in contact, so
 * buying uses their session identity — no email/name to type. Expanding the card
 * ("More") reveals a detailed description and a "See Reviews" button that opens
 * the course's dedicated, review-only page; reviews are no longer listed inline.
 * Writing a review lives on the My Courses page, scoped to enrolled courses.
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
  const router = useRouter();
  const [expanded, setExpanded] = useState(false);
  // Whether this card's click-through has already been counted this mount.
  const [clicked, setClicked] = useState(false);

  const average = course.averageRating;
  const count = course.reviewCount;

  // Buy
  const [buyLoading, setBuyLoading] = useState(false);
  const [buyError, setBuyError] = useState<string | null>(null);

  const details = buildCourseDetails(course);

  function toggleExpanded() {
    const next = !expanded;
    setExpanded(next);
    if (next && !clicked) {
      // First open = a click-through; track it once for the admin CTR metric.
      api.post(`/api/public/courses/${course.id}/click`, null).catch(() => {});
      setClicked(true);
    }
  }

  /**
   * Grant one-time access to this course's reviews page and go there. The grant
   * is revoked when the visitor leaves, so the page can't be reached any other way.
   */
  function seeReviews() {
    openReviews({ id: course.id, name: course.name });
    router.push(`/courses/${course.id}/reviews`);
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
            {/* ---- Detailed description ---- */}
            <div className="space-y-3 text-sm leading-relaxed text-zinc-600 dark:text-zinc-300">
              {details.overview.map((para, i) => (
                <p key={i}>{para}</p>
              ))}
              <div>
                <p className="mb-1.5 font-semibold text-ink dark:text-white">What you'll learn</p>
                <ul className="space-y-1.5">
                  {details.learn.map((item) => (
                    <li key={item} className="flex items-start gap-2">
                      <CheckIcon className="mt-0.5 h-4 w-4 shrink-0 text-brand-500" />
                      <span>{item}</span>
                    </li>
                  ))}
                </ul>
              </div>
              <div>
                <p className="mb-1 font-semibold text-ink dark:text-white">Who it's for</p>
                <p>{details.audience}</p>
              </div>
              <div>
                <p className="mb-1 font-semibold text-ink dark:text-white">Format &amp; commitment</p>
                <p>{details.format}</p>
              </div>
            </div>

            {/* ---- See Reviews (opens the dedicated, review-only page) ---- */}
            <button
              type="button"
              onClick={seeReviews}
              className="w-full rounded-full border border-brand-200 px-4 py-2.5 text-sm font-semibold text-brand-700 transition hover:border-brand-300 hover:bg-brand-50 dark:border-brand-500/30 dark:text-brand-300 dark:hover:bg-brand-500/10"
            >
              See Reviews
            </button>

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
          </div>
        </div>
      </div>
    </article>
  );
}
