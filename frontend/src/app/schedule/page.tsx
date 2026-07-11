"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import { SCHEDULE_EVENT, closeSchedule, isScheduleOpen } from "@/lib/schedule";
import { ArrowLeftIcon, ArrowRightIcon } from "@/components/icons";
import type {
  BookingResponse,
  DayAvailability,
  MyPurchase,
  Trainer,
} from "@/lib/types";

const WORK_START = 8; // 08:00 — earliest a session may start
const WORK_END = 20; // 20:00 — latest a session may end
const PRICE_PER_HOUR = 10; // USD charged per booked hour

// Bank details the contact transfers the session fee to. Fixed, display-only text.
const BANK_NAME = "Banca Transilvania";
const BANK_IBAN = "RO49 BTRL 0000 1234 5678 9012";
const COMPANY_NAME = "TrainingIT SRL";

const MONTHS = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];
// Monday-first weekday headers.
const WEEKDAYS = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"];

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
  // Automatic price reduction for this account (0 = none). Only employee
  // accounts carry one; contacts stay at full price.
  const [discountRate, setDiscountRate] = useState(0);
  // null = not yet read (avoids a flash before the effect runs); the window may
  // only be reached after pressing "Schedule a session" in My courses.
  const [windowOpen, setWindowOpen] = useState<boolean | null>(null);
  // null = ownership not yet known. Booking requires owning at least one course.
  const [hasPurchases, setHasPurchases] = useState<boolean | null>(null);
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
  // The payment window shown after "Confirm booking". The session is only booked
  // once the contact presses "Transfer" inside it.
  const [showPayment, setShowPayment] = useState(false);

  const today = todayIso();
  const thisMonth = { year: now.getFullYear(), month: now.getMonth() };
  const atCurrentMonth = cursor.year === thisMonth.year && cursor.month === thisMonth.month;

  // --- session + trainers -------------------------------------------------
  useEffect(() => {
    const session = getSession();
    setWindowOpen(isScheduleOpen());
    const sync = () => setWindowOpen(isScheduleOpen());
    window.addEventListener(SCHEDULE_EVENT, sync);
    window.addEventListener("storage", sync);
    if (!session) return () => {
      window.removeEventListener(SCHEDULE_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
    setContact({ id: session.id, email: session.email });
    setDiscountRate(session.discountRate ?? 0);
    // A contact can only book a session for a course it actually owns. Verify
    // ownership against the server and drop any stale window flag if it owns none.
    api
      .get<MyPurchase[]>(`/api/public/me/purchases?email=${encodeURIComponent(session.email)}`)
      .then((list) => {
        const owns = list.length > 0;
        setHasPurchases(owns);
        if (!owns) closeSchedule();
      })
      .catch(() => setHasPurchases(false));
    api
      .get<Trainer[]>("/api/trainers")
      .then(setTrainers)
      .catch((e) => setError((e as Error).message));
    return () => {
      window.removeEventListener(SCHEDULE_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
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

  // "Confirm booking" only opens the payment window; nothing is booked yet.
  function confirm() {
    if (!trainer || !contact || !selectedDate || startHour == null) return;
    setError(null);
    setShowPayment(true);
  }

  // "Transfer" inside the payment window is what actually books the session.
  async function transfer() {
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
      setShowPayment(false);
      setSelectedDate(null);
      setStartHour(null);
      setDuration(1);
      setReloadKey((k) => k + 1); // refetch so the new booking shows on the calendar
      closeSchedule(); // booking succeeded — the window disappears until reopened
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSubmitting(false);
    }
  }

  const selectedDay = selectedDate ? days[selectedDate] : undefined;
  const starts = freeStartHours(selectedDay);
  const maxDuration = startHour == null ? 1 : maxDurationFrom(startHour, selectedDay);

  // Session price: full price, and the discounted price for accounts that carry
  // a discount (employees). Contacts have discountRate 0, so net === gross.
  const grossPrice = duration * PRICE_PER_HOUR;
  const netPrice = Math.round(grossPrice * (1 - discountRate) * 100) / 100;
  const hasDiscount = discountRate > 0;
  const discountPct = Math.round(discountRate * 100);
  const fmtUSD = (n: number) => (Number.isInteger(n) ? `$${n}` : `$${n.toFixed(2)}`);

  if (!contact) {
    return (
      <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-8 text-center dark:border-zinc-700 dark:bg-zinc-900">
        <p className="text-zinc-600 dark:text-zinc-300">
          You must be signed in to schedule a session.
        </p>
        <Link
          href="/login"
          className="mt-3 inline-block rounded-lg bg-gradient-to-r from-brand-600 to-brand-600 px-4 py-2 text-sm font-semibold text-white hover:from-brand-700 hover:to-brand-700"
        >
          Log in
        </Link>
      </div>
    );
  }

  // Hard gate: without a purchased course there is nothing to schedule a session
  // for, so booking is refused no matter what the local window flag says.
  if (hasPurchases === false && !result) {
    return (
      <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-8 text-center dark:border-zinc-700 dark:bg-zinc-900">
        <p className="text-zinc-600 dark:text-zinc-300">
          You must buy a course before you can schedule an online session.
        </p>
        <Link
          href="/"
          className="mt-3 inline-block rounded-lg bg-gradient-to-r from-brand-600 to-brand-600 px-4 py-2 text-sm font-semibold text-white hover:from-brand-700 hover:to-brand-700"
        >
          Browse courses
        </Link>
      </div>
    );
  }

  // Ownership still being verified — don't flash the booking form before we know
  // whether the contact is even allowed to book.
  if (hasPurchases === null && !result) {
    return (
      <p className="py-10 text-center text-sm text-zinc-500 dark:text-zinc-400">
        Loading…
      </p>
    );
  }

  // The window only exists once opened from My courses, and is gone after a
  // booking. Still show the success message (result) before it disappears.
  if (windowOpen === false && !result) {
    return (
      <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-8 text-center dark:border-zinc-700 dark:bg-zinc-900">
        <p className="text-zinc-600 dark:text-zinc-300">
          Press "Schedule a session" next to a purchased course to open scheduling.
        </p>
        <Link
          href="/my-courses"
          className="mt-3 inline-block rounded-lg bg-gradient-to-r from-brand-600 to-brand-600 px-4 py-2 text-sm font-semibold text-white hover:from-brand-700 hover:to-brand-700"
        >
          My courses
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900 dark:text-white">
          Schedule an online session
        </h1>
        <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
          Choose a trainer, then the day, the time and how many hours you want. Trainers are
          available Monday to Saturday, between {fmtHour(WORK_START)} and {fmtHour(WORK_END)}.
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
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
          1. Choose the trainer
        </h2>
        {trainers === null && !error && (
          <p className="text-zinc-500 dark:text-zinc-400">Loading…</p>
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
                    ? "border-brand-500 bg-brand-50 ring-1 ring-brand-500 dark:border-brand-400 dark:bg-brand-500/10"
                    : "border-zinc-200 bg-white hover:border-brand-300 dark:border-zinc-800 dark:bg-zinc-900 dark:hover:border-brand-500/50"
                }`}
              >
                <p className="font-semibold text-zinc-900 dark:text-white">{t.fullName}</p>
                <p className="mt-0.5 truncate text-xs text-zinc-500 dark:text-zinc-400">{t.email}</p>
              </button>
            );
          })}
        </div>
      </section>

      {/* Step 2 — calendar */}
      {trainer && (
        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
            2. Choose the day
          </h2>
          <div className="rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900">
            <div className="mb-3 flex items-center justify-between">
              <button
                type="button"
                onClick={() => changeMonth(-1)}
                disabled={atCurrentMonth}
                className="rounded-lg p-1.5 text-zinc-500 hover:bg-zinc-100 disabled:opacity-30 disabled:hover:bg-transparent dark:text-zinc-400 dark:hover:bg-zinc-800"
                aria-label="Previous month"
              >
                <ArrowLeftIcon />
              </button>
              <span className="font-semibold text-zinc-900 dark:text-white">
                {MONTHS[cursor.month]} {cursor.year}
              </span>
              <button
                type="button"
                onClick={() => changeMonth(1)}
                className="rounded-lg p-1.5 text-zinc-500 hover:bg-zinc-100 dark:text-zinc-400 dark:hover:bg-zinc-800"
                aria-label="Next month"
              >
                <ArrowRightIcon />
              </button>
            </div>

            <div className="mb-1 grid grid-cols-7 gap-1 text-center text-xs font-medium text-zinc-400 dark:text-zinc-500">
              {WEEKDAYS.map((w) => (
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
                  cls += "bg-brand-600 font-semibold text-white";
                } else if (selectable) {
                  cls +=
                    "cursor-pointer bg-zinc-50 text-zinc-700 hover:bg-brand-100 dark:bg-zinc-800 dark:text-zinc-200 dark:hover:bg-brand-500/20";
                } else {
                  // Sunday, past, or fully-booked → greyed out, not selectable.
                  cls +=
                    "cursor-not-allowed bg-zinc-100 text-zinc-300 dark:bg-zinc-800/40 dark:text-zinc-600";
                }

                const title = isPast
                  ? "Past day"
                  : !day || !day.working
                    ? "Unavailable"
                    : day.full
                      ? "Fully booked day"
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

            <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-zinc-400 dark:text-zinc-500">
              <span className="flex items-center gap-1.5">
                <span className="inline-block h-3 w-3 rounded bg-zinc-50 ring-1 ring-zinc-200 dark:bg-zinc-800 dark:ring-zinc-700" />
                Available
              </span>
              <span className="flex items-center gap-1.5">
                <span className="inline-block h-3 w-3 rounded bg-zinc-200 dark:bg-zinc-700" />
                Booked / unavailable
              </span>
              {loadingCal && <span>Updating…</span>}
            </div>
          </div>
        </section>
      )}

      {/* Step 3 — hour + duration */}
      {selectedDate && (
        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
            3. Choose the time and duration
          </h2>
          <div className="space-y-4 rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900">
            <div>
              <p className="mb-2 text-sm text-zinc-600 dark:text-zinc-300">Start time</p>
              {starts.length === 0 ? (
                <p className="text-sm text-zinc-400">No free hours left on this day.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {starts.map((h) => (
                    <button
                      key={h}
                      type="button"
                      onClick={() => pickStart(h)}
                      className={`rounded-lg border px-3 py-1.5 text-sm transition-colors ${
                        startHour === h
                          ? "border-brand-500 bg-brand-600 text-white"
                          : "border-zinc-200 bg-white text-zinc-700 hover:border-brand-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-200"
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
                <p className="mb-2 text-sm text-zinc-600 dark:text-zinc-300">
                  How many hours? (max {maxDuration} consecutive {maxDuration === 1 ? "hour" : "hours"}
                  {" "}from {fmtHour(startHour)})
                </p>
                <div className="flex flex-wrap gap-2">
                  {Array.from({ length: maxDuration }, (_, i) => i + 1).map((d) => (
                    <button
                      key={d}
                      type="button"
                      onClick={() => setDuration(d)}
                      className={`rounded-lg border px-3 py-1.5 text-sm transition-colors ${
                        duration === d
                          ? "border-brand-500 bg-brand-600 text-white"
                          : "border-zinc-200 bg-white text-zinc-700 hover:border-brand-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-200"
                      }`}
                    >
                      {d}h
                    </button>
                  ))}
                </div>
              </div>
            )}

            {startHour != null && (
              <div className="flex flex-wrap items-center justify-between gap-3 border-t border-zinc-100 pt-4 dark:border-zinc-800">
                <p className="text-sm text-zinc-600 dark:text-zinc-300">
                  <span className="font-semibold text-zinc-900 dark:text-white">{trainer?.fullName}</span>
                  {" · "}
                  {selectedDate}
                  {" · "}
                  {fmtHour(startHour)}–{fmtHour(startHour + duration)}
                  {" · "}
                  {hasDiscount ? (
                    <>
                      <span className="text-zinc-400 line-through dark:text-zinc-500">
                        {fmtUSD(grossPrice)}
                      </span>
                      {" "}
                      <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                        {fmtUSD(netPrice)}
                      </span>
                      {" "}
                      <span className="rounded bg-emerald-100 px-1.5 py-0.5 text-xs font-semibold text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300">
                        −{discountPct}%
                      </span>
                    </>
                  ) : (
                    <>
                      <span className="font-semibold text-zinc-900 dark:text-white">
                        {fmtUSD(grossPrice)}
                      </span>
                      <span className="text-zinc-500 dark:text-zinc-400">
                        {" "}(${PRICE_PER_HOUR}/hour × {duration}h)
                      </span>
                    </>
                  )}
                </p>
                <button
                  type="button"
                  onClick={confirm}
                  disabled={submitting}
                  className="rounded-lg bg-gradient-to-r from-brand-600 to-brand-600 px-4 py-2 text-sm font-semibold text-white hover:from-brand-700 hover:to-brand-700 disabled:opacity-60"
                >
                  Confirm booking — {fmtUSD(netPrice)}
                </button>
              </div>
            )}
          </div>
        </section>
      )}

      {/* Payment window — shown after "Confirm booking". The session is booked
          only when "Transfer" is pressed. */}
      {showPayment && startHour != null && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-900/50 p-4">
          <div className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-6 shadow-xl dark:border-zinc-800 dark:bg-zinc-900">
            <h3 className="text-lg font-bold text-zinc-900 dark:text-white">
              Complete your payment
            </h3>
            <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              Transfer the session fee to the account below, then press Transfer to
              confirm your booking.
            </p>

            <dl className="mt-4 space-y-3 rounded-xl border border-zinc-200 bg-zinc-50 p-4 text-sm dark:border-zinc-800 dark:bg-zinc-800/40">
              <div className="flex items-center justify-between gap-3">
                <dt className="text-zinc-500 dark:text-zinc-400">Company</dt>
                <dd className="font-semibold text-zinc-900 dark:text-white">{COMPANY_NAME}</dd>
              </div>
              <div className="flex items-center justify-between gap-3">
                <dt className="text-zinc-500 dark:text-zinc-400">Bank</dt>
                <dd className="font-semibold text-zinc-900 dark:text-white">{BANK_NAME}</dd>
              </div>
              <div className="flex items-center justify-between gap-3">
                <dt className="text-zinc-500 dark:text-zinc-400">IBAN</dt>
                <dd className="font-mono font-semibold text-zinc-900 dark:text-white">{BANK_IBAN}</dd>
              </div>
              {hasDiscount && (
                <div className="flex items-center justify-between gap-3">
                  <dt className="text-zinc-500 dark:text-zinc-400">
                    Employee discount
                  </dt>
                  <dd className="font-semibold text-emerald-600 dark:text-emerald-400">
                    −{discountPct}%
                  </dd>
                </div>
              )}
              <div className="flex items-center justify-between gap-3 border-t border-zinc-200 pt-3 dark:border-zinc-700">
                <dt className="text-zinc-500 dark:text-zinc-400">Amount</dt>
                <dd className="text-right font-semibold text-zinc-900 dark:text-white">
                  {hasDiscount ? (
                    <>
                      <span className="mr-2 font-normal text-zinc-400 line-through dark:text-zinc-500">
                        {fmtUSD(grossPrice)}
                      </span>
                      <span className="text-emerald-600 dark:text-emerald-400">
                        {fmtUSD(netPrice)}
                      </span>
                    </>
                  ) : (
                    fmtUSD(grossPrice)
                  )}
                </dd>
              </div>
            </dl>

            {error && (
              <div className="mt-4 rounded-lg border border-red-300 bg-red-50 p-3 text-sm text-red-700 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300">
                {error}
              </div>
            )}

            <div className="mt-6 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowPayment(false)}
                disabled={submitting}
                className="rounded-lg border border-zinc-300 bg-white px-4 py-2 text-sm font-semibold text-zinc-700 hover:bg-zinc-50 disabled:opacity-60 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={transfer}
                disabled={submitting}
                className="rounded-lg bg-gradient-to-r from-brand-600 to-brand-600 px-4 py-2 text-sm font-semibold text-white hover:from-brand-700 hover:to-brand-700 disabled:opacity-60"
              >
                {submitting ? "Transferring…" : `Transfer ${fmtUSD(netPrice)}`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
