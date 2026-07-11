"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import { ChatIcon, CloseIcon } from "@/components/icons";

interface Msg {
  role: "user" | "assistant";
  content: string;
}

export default function Chatbot() {
  const [open, setOpen] = useState(false);
  const [enabled, setEnabled] = useState(false);
  const [messages, setMessages] = useState<Msg[]>([
    { role: "assistant", content: "Hi! I'm the TrainingIT assistant. Ask me about our courses." },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const audioCtxRef = useRef<AudioContext | null>(null);

  useEffect(() => {
    api.get<{ enabled: boolean }>("/api/ai/status").then((s) => setEnabled(s.enabled)).catch(() => {});
  }, []);

  /** Plays a short, pleasant two-tone chime when the assistant replies. */
  function playChime() {
    try {
      const Ctx = window.AudioContext ?? (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
      if (!Ctx) return;
      const ctx = audioCtxRef.current ?? (audioCtxRef.current = new Ctx());
      if (ctx.state === "suspended") void ctx.resume();

      const now = ctx.currentTime;
      // A5 -> D6, a gentle rising notification.
      [{ f: 880, t: 0 }, { f: 1174.66, t: 0.11 }].forEach(({ f, t }) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = "sine";
        osc.frequency.value = f;
        gain.gain.setValueAtTime(0.0001, now + t);
        gain.gain.exponentialRampToValueAtTime(0.12, now + t + 0.02);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + t + 0.25);
        osc.connect(gain).connect(ctx.destination);
        osc.start(now + t);
        osc.stop(now + t + 0.3);
      });
    } catch {
      /* audio not available — ignore */
    }
  }

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, open]);

  async function send(e: React.FormEvent) {
    e.preventDefault();
    const text = input.trim();
    if (!text || loading) return;
    const next: Msg[] = [...messages, { role: "user", content: text }];
    setMessages(next);
    setInput("");
    setLoading(true);
    try {
      // Send only the user/assistant exchange (skip the initial greeting).
      const history = next.filter((m, i) => !(i === 0 && m.role === "assistant"));
      const res = await api.post<{ reply: string }>("/api/ai/chat", { messages: history });
      setMessages((m) => [...m, { role: "assistant", content: res.reply }]);
      playChime();
    } catch (err) {
      setMessages((m) => [...m, { role: "assistant", content: "Sorry, I couldn't answer right now." }]);
      playChime();
    } finally {
      setLoading(false);
    }
  }

  if (!enabled) return null;

  return (
    <>
      <button
        onClick={() => setOpen((o) => !o)}
        className="fixed bottom-5 right-5 z-50 flex h-14 w-14 items-center justify-center rounded-full bg-brand-500 text-2xl text-white shadow-lg shadow-brand-500/30 transition hover:bg-brand-600"
        aria-label="Open chat"
      >
        {open ? <CloseIcon /> : <ChatIcon />}
      </button>

      {open && (
        <div className="fixed bottom-24 right-5 z-50 flex h-[28rem] w-80 flex-col overflow-hidden rounded-2xl border border-black/5 bg-white shadow-2xl dark:border-white/10 dark:bg-zinc-900">
          <div className="bg-brand-500 px-4 py-3 text-sm font-semibold text-white">TrainingIT Assistant</div>
          <div className="flex-1 space-y-3 overflow-y-auto p-3">
            {messages.map((m, i) => (
              <div key={i} className={`flex ${m.role === "user" ? "justify-end" : "justify-start"}`}>
                <div
                  className={`max-w-[85%] whitespace-pre-wrap rounded-2xl px-3 py-2 text-sm ${
                    m.role === "user" ? "bg-brand-500 text-white" : "bg-canvas text-zinc-800 dark:bg-zinc-800 dark:text-zinc-200"
                  }`}
                >
                  {m.content}
                </div>
              </div>
            ))}
            {loading && <div className="text-xs text-zinc-400 dark:text-zinc-500">Assistant is typing…</div>}
            <div ref={bottomRef} />
          </div>
          <form onSubmit={send} className="flex gap-2 border-t border-black/5 p-2 dark:border-white/10">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type a message…"
              className="flex-1 rounded-full border border-zinc-300 px-3 py-1.5 text-sm outline-none focus:border-brand-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder:text-zinc-500"
            />
            <button className="rounded-full bg-brand-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:opacity-50" disabled={loading}>
              Send
            </button>
          </form>
        </div>
      )}
    </>
  );
}
