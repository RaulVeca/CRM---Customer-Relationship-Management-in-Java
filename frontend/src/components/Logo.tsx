// IT-Labs brand mark. Renders the official vector-derived logo asset (a blue
// laptop/circuit icon next to the orange "IT-Labs" wordmark with the
// "Training & Development" tagline). The full-colour artwork carries the brand
// palette itself, so it sits on both light and dark surfaces unchanged.

import Image from "next/image";

// Intrinsic pixel dimensions of the exported PNGs, used to keep next/image's
// aspect ratio correct while callers size the mark by height alone.
const FULL = { w: 1400, h: 489 }; // itlabs-logo.png  (icon + wordmark)
const ICON = { w: 440, h: 397 }; //  itlabs-icon.png  (laptop badge only)

type LogoProps = {
  /** Rendered height in pixels; width scales to preserve the artwork ratio. */
  size?: number;
  /**
   * Kept for backwards compatibility with earlier call sites. The artwork is
   * multi-colour and theme-independent, so this no longer affects rendering.
   */
  tone?: "auto" | "dark" | "light";
  /** Render only the laptop badge, without the wordmark. */
  iconOnly?: boolean;
  className?: string;
};

export default function Logo({ size = 32, iconOnly = false, className = "" }: LogoProps) {
  const art = iconOnly ? ICON : FULL;
  const width = Math.round((size * art.w) / art.h);
  return (
    <Image
      src={iconOnly ? "/itlabs-icon.png" : "/itlabs-logo.png"}
      alt="IT-Labs — Training & Development"
      width={width}
      height={size}
      priority
      className={`inline-block w-auto ${className}`}
      style={{ height: size, width }}
    />
  );
}
