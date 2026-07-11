"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { StarRating } from "@/components/Stars";
import { QuoteIcon } from "@/components/icons";
import type { PublicReview } from "@/lib/types";

/**
 * A continuously scrolling ticker of every review across every course, in
 * order (newest first), regardless of rating or whether the review has written
 * feedback. The track is rendered twice and animated by -50% so it loops
 * seamlessly; hovering pauses it. See `.ti-marquee-*` in globals.css.
 */
export default function ReviewsTicker() {
  const [reviews, setReviews] = useState<PublicReview[]>([]);

  useEffect(() => {
    api
      .get<PublicReview[]>("/api/public/reviews")
      .then(setReviews)
      .catch(() => setReviews([]));
  }, []);

  // Nothing to scroll — keep the landing page clean rather than show an empty bar.
  if (reviews.length === 0) return null;

  // Constant visual speed: ~5s of travel per review, halved because the track
  // is duplicated (the -50% loop covers one full copy).
  const duration = Math.max(20, reviews.length * 5);
  const loop = [...reviews, ...reviews];

  return (
    <section
      aria-label="What learners say"
      className="full-bleed border-y border-black/5 bg-white py-5 dark:border-white/10 dark:bg-zinc-900/40"
    >
      <div
        className="ti-marquee-wrap relative overflow-hidden"
        // Fade the edges so chips slide in/out instead of popping at the border.
        style={{
          maskImage:
            "linear-gradient(to right, transparent, black 6%, black 94%, transparent)",
          WebkitMaskImage:
            "linear-gradient(to right, transparent, black 6%, black 94%, transparent)",
        }}
      >
        <ul className="ti-marquee-track" style={{ animationDuration: `${duration}s` }}>
          {loop.map((r, i) => (
            <li
              key={`${r.courseId}-${r.author}-${i}`}
              aria-hidden={i >= reviews.length}
              className="mx-3 flex max-w-md shrink-0 items-start gap-3 rounded-2xl border border-black/5 bg-canvas px-5 py-3.5 dark:border-white/10 dark:bg-zinc-900"
            >
              <QuoteIcon className="mt-0.5 h-4 w-4 shrink-0 text-brand-500" />
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <StarRating value={r.rating} size={13} />
                  <span className="truncate text-xs font-medium text-brand-600 dark:text-brand-400">
                    {r.courseName}
                  </span>
                </div>
                <p className="mt-1 text-sm leading-snug text-zinc-700 dark:text-zinc-300">
                  &ldquo;{r.comment}&rdquo;
                </p>
                <p className="mt-1 text-xs text-zinc-500 dark:text-zinc-500">— {r.author}</p>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
