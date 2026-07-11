"use client";

import { createContext, useContext, useEffect, useRef, useState } from "react";
import { startTranslation } from "@/lib/translate";
import { DEFAULT_LANG, isRtl } from "@/lib/languages";

const STORAGE_KEY = "lang";

interface TranslationCtx {
  /** Active language code ("en" = original, untranslated). */
  lang: string;
  /** True while translation requests are in flight. */
  busy: boolean;
  /**
   * Switch language. Persists the choice and reloads so the page is
   * re-translated from clean English — guaranteeing a full, correct result.
   */
  setLang: (code: string, name: string) => void;
}

const Ctx = createContext<TranslationCtx>({
  lang: "en",
  busy: false,
  setLang: () => {},
});

export const useTranslation = () => useContext(Ctx);

export default function TranslationProvider({ children }: { children: React.ReactNode }) {
  const [lang, setLangState] = useState<string>("en");
  const [busy, setBusy] = useState(false);
  const engineRef = useRef<{ stop: () => void } | null>(null);

  // Read the persisted choice after mount (avoids hydration mismatch) and start
  // the engine if a non-English language is active.
  useEffect(() => {
    let code = DEFAULT_LANG.code;
    let name = DEFAULT_LANG.name;
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored) as { code: string; name: string };
        if (parsed?.code && parsed.code !== "en") {
          code = parsed.code;
          name = parsed.name;
        }
      }
    } catch {
      /* ignore */
    }

    setLangState(code);

    if (code !== "en") {
      document.documentElement.lang = code;
      document.documentElement.dir = isRtl(code) ? "rtl" : "ltr";
      engineRef.current = startTranslation(code, name, setBusy);
    }

    return () => {
      engineRef.current?.stop();
      engineRef.current = null;
    };
  }, []);

  function setLang(code: string, name: string) {
    if (code === lang) return;
    try {
      if (code === "en") {
        localStorage.removeItem(STORAGE_KEY);
      } else {
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ code, name }));
      }
    } catch {
      /* ignore */
    }
    // Reload so translation always starts from the pristine English DOM.
    window.location.reload();
  }

  return <Ctx.Provider value={{ lang, busy, setLang }}>{children}</Ctx.Provider>;
}
