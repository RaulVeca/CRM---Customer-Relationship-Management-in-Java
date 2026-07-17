"use client";

import type { CSSProperties, ReactNode } from "react";
import { useInView } from "@/lib/useInView";

type RevealProps = {
  children: ReactNode;
  /** The direction the element settles in from. */
  animation?: "up" | "left" | "right" | "grow";
  /** Stagger, in milliseconds — give siblings 100/200/300… to cascade them in. */
  delay?: number;
  className?: string;
};

/**
 * Reveals its children the first time they scroll into view: the element starts
 * offset and transparent (`.ti-reveal*` in globals.css) and settles into place
 * once `is-in` lands on it.
 */
export default function Reveal({
  children,
  animation = "up",
  delay = 0,
  className = "",
}: RevealProps) {
  const { ref, inView } = useInView<HTMLDivElement>();

  return (
    <div
      ref={ref}
      className={`ti-reveal ti-reveal-${animation} ${inView ? "is-in" : ""} ${className}`}
      style={delay ? ({ "--ti-delay": `${delay}ms` } as CSSProperties) : undefined}
    >
      {children}
    </div>
  );
}
