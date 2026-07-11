// Live, whole-page translation engine.
//
// Strategy: we only ever translate the ORIGINAL (English) DOM into a target
// language. Switching language or returning to English is done by reloading the
// page (see TranslationProvider), so the source text is always clean English and
// the result is always "full and correct" — we never have to un-translate.
//
// A running dictionary (english -> translated) is filled from a localStorage
// cache and from batched calls to the backend `/api/ai/translate` endpoint. A
// MutationObserver re-applies the dictionary as the SPA navigates and as async
// data (e.g. course lists) loads in, so nothing is missed.

import { api } from "@/lib/api";

const CHUNK = 25; // strings per backend request
const MAX_PARALLEL = 3; // concurrent backend requests
const DEBOUNCE_MS = 200;

// Attributes whose values are user-visible and worth translating.
const ATTRS = ["placeholder", "title", "aria-label", "alt"] as const;

const SKIP_TAGS = new Set(["SCRIPT", "STYLE", "NOSCRIPT", "CODE", "PRE", "TEXTAREA"]);

// Skip strings that are purely symbols / numbers / punctuation — nothing to translate.
function translatable(s: string): boolean {
  const t = s.trim();
  if (t.length < 2) return false;
  if (!/[\p{L}]/u.test(t)) return false; // must contain at least one letter
  return true;
}

interface Engine {
  stop: () => void;
}

/**
 * Start translating the current document into `languageName` (an English
 * language name such as "French"). Returns a handle to stop the engine.
 * `onBusy` is called with true while network requests are in flight.
 */
export function startTranslation(
  langCode: string,
  languageName: string,
  onBusy: (busy: boolean) => void,
): Engine {
  const cacheKey = `tr:${langCode}`;
  const dict = new Map<string, string>();
  const translated = new Set<string>(); // known translated values — never re-translate these
  const pending = new Set<string>();
  const inFlight = new Set<string>();
  let stopped = false;
  let applying = false; // guards the observer against our own writes
  let debounce: ReturnType<typeof setTimeout> | null = null;
  let observer: MutationObserver | null = null;

  // Load cached translations for this language.
  try {
    const raw = localStorage.getItem(cacheKey);
    if (raw) {
      const obj = JSON.parse(raw) as Record<string, string>;
      for (const [k, v] of Object.entries(obj)) {
        dict.set(k, v);
        translated.add(v);
      }
    }
  } catch {
    /* corrupt cache — ignore */
  }

  function persist() {
    try {
      localStorage.setItem(cacheKey, JSON.stringify(Object.fromEntries(dict)));
    } catch {
      /* quota / unavailable — ignore, translations still work in-memory */
    }
  }

  function shouldSkip(el: Element | null): boolean {
    if (!el) return true;
    if (SKIP_TAGS.has(el.tagName)) return true;
    return !!el.closest("[data-no-translate]");
  }

  // Walk the tree, apply known translations, and queue unknown strings.
  function scan(root: Node) {
    // Text nodes
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
      acceptNode(node) {
        const v = node.nodeValue;
        if (!v || !translatable(v)) return NodeFilter.FILTER_REJECT;
        if (shouldSkip(node.parentElement)) return NodeFilter.FILTER_REJECT;
        return NodeFilter.FILTER_ACCEPT;
      },
    });
    const textNodes: Text[] = [];
    for (let n = walker.nextNode(); n; n = walker.nextNode()) textNodes.push(n as Text);

    for (const node of textNodes) {
      const raw = node.nodeValue as string;
      const key = raw.trim();
      const hit = dict.get(key);
      if (hit !== undefined && hit !== key) {
        // Preserve surrounding whitespace so layout/spacing is unchanged.
        const leadWs = raw.match(/^\s*/)?.[0] ?? "";
        const trailWs = raw.match(/\s*$/)?.[0] ?? "";
        const next = leadWs + hit + trailWs;
        if (node.nodeValue !== next) node.nodeValue = next;
      } else if (hit === undefined && !translated.has(key)) {
        pending.add(key);
      }
    }

    // Translatable attributes
    const rootEl = root instanceof Element ? root : (root as Document).body ?? document.body;
    if (rootEl) {
      const scope: Element[] = [];
      if (root instanceof Element) scope.push(root);
      rootEl
        .querySelectorAll?.(ATTRS.map((a) => `[${a}]`).join(","))
        .forEach((el) => scope.push(el));
      for (const el of scope) {
        if (shouldSkip(el)) continue;
        for (const attr of ATTRS) {
          const val = el.getAttribute(attr);
          if (!val || !translatable(val)) continue;
          const key = val.trim();
          const hit = dict.get(key);
          if (hit !== undefined && hit !== key) {
            if (el.getAttribute(attr) !== hit) el.setAttribute(attr, hit);
          } else if (hit === undefined && !translated.has(key)) {
            pending.add(key);
          }
        }
      }
    }
  }

  function applyAll() {
    if (stopped) return;
    applying = true;
    try {
      scan(document.body);
    } finally {
      applying = false;
    }
    void flush();
  }

  async function flush() {
    if (stopped) return;
    const todo = [...pending].filter((s) => !dict.has(s) && !inFlight.has(s));
    pending.clear();
    if (todo.length === 0) return;

    // Build chunks.
    const chunks: string[][] = [];
    for (let i = 0; i < todo.length; i += CHUNK) {
      const c = todo.slice(i, i + CHUNK);
      c.forEach((s) => inFlight.add(s));
      chunks.push(c);
    }

    onBusy(true);
    let changed = false;
    // Run chunks with limited concurrency.
    for (let i = 0; i < chunks.length; i += MAX_PARALLEL) {
      if (stopped) break;
      const batch = chunks.slice(i, i + MAX_PARALLEL);
      await Promise.all(
        batch.map(async (chunk) => {
          try {
            const res = await api.post<{ translations: string[] }>("/api/ai/translate", {
              texts: chunk,
              targetLanguage: languageName,
            });
            const out = res.translations ?? [];
            chunk.forEach((s, idx) => {
              const t = out[idx] ?? s;
              dict.set(s, t);
              translated.add(t);
            });
            changed = true;
          } catch {
            // On failure, cache identity so we don't hammer the backend; the
            // original English stays visible.
            chunk.forEach((s) => dict.set(s, s));
          } finally {
            chunk.forEach((s) => inFlight.delete(s));
          }
        }),
      );
      if (changed) {
        persist();
        applying = true;
        try {
          scan(document.body);
        } finally {
          applying = false;
        }
      }
    }
    onBusy(false);

    // New strings may have surfaced while we were working.
    if (!stopped && pending.size > 0) void flush();
  }

  function schedule() {
    if (debounce) clearTimeout(debounce);
    debounce = setTimeout(() => {
      debounce = null;
      applyAll();
    }, DEBOUNCE_MS);
  }

  // Observe the whole document for SPA navigation & async content.
  observer = new MutationObserver((mutations) => {
    if (applying) return;
    for (const m of mutations) {
      if (m.type === "childList" && (m.addedNodes.length || m.removedNodes.length)) {
        schedule();
        return;
      }
      if (m.type === "characterData") {
        schedule();
        return;
      }
    }
  });
  observer.observe(document.body, {
    childList: true,
    subtree: true,
    characterData: true,
  });

  // First pass.
  applyAll();

  return {
    stop() {
      stopped = true;
      if (debounce) clearTimeout(debounce);
      observer?.disconnect();
      observer = null;
    },
  };
}
