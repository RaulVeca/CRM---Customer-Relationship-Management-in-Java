"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { ArrowLeftIcon, ArrowRightIcon } from "@/components/icons";
import type {
  BookingResponse,
  DayAvailability,
  Trainer,
} from "@/lib/types";

const WORK_START = 8; // 08:00 — earliest a session may start
const WORK_END = 20; // 20:00 — latest a session may end

const MONTHS_RO = [
  "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
  "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie",
];
// Monday-first weekday headers.
const WEEKDAYS_RO = ["Lu", "Ma", "Mi", "Jo", "Vi", "Sâ", "Du"];

const pad = (n: number) => String(n).padStart(2, "0");
const iso = (y: number, m: number, d: number) => `${y}-${pad(m + 1)}-${pad(d)}`;
const todayIso = () => {
  const n = new Date();
  return iso(n.getFullYear(), n.getMonth(), n.getDate());
};
const fmtHour = (h: number) => `${pad(h)}:00`;

/** Hours marked busy by the booked intervals, as a boolean lookup [0..WORK_END). */
function occupiedHours(day: DayAvailability | undefined): boolean[] {
  const occ = new Array(WORK_END).fill(false);
  if (!day) return occ;
  for (const b of day.booked) {
    for (let h = Math.max(b.start, WORK_START); h < Math.min(b.end, WORK_END); h++) {
      occ[h] = true;
    }
  }
  return occ;
}

/** Start hours in [WORK_START, WORK_END) at which a 1-hour session could begin. */
function freeStartHours(day: DayAvailability | undefined): number[] {
  const occ = occupiedHours(day);
  const out: number[] = [];
  for (let h = WORK_START; h < WORK_END; h++) if (!occ[h]) out.push(h);
  return out;
}

/** Longest session (in hours) that fits starting at `start` without hitting a booking. */
function maxDurationFrom(start: number, day: DayAvailability | undefined): number {
  const occ = occupiedHours(day);
  let h = start;
  while (h < WORK_END && !occ[h]) h++;
  return h - start;
}

export default function SchedulePage() {
  const [contact, setContact] = useState<{ id: number; email: string } | null>(null);
  const [trainers, setTrainers] = useState<Trainer[] | null>(null);
  const [trainer, setTrainer] = useState<Trainer | null>(null);

  // Visible month, kept as a {year, month} cursor (month is 0-based).
  const now = new Date();
  const [cursor, setCursor] = useState({ year: now.getFullYear(), month: now.getMonth() });
  const [days, setDays] = useState<Record<string, DayAvailability>>({});
  const [loadingCal, setLoadingCal] = useState(false);

  const [reloadKey, setReloadKey] = useState(0); // bumped to refetch availability
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [startHour, setStartHour] = useState<number | null>(null);
  const [duration, setDuration] = useState<number>(1);

  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<BookingResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const today = todayIso();
  const thisMonth = { year: now.getFullYear(), month: now.getMonth() };
  const atCurrentMonth = cursor.year === thisMonth.year && cursor.month === thisMonth.month;

  // --- session + trainers -------------------------------------------------
  useEffect(() => {
    const session = getSession();
    if (!session) return;
    setContact({ id: session.id, email: session.email });
    api
      .get<Trainer[]>("/api/trainers")
      .then(setTrainers)
      .catch((e) => setError((e as Error).message));
  }, []);

  // --- availability for the visible month ---------------------------------
  useEffect(() => {
    if (!trainer) return;
    const last = new Date(cursor.year, cursor.month + 1, 0).getDate();
    const from = iso(cursor.year, cursor.month, 1);
    const to = iso(cursor.year, cursor.month, last);
    let cancelled = false;
    setLoadingCal(true);
    api
      .get<DayAvailability[]>(`/api/trainers/${trainer.id}/availability?from=${from}&to=${to}`)
      .then((list) => {
        if (cancelled) return;
        const map: Record<string, DayAvailability> = {};
        for (const d of list) map[d.date] = d;
        setDays(map);
      })
      .catch((e) => {
        if (!cancelled) setError((e as Error).message);
      })
      .finally(() => {
        if (!cancelled) setLoadingCal(false);
      });
    return () => {
      cancelled = true;
    };
  }, [trainer, cursor.year, cursor.month, reloadKey]);

  // Grid of day-cells for the visible month, padded so the 1st lands on its weekday.
  const firstWeekday = (new Date(cursor.year, cursor.month, 1).getDay() + 6) % 7; // Mon=0
  const daysInMonth = new Date(cursor.year, cursor.month + 1, 0).getDate();
  const grid: (string | null)[] = [];
  for (let i = 0; i < firstWeekday; i++) grid.push(null);
  for (let d = 1; d <= daysInMonth; d++) grid.push(iso(cursor.year, cursor.month, d));

  function pickTrainer(t: Trainer) {
    setTrainer(t);
    setSelectedDate(null);
    setStartHour(null);
    setDuration(1);
    setResult(null);
    setError(null);
    setDays({});
  }

  function changeMonth(delta: number) {
    setSelectedDate(null);
    setStartHour(null);
    const m = cursor.month + delta;
    setCursor({ year: cursor.year + Math.floor(m / 12), month: ((m % 12) + 12) % 12 });
  }

  function pickDay(date: string) {
    const day = days[date];
    if (!day || !day.working || day.full || date < today) return;
    setSelectedDate(date);
    setStartHour(null);
    setDuration(1);
    setResult(null);
  }

  function pickStart(h: number) {
    setStartHour(h);
    setDuration(1);
  }

  async function confirm() {
    if (!trainer || !contact || !selectedDate || startHour == null) return;
    setSubmitting(true);
    setError(null);
    try {
      const res = await api.post<BookingResponse>(`/api/trainers/${trainer.id}/sessions`, {
        contactId: contact.id,
        email: contact.email,
        date: selectedDate,
        startHour,
        durationHours: duration,
      });
      setResult(res);
      setSelectedDate(null);
      setStartHour(null);
      setDuration(1);
      setReloadKey((k) => k + 1); // refetch so the new booking shows on the calendar
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSubmitting(false);
    }
  }

  const selectedDay = selectedDate ? days[selectedDate] : undefined;
  const starts = freeStartHours(selectedDay);
  const maxDuration = startHour == null ? 1 : maxDurationFrom(startHour, selectedDay);

  if (!contact) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
        <p className="text-slate-600 dark:text-slate-300">
          Trebuie să fii autentificat pentru a programa o ședință.
        </p>
        <Link
          href="/login"
          className="mt-3 inline-block rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2 text-sm font-semibold text-white hover:from-indigo-700 hover:to-violet-700"
        >
          Autentificare
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Programează o ședință online
        </h1>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Alege un trainer, apoi ziua, ora și câte ore vrei. Trainerii sunt disponibili de luni
          până sâmbătă, între {fmtHour(WORK_START)} și {fmtHour(WORK_END)}.
        </p>
      </div>

      {result && (
        <div className="rounded-xl border border-emerald-300 bg-emerald-50 p-4 text-emerald-800 dark:border-emerald-500/40 dark:bg-emerald-500/10 dark:text-emerald-300">
          {result.message} {result.trainerName}, {result.date}, {fmtHour(result.startHour)}–
          {fmtHour(result.endHour)}.
        </div>
      )}
      {error && (
        <div className="rounded-xl border border-red-300 bg-red-50 p-4 text-red-700 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300">
          {error}
        </div>
      )}

      {/* Step 1 — choose a trainer */}
      <section>
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
          1. Alege trainerul
        </h2>
        {trainers === null && !error && (
          <p className="text-slate-500 dark:text-slate-400">Se încarcă…</p>
        )}
        <div className="grid gap-3 sm:grid-cols-3">
          {trainers?.map((t) => {
            const active = trainer?.id === t.id;
            return (
              <button
                key={t.id}
                type="button"
                onClick={() => pickTrainer(t)}
                className={`rounded-xl border p-4 text-left transition-colors ${
                  active
                    ? "border-indigo-500 bg-indigo-50 ring-1 ring-indigo-500 dark:border-indigo-400 dark:bg-indigo-500/10"
                    : "border-slate-200 bg-white hover:border-indigo-300 dark:border-slate-800 dark:bg-slate-900 dark:hover:border-indigo-500/50"
                }`}
              >
                <p className="font-semibold text-slate-900 dark:text-white">{t.fullName}</p>
                <p className="mt-0.5 truncate text-xs text-slate-500 dark:text-slate-400">{t.email}</p>
              </button>
            );
          })}
        </div>
      </section>

      {/* Step 2 — calendar */}
      {trainer && (
        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
            2. Alege ziua
          </h2>
          <div className="rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
            <div className="mb-3 flex items-center justify-between">
              <button
                type="button"
                onClick={() => changeMonth(-1)}
                disabled={atCurrentMonth}
                className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 disabled:opacity-30 disabled:hover:bg-transparent dark:text-slate-400 dark:hover:bg-slate-800"
                aria-label="Luna anterioară"
              >
                <ArrowLeftIcon />
              </button>
              <span className="font-semibold text-slate-900 dark:text-white">
                {MONTHS_RO[cursor.month]} {cursor.year}
              </span>
              <button
                type="button"
                onClick={() => changeMonth(1)}
                className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800"
                aria-label="Luna următoare"
              >
                <ArrowRightIcon />
              </button>
            </div>

            <div className="mb-1 grid grid-cols-7 gap-1 text-center text-xs font-medium text-slate-400 dark:text-slate-500">
              {WEEKDAYS_RO.map((w) => (
                <div key={w} className="py-1">{w}</div>
              ))}
            </div>

            <div className="grid grid-cols-7 gap-1">
              {grid.map((date, idx) => {
                if (!date) return <div key={`empty-${idx}`} />;
                const day = days[date];
                const dayNum = Number(date.slice(8, 10));
                const isPast = date < today;
                const selectable = !!day && day.working && !day.full && !isPast;
                const isSelected = date === selectedDate;

                let cls =
                  "flex h-10 items-center justify-center rounded-lg text-sm transition-colors ";
                if (isSelected) {
                  cls += "bg-indigo-600 font-semibold text-white";
                } else if (selectable) {
                  cls +=
                    "cursor-pointer bg-slate-50 text-slate-700 hover:bg-indigo-100 dark:bg-slate-800 dark:text-slate-200 dark:hover:bg-indigo-500/20";
                } else {
                  // Sunday, past, or fully-booked → greyed out, not selectable.
                  cls +=
                    "cursor-not-allowed bg-slate-100 text-slate-300 dark:bg-slate-800/40 dark:text-slate-600";
                }

                const title = isPast
                  ? "Zi trecută"
                  : !day || !day.working
                    ? "Indisponibil"
                    : day.full
                      ? "Zi ocupată complet"
                      : undefined;

                return (
                  <button
                    key={date}
                    type="button"
                    disabled={!selectable}
                    onClick={() => pickDay(date)}
                    className={cls}
                    title={title}
                  >
                    {dayNum}
                  </button>
                );
              })}
            </div>

            <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-400 dark:text-slate-500">
              <span className="flex items-center gap-1.5">
                <span className="inline-block h-3 w-3 rounded bg-slate-50 ring-1 ring-slate-200 dark:bg-slate-800 dark:ring-slate-700" />
                Disponibil
              </span>
              <span className="flex items-center gap-1.5">
                <span className="inline-block h-3 w-3 rounded bg-slate-200 dark:bg-slate-700" />
                Ocupat / indisponibil
              </span>
              {loadingCal && <span>Se actualizează…</span>}
            </div>
          </div>
        </section>
      )}

      {/* Step 3 — hour + duration */}
      {selectedDate && (
        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
            3. Alege ora și durata
          </h2>
          <div className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
            <div>
              <p className="mb-2 text-sm text-slate-600 dark:text-slate-300">Ora de început</p>
              {starts.length === 0 ? (
                <p className="text-sm text-slate-400">Nu mai sunt ore libere în această zi.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {starts.map((h) => (
                    <button
                      key={h}
                      type="button"
                      onClick={() => pickStart(h)}
                      className={`rounded-lg border px-3 py-1.5 text-sm transition-colors ${
                        startHour === h
                          ? "border-indigo-500 bg-indigo-600 text-white"
                          : "border-slate-200 bg-white text-slate-700 hover:border-indigo-300 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200"
                      }`}
                    >
                      {fmtHour(h)}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {startHour != null && (
              <div>
                <p className="mb-2 text-sm text-slate-600 dark:text-slate-300">
                  Câte ore? (maxim {maxDuration} {maxDuration === 1 ? "oră" : "ore"} consecutive de
                  la {fmtHour(startHour)})
                </p>
                <div className="flex flex-wrap gap-2">
                  {Array.from({ length: maxDuration }, (_, i) => i + 1).map((d) => (
                    <button
                      key={d}
                      type="button"
                      onClick={() => setDuration(d)}
                      className={`rounded-lg border px-3 py-1.5 text-sm transition-colors ${
                        duration === d
                          ? "border-indigo-500 bg-indigo-600 text-white"
                          : "border-slate-200 bg-white text-slate-700 hover:border-indigo-300 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200"
                      }`}
                    >
                      {d}h
                    </button>
                  ))}
                </div>
              </div>
            )}

            {startHour != null && (
              <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-4 dark:border-slate-800">
                <p className="text-sm text-slate-600 dark:text-slate-300">
                  <span className="font-semibold text-slate-900 dark:text-white">{trainer?.fullName}</span>
                  {" · "}
                  {selectedDate}
                  {" · "}
                  {fmtHour(startHour)}–{fmtHour(startHour + duration)}
                </p>
                <button
                  type="button"
                  onClick={confirm}
                  disabled={submitting}
                  className="rounded-lg bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-2 text-sm font-semibold text-white hover:from-indigo-700 hover:to-violet-700 disabled:opacity-60"
                >
                  {submitting ? "Se programează…" : "Confirmă programarea"}
                </button>
              </div>
            )}
          </div>
        </section>
      )}
    </div>
  );
}
