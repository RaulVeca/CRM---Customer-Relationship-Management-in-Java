"use client";

import { useEffect, useState, useSyncExternalStore } from "react";
import type { ElementType } from "react";
import { useInView } from "@/lib/useInView";

/** A stretch of heading text, optionally styled (e.g. the pink accent). */
type Part = string | { text: string; className?: string };

type TypewriterProps = {
  parts: Part[];
  /** Heading level to render. */
  as?: ElementType;
  className?: string;
  /** Pause before the first character lands, in milliseconds. */
  delay?: number;
  /** Milliseconds per character. */
  speed?: number;
};

/** One character, carrying the styling of the part it came from. */
type Char = { char: string; className?: string };

function buildChars(parts: Part[]): Char[] {
  const chars: Char[] = [];
  for (const part of parts) {
    const text = typeof part === "string" ? part : part.text;
    const className = typeof part === "string" ? undefined : part.className;
    for (const char of text) chars.push({ char, className });
  }
  return chars;
}

const REDUCED_MOTION = "(prefers-reduced-motion: reduce)";

/** Live `prefers-reduced-motion`, read the way React wants an external store read. */
function usePrefersReducedMotion() {
  return useSyncExternalStore(
    (onChange) => {
      const query = window.matchMedia(REDUCED_MOTION);
      query.addEventListener("change", onChange);
      return () => query.removeEventListener("change", onChange);
    },
    () => window.matchMedia(REDUCED_MOTION).matches,
    () => false, // on the server, assume motion is fine
  );
}

/**
 * Types a heading out character by character, with a blinking caret, the first
 * time it scrolls into view.
 *
 * Every character is always in the DOM — untyped ones are merely hidden by CSS,
 * so they still take up space. That is what keeps the heading on its natural
 * number of lines: it wraps exactly as it would without the effect, and nothing
 * below it jumps as the text arrives. (Animating `width` from 0 instead, as the
 * classic CSS-only typewriter does, forces `white-space: nowrap` — which would
 * clip long headings on narrow screens and park the caret at the container's
 * edge rather than at the last letter.)
 *
 * The hiding lives under `html.js`, so with JavaScript off the heading is simply
 * plain text rather than permanently invisible.
 */
export default function Typewriter({
  parts,
  as: Tag = "h2",
  className = "",
  delay = 0,
  speed = 45,
}: TypewriterProps) {
  const { ref, inView } = useInView<HTMLHeadingElement>();
  const reduced = usePrefersReducedMotion();
  const chars = buildChars(parts);
  const label = chars.map((c) => c.char).join("");

  const [progress, setProgress] = useState(0);
  // Reduced motion means the heading is simply already written, wherever it is.
  const typed = reduced ? chars.length : progress;
  const done = typed >= chars.length;

  useEffect(() => {
    if (!inView || reduced) return;
    const timers = chars.map((_, i) =>
      setTimeout(() => setProgress(i + 1), delay + (i + 1) * speed),
    );
    return () => timers.forEach(clearTimeout);
    // `chars` is rebuilt each render; its length is what the schedule depends on.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inView, reduced, chars.length, delay, speed]);

  return (
    <Tag ref={ref} aria-label={label} className={`ti-typewriter ${className}`}>
      {chars.map((c, i) => (
        <span
          key={i}
          aria-hidden="true"
          className={`ti-tw-char ${i < typed ? "is-typed" : ""} ${
            i === typed - 1 && !done ? "ti-tw-caret" : ""
          } ${c.className ?? ""}`}
        >
          {c.char}
        </span>
      ))}
      {/* Once the last character lands the caret has no character to sit on, so
          it becomes a zero-width element parked at the end of the text. */}
      {done && <span aria-hidden="true" className="ti-tw-caret ti-tw-caret-end" />}
    </Tag>
  );
}
