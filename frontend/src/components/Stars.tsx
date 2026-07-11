"use client";

import { useState } from "react";

// Star rating UI. Stars are real SVG vector graphics (never emoji), matching
// the rest of the app's SVG icon set.

function StarShape({
  className = "",
  style,
}: {
  className?: string;
  style?: React.CSSProperties;
}) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className} style={style} aria-hidden="true">
      <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
    </svg>
  );
}

/**
 * Read-only star rating. Supports fractional values (e.g. 4.3) by clipping a
 * gold star layer over a muted background layer.
 */
export function StarRating({
  value,
  size = 16,
  className = "",
}: {
  value: number;
  size?: number;
  className?: string;
}) {
  const clamped = Math.max(0, Math.min(5, value || 0));
  const pct = (clamped / 5) * 100;
  const px = `${size}px`;
  const dims: React.CSSProperties = { width: px, height: px, flex: "0 0 auto" };

  return (
    <span
      className={`relative inline-flex ${className}`}
      style={{ width: `${size * 5}px`, height: px }}
      role="img"
      aria-label={`${clamped.toFixed(1)} out of 5 stars`}
    >
      <span className="absolute inset-0 flex">
        {[0, 1, 2, 3, 4].map((i) => (
          <StarShape key={i} style={dims} className="text-zinc-300 dark:text-zinc-600" />
        ))}
      </span>
      <span className="absolute inset-0 flex overflow-hidden" style={{ width: `${pct}%` }}>
        {[0, 1, 2, 3, 4].map((i) => (
          <StarShape key={i} style={dims} className="text-amber-400" />
        ))}
      </span>
    </span>
  );
}

/**
 * Interactive 1-5 star picker for submitting a review.
 */
export function StarInput({
  value,
  onChange,
  size = 28,
}: {
  value: number;
  onChange: (n: number) => void;
  size?: number;
}) {
  const [hover, setHover] = useState(0);
  const active = hover || value;
  const px = `${size}px`;

  return (
    <div className="inline-flex gap-1" role="radiogroup" aria-label="Pick a rating from 1 to 5 stars">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          onMouseEnter={() => setHover(n)}
          onMouseLeave={() => setHover(0)}
          aria-label={`${n} ${n === 1 ? "star" : "stars"}`}
          aria-pressed={value === n}
          className="rounded transition-transform hover:scale-110 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
        >
          <StarShape
            style={{ width: px, height: px }}
            className={n <= active ? "text-amber-400" : "text-zinc-300 dark:text-zinc-600"}
          />
        </button>
      ))}
    </div>
  );
}
