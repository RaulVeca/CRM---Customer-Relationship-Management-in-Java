// TrainingIT wordmark — a magenta badge with a black cog, next to the
// "trainingit." lockup (the trailing dot carries the brand pink). Rendered as
// inline SVG + text so it stays crisp at any size and adapts to light/dark
// backgrounds via the `tone` prop.

type LogoProps = {
  /** Badge + text height in pixels. */
  size?: number;
  /**
   * Wordmark colour. "auto" (the default) inherits the parent's text colour, so
   * a single Logo follows the theme via `text-ink dark:text-white`. "dark" and
   * "light" pin it to near-black / white for a fixed-tone background.
   */
  tone?: "auto" | "dark" | "light";
  /** Hide the wordmark and render only the cog badge. */
  iconOnly?: boolean;
  className?: string;
};

export default function Logo({
  size = 32,
  tone = "auto",
  iconOnly = false,
  className = "",
}: LogoProps) {
  const wordColor =
    tone === "light" ? "#ffffff" : tone === "dark" ? "#0a0a0a" : "currentColor";
  return (
    <span
      className={`inline-flex items-center ${className}`}
      style={{ gap: size * 0.32 }}
      aria-label="TrainingIT"
    >
      <svg
        width={size}
        height={size}
        viewBox="0 0 40 40"
        fill="none"
        aria-hidden="true"
        className="shrink-0"
      >
        <circle cx="20" cy="20" r="20" fill="var(--color-brand-500, #de3fd0)" />
        <g transform="translate(8 8)" fill="#0a0a0a">
          <path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54A.49.49 0 0 0 13.4 2h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L1.81 8.47a.49.49 0 0 0 .12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.49.49 0 0 0-.12-.61l-2.01-1.58ZM11.48 15.6a3.6 3.6 0 1 1 0-7.2 3.6 3.6 0 0 1 0 7.2Z" />
        </g>
      </svg>
      {!iconOnly && (
        <span
          style={{
            fontSize: size * 0.62,
            color: wordColor,
            fontWeight: 700,
            letterSpacing: "-0.035em",
            lineHeight: 1,
          }}
        >
          trainingit<span style={{ color: "var(--color-brand-500, #de3fd0)" }}>.</span>
        </span>
      )}
    </span>
  );
}
