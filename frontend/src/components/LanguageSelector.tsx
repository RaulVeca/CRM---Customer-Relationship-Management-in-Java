"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "@/components/TranslationProvider";
import { LANGUAGES, findLanguage } from "@/lib/languages";

function GlobeIcon({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="9" />
      <path d="M3 12h18M12 3c2.5 2.7 2.5 15.3 0 18M12 3c-2.5 2.7-2.5 15.3 0 18" />
    </svg>
  );
}

/**
 * Header control that lets the visitor translate every page into any language.
 * Known languages are one click away; any other language can be typed in.
 */
export default function LanguageSelector() {
  const { lang, busy, setLang } = useTranslation();
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const boxRef = useRef<HTMLDivElement>(null);

  const current = findLanguage(lang);
  const currentLabel = current ? current.code.toUpperCase() : lang.toUpperCase();

  useEffect(() => {
    if (!open) return;
    function onDown(e: MouseEvent) {
      if (boxRef.current && !boxRef.current.contains(e.target as Node)) setOpen(false);
    }
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDown);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const q = query.trim().toLowerCase();
  const filtered = useMemo(() => {
    if (!q) return LANGUAGES;
    return LANGUAGES.filter(
      (l) => l.native.toLowerCase().includes(q) || l.name.toLowerCase().includes(q),
    );
  }, [q]);

  // Allow translating into a language that isn't in the shortlist.
  const custom =
    q && filtered.length === 0
      ? { code: query.trim().toLowerCase().replace(/\s+/g, "-"), native: query.trim(), name: query.trim() }
      : null;

  function choose(code: string, name: string) {
    setOpen(false);
    setQuery("");
    setLang(code, name); // reloads the page and translates
  }

  return (
    <div className="relative" ref={boxRef} data-no-translate>
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-label="Change language"
        title="Translate this site"
        aria-expanded={open}
        className="inline-flex h-9 items-center gap-1.5 rounded-full border border-black/10 bg-white px-3 text-zinc-600 transition hover:text-brand-600 dark:border-white/15 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:text-brand-400"
      >
        {busy ? (
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
        ) : (
          <GlobeIcon className="h-5 w-5" />
        )}
        <span className="text-xs font-semibold uppercase">{currentLabel}</span>
      </button>

      {open && (
        <div className="absolute right-0 top-11 z-50 w-64 overflow-hidden rounded-2xl border border-black/10 bg-white shadow-xl dark:border-white/10 dark:bg-zinc-900">
          <div className="border-b border-black/5 p-2 dark:border-white/10">
            <input
              autoFocus
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search or type any language…"
              className="w-full rounded-lg border border-zinc-200 bg-canvas px-3 py-2 text-sm outline-none focus:border-brand-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500"
            />
          </div>
          <ul className="max-h-72 overflow-y-auto py-1">
            {filtered.map((l) => (
              <li key={l.code}>
                <button
                  type="button"
                  onClick={() => choose(l.code, l.name)}
                  className={`flex w-full items-center justify-between px-4 py-2 text-left text-sm transition hover:bg-canvas dark:hover:bg-zinc-800 ${
                    l.code === lang ? "font-semibold text-brand-600 dark:text-brand-400" : "text-zinc-700 dark:text-zinc-200"
                  }`}
                >
                  <span>{l.native}</span>
                  <span className="text-xs text-zinc-400">{l.name}</span>
                </button>
              </li>
            ))}
            {custom && (
              <li>
                <button
                  type="button"
                  onClick={() => choose(custom.code, custom.name)}
                  className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-brand-600 transition hover:bg-canvas dark:text-brand-400 dark:hover:bg-zinc-800"
                >
                  <GlobeIcon className="h-4 w-4" />
                  Translate to “{custom.native}”
                </button>
              </li>
            )}
            {!custom && filtered.length === 0 && (
              <li className="px-4 py-3 text-sm text-zinc-400">No matches</li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
}
