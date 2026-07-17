"use client";

import { useEffect, useRef, useState } from "react";

/**
 * Tracks whether an element has scrolled into view, latching to `true` the first
 * time it does — the reveal animations across the site are one-shot, since
 * re-running them on every pass makes a long page feel restless.
 *
 * The trigger line sits 10% above the viewport's bottom edge, so an element
 * starts moving just after it appears rather than the instant its first pixel
 * crosses the fold.
 */
export function useInView<T extends HTMLElement = HTMLElement>() {
  const ref = useRef<T | null>(null);
  const [inView, setInView] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el || inView) return;

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            setInView(true);
            observer.disconnect();
          }
        }
      },
      { rootMargin: "0px 0px -10% 0px", threshold: 0 },
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [inView]);

  return { ref, inView };
}
