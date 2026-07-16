"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import { AUTH_EVENT, getSession } from "@/lib/auth";
import { FlagIcon, CloseIcon } from "@/components/icons";
import type { AuthSession } from "@/lib/types";

/**
 * Client-portal "Report an issue" widget. A floating flag button opens a window
 * that slowly grows to half the screen before its text field waits for the
 * client to type their problem. Dismissing it (the "×", clicking the backdrop,
 * or Escape) slowly shrinks it back to nothing before it disappears. Pressing
 * "Send" posts it to /api/issues (where it shows up in the admin portal's Issues
 * view), shrinks the window away, then swaps in a confirmation window that
 * slowly grows to half the screen reading "Thank you for submitting your
 * issue!", holds for a beat, then slowly shrinks back to nothing and vanishes.
 *
 * Only signed-in clients (USER role) see the button — admins and signed-out
 * visitors never do.
 *
 * Phases: "open" grows in and accepts input; "dismissing" shrinks away to idle
 * (cancelled without sending); "closing" shrinks away then hands off to
 * "thanks" (sent successfully).
 */
type Phase = "idle" | "open" | "dismissing" | "closing" | "thanks";

// Animation timings (ms). These MUST match the `duration-700` transitions on the
// windows below. The phase sequence is advanced by timers rather than by
// `transitionend` events, because a `transitionend` never fires when the browser
// has transitions disabled (e.g. `prefers-reduced-motion: reduce`), which would
// otherwise leave the window stuck mid-sequence.
const GROW_MS = 700; // a window growing to half-screen (report on open, thanks)
const SHRINK_MS = 700; // a window shrinking away (report on dismiss/send, thanks)
const HOLD_MS = 1600; // thanks window held fully open before it shrinks

export default function ReportIssue() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [phase, setPhase] = useState<Phase>("idle");
  const [reportGrown, setReportGrown] = useState(false);
  const [thanksGrown, setThanksGrown] = useState(false);
  const [text, setText] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const textRef = useRef<HTMLTextAreaElement>(null);

  // Track the login session so the button only appears for clients.
  useEffect(() => {
    const sync = () => setSession(getSession());
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  // Grow the report window in from nothing, then focus the field once it's open.
  useEffect(() => {
    if (phase !== "open") return;
    setReportGrown(false);
    // Paint at scale(0) first, then flip to scale(1) so the growth animates.
    const raf = requestAnimationFrame(() => setReportGrown(true));
    // Focus so the field "waits for characters" as soon as the window appears.
    textRef.current?.focus();
    return () => cancelAnimationFrame(raf);
  }, [phase]);

  // Allow Escape to dismiss the window while it's open (but not mid-animation).
  useEffect(() => {
    if (phase !== "open") return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setPhase("dismissing");
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [phase]);

  // Dismissed without sending: shrink the report window away, then reset to idle.
  useEffect(() => {
    if (phase !== "dismissing") return;
    setReportGrown(false);
    const t = setTimeout(() => setPhase("idle"), SHRINK_MS);
    return () => clearTimeout(t);
  }, [phase]);

  // Sent: shrink the report window away, then hand off to the "thanks" window.
  useEffect(() => {
    if (phase !== "closing") return;
    setReportGrown(false);
    const t = setTimeout(() => {
      setSending(false);
      setText("");
      setPhase("thanks");
    }, SHRINK_MS);
    return () => clearTimeout(t);
  }, [phase]);

  // Drive the confirmation window: grow from nothing → hold → shrink → vanish.
  useEffect(() => {
    if (phase !== "thanks") return;
    setThanksGrown(false);
    // Paint at scale(0) first, then flip to scale(1) so the growth animates.
    const raf = requestAnimationFrame(() => setThanksGrown(true));
    // After it has grown and held for a beat, shrink it back down…
    const shrink = setTimeout(() => setThanksGrown(false), GROW_MS + HOLD_MS);
    // …then, once it has finished shrinking, fully reset back to idle.
    const done = setTimeout(() => setPhase("idle"), GROW_MS + HOLD_MS + SHRINK_MS);
    return () => {
      cancelAnimationFrame(raf);
      clearTimeout(shrink);
      clearTimeout(done);
    };
  }, [phase]);

  if (session?.role !== "USER") return null;

  function open() {
    setError(null);
    setText("");
    setSending(false);
    // Start collapsed so the "open" effect can grow the window in from nothing.
    setReportGrown(false);
    setPhase("open");
  }

  async function send(e: React.FormEvent) {
    e.preventDefault();
    const message = text.trim();
    if (!message || sending) return;
    setSending(true);
    setError(null);
    try {
      const name = [session!.firstName, session!.lastName].filter(Boolean).join(" ").trim();
      await api.post("/api/issues", {
        email: session!.email,
        name: name || null,
        message,
      });
      // Sent — first shrink the report window down to nothing; a timer then
      // hands off to the "thanks" confirmation window.
      setPhase("closing");
    } catch {
      setError("Could not send your report. Please try again.");
      setSending(false);
    }
  }

  // The report window stays mounted across its whole life (grow → input →
  // shrink); `reportGrown` alone drives whether it's at full or zero scale.
  const reportMounted = phase === "open" || phase === "dismissing" || phase === "closing";

  return (
    <>
      <button
        onClick={open}
        className="fixed bottom-5 left-5 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-brand-500 text-white shadow-lg shadow-brand-500/30 transition hover:bg-brand-600"
        aria-label="Report an issue"
      >
        <FlagIcon />
      </button>

      {reportMounted && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
          onClick={() => {
            if (phase === "open") setPhase("dismissing");
          }}
        >
          <div
            role="dialog"
            aria-modal="true"
            aria-label="Report an issue"
            onClick={(e) => e.stopPropagation()}
            style={{
              transform: reportGrown ? "scale(1)" : "scale(0)",
              opacity: reportGrown ? 1 : 0,
            }}
            className="flex h-1/2 min-h-[320px] w-1/2 min-w-[320px] max-w-2xl origin-center flex-col overflow-hidden rounded-2xl border border-black/5 bg-white shadow-2xl transition-all duration-700 ease-in-out dark:border-white/10 dark:bg-zinc-900"
          >
            <div className="flex items-center justify-between bg-brand-500 px-5 py-3 text-white">
              <div className="flex items-center gap-2">
                <FlagIcon className="h-5 w-5" />
                <span className="text-sm font-semibold">Report an issue</span>
              </div>
              <button
                onClick={() => setPhase("dismissing")}
                aria-label="Close"
                className="rounded-full p-1 transition hover:bg-white/20"
              >
                <CloseIcon className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={send} className="flex flex-1 flex-col gap-3 p-5">
              <label htmlFor="issue-message" className="text-sm text-zinc-600 dark:text-zinc-300">
                Tell us what went wrong and we&rsquo;ll look into it.
              </label>
              <textarea
                id="issue-message"
                ref={textRef}
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Describe the issue…"
                className="flex-1 resize-none rounded-xl border border-zinc-300 p-3 text-sm outline-none focus:border-brand-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500"
              />
              {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}
              <div className="flex justify-end">
                <button
                  type="submit"
                  disabled={!text.trim() || sending}
                  className="rounded-full bg-brand-500 px-6 py-2 text-sm font-medium text-white transition hover:bg-brand-600 disabled:opacity-50"
                >
                  {sending ? "Sending…" : "Send"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {phase === "thanks" && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm">
          <div
            role="dialog"
            aria-modal="true"
            aria-label="Issue submitted"
            style={{
              transform: thanksGrown ? "scale(1)" : "scale(0)",
              opacity: thanksGrown ? 1 : 0,
            }}
            className="flex h-1/2 min-h-[320px] w-1/2 min-w-[320px] max-w-2xl origin-center flex-col items-center justify-center gap-4 overflow-hidden rounded-2xl border border-black/5 bg-white p-8 text-center shadow-2xl transition-all duration-700 ease-in-out dark:border-white/10 dark:bg-zinc-900"
          >
            <span className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-500/10 text-brand-500">
              <FlagIcon className="h-8 w-8" />
            </span>
            <p className="text-xl font-semibold text-zinc-900 dark:text-zinc-100">
              Thank you for submitting your issue!
            </p>
          </div>
        </div>
      )}
    </>
  );
}
