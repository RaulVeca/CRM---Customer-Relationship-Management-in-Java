"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { getSession } from "@/lib/auth";
import CourseQuiz from "@/components/CourseQuiz";
import CourseCard from "@/components/CourseCard";
import ReviewsTicker from "@/components/ReviewsTicker";
import Logo from "@/components/Logo";
import {
  CodeIcon,
  LaptopIcon,
  BriefcaseIcon,
  CertificateIcon,
  HeadsetIcon,
  RocketIcon,
  CheckIcon,
  QuoteIcon,
  StarBurstIcon,
  ArrowRightIcon,
  UserIcon,
} from "@/components/icons";
import type { Category, MyPurchase, PublicCourse } from "@/lib/types";

const SEEN_KEY = "ctrSeenCourses";

/**
 * Count a catalog impression only the first time this browser sees a course.
 * Re-entering Courses, refreshing or switching category no longer inflates the
 * view count — the same visitor is only counted once per course.
 */
function trackImpressions(courseIds: number[]) {
  if (courseIds.length === 0) return;
  let seen: number[] = [];
  try {
    seen = JSON.parse(localStorage.getItem(SEEN_KEY) ?? "[]");
  } catch {
    seen = [];
  }
  const seenSet = new Set(seen);
  const fresh = courseIds.filter((id) => !seenSet.has(id));
  if (fresh.length === 0) return;

  api.post("/api/public/metrics/impressions", { courseIds: fresh }).catch(() => {});
  try {
    localStorage.setItem(SEEN_KEY, JSON.stringify([...seenSet, ...fresh]));
  } catch {
    /* localStorage unavailable — skip persistence */
  }
}

// ---- Static marketing content for the landing sections ---------------------

const SERVICES = [
  {
    Icon: CodeIcon,
    title: "Instructor-led courses",
    body: "Live, structured courses in programming, cloud, AI and data science, taught by working engineers.",
  },
  {
    Icon: LaptopIcon,
    title: "Hands-on labs",
    body: "Practice on real projects and guided exercises, so every concept is reinforced by doing, not just watching.",
  },
  {
    Icon: BriefcaseIcon,
    title: "Corporate training",
    body: "Upskill entire teams with tailored programmes, group enrolment and reporting for your organisation.",
  },
  {
    Icon: CertificateIcon,
    title: "Recognised progress",
    body: "Track completed courses and build a portfolio of skills that demonstrates your progress to employers.",
  },
  {
    Icon: HeadsetIcon,
    title: "1:1 mentoring",
    body: "Book online sessions with a trainer to get personal guidance, code review and career advice.",
  },
  {
    Icon: RocketIcon,
    title: "Career acceleration",
    body: "From first steps to senior roles — clear learning paths help you move forward with confidence.",
  },
];

const STEPS = [
  {
    n: 1,
    title: "Browse the catalog",
    body: "Explore courses by category, or take the short quiz to get a recommendation matched to your goals.",
  },
  {
    n: 2,
    title: "Enrol in a course",
    body: "Buy a course in a couple of clicks with your account — it's instantly added to your learning space.",
  },
  {
    n: 3,
    title: "Learn by doing",
    body: "Work through the material and hands-on labs at your own pace, on any device.",
  },
  {
    n: 4,
    title: "Book a 1:1 session",
    body: "Owners of a course can schedule online mentoring sessions with a trainer for personal guidance.",
  },
];

const TESTIMONIALS = [
  {
    quote:
      "The hands-on labs made the difference. I went from writing my first script to shipping a real project in a few weeks.",
    name: "Andrei M.",
    role: "Junior Developer",
  },
  {
    quote:
      "We enrolled our whole team. The corporate programme was tailored to our stack and the reporting kept managers in the loop.",
    name: "Ioana P.",
    role: "Engineering Manager",
  },
  {
    quote:
      "Booking a 1:1 session with a trainer was the fastest way to unblock myself. Practical, honest, career-focused advice.",
    name: "Robert D.",
    role: "Data Analyst",
  },
];

const STATS = [
  { value: "40+", label: "Courses" },
  { value: "12k+", label: "Learners" },
  { value: "4.8/5", label: "Average rating" },
  { value: "1:1", label: "Mentoring" },
];

const TRAINERS = [
  {
    name: "Alexandru Ionescu",
    role: "Lead Software Engineering Trainer",
    tags: ["Java", "Spring", "Microservices"],
    body: "Over 14 years building backend systems, most recently as a principal engineer designing high-throughput payment platforms for a fintech scale-up. He has led teams of up to 20 developers, owns several open-source Spring libraries and has mentored more than 300 junior engineers into their first professional roles. His courses focus on clean architecture, testing discipline and shipping production-grade code from day one.",
  },
  {
    name: "Maria Popescu",
    role: "Cloud & DevOps Trainer",
    tags: ["AWS", "Kubernetes", "CI/CD"],
    body: "A certified AWS Solutions Architect and CNCF Kubernetes administrator with 11 years spanning infrastructure engineering and platform teams at two unicorn startups. She has migrated dozens of monoliths to containerised, auto-scaling deployments and built the internal training track that upskilled her company's entire engineering org. In class she pairs real cloud environments with hands-on labs so learners deploy, break and fix live systems.",
  },
  {
    name: "David Georgescu",
    role: "AI & Data Science Trainer",
    tags: ["Python", "ML", "LLMs"],
    body: "Holds a PhD in machine learning and spent 9 years as a data scientist and ML lead, taking recommendation and NLP models from notebook to production for e-commerce and healthcare clients. He has published peer-reviewed research, speaks regularly at data conferences and now designs TrainingIT's applied AI curriculum — from the fundamentals of Python and statistics through to fine-tuning and deploying large language models.",
  },
];

export default function Home() {
  const [courses, setCourses] = useState<PublicCourse[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [active, setActive] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // Course ids the logged-in contact has already bought (personal to them).
  const [purchasedIds, setPurchasedIds] = useState<Set<number>>(new Set());
  // Greeting name for a signed-in contact (client portal only). Null when signed
  // out or for any non-USER session, so the greeting stays exclusive to contacts.
  const [greetingName, setGreetingName] = useState<string | null>(null);

  useEffect(() => {
    api.get<Category[]>("/api/public/categories").then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    const session = getSession();
    if (session?.role !== "USER") return;
    const name = [session.firstName, session.lastName].filter(Boolean).join(" ") || session.email;
    setGreetingName(name);
  }, []);

  useEffect(() => {
    const session = getSession();
    if (!session) return;
    api
      .get<MyPurchase[]>(`/api/public/me/purchases?email=${encodeURIComponent(session.email)}`)
      .then((data) => setPurchasedIds(new Set(data.map((p) => p.courseId))))
      .catch(() => {});
  }, []);

  function markPurchased(courseId: number) {
    setPurchasedIds((prev) => {
      const next = new Set(prev);
      next.add(courseId);
      return next;
    });
  }

  useEffect(() => {
    setLoading(true);
    const path = active ? `/api/public/courses?category=${active}` : "/api/public/courses";
    api
      .get<PublicCourse[]>(path)
      .then((data) => {
        setCourses(data);
        setError(null);
        // Track catalog impressions (feeds the admin click-through-rate metric),
        // counting each course only once per browser.
        trackImpressions(data.map((c) => c.id));
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [active]);

  return (
    <div className="-mt-10 -mb-10">
      {/* ============================= HERO ============================= */}
      <section className="full-bleed relative overflow-hidden bg-zinc-950 text-white">
        {/* Brand glow */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -right-40 -top-40 h-[38rem] w-[38rem] rounded-full opacity-30 blur-3xl"
          style={{ background: "radial-gradient(circle, var(--color-brand-500), transparent 60%)" }}
        />
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -bottom-48 -left-40 h-[34rem] w-[34rem] rounded-full opacity-20 blur-3xl"
          style={{ background: "radial-gradient(circle, var(--color-brand-600), transparent 60%)" }}
        />
        <div className="relative mx-auto max-w-6xl px-6 py-24 sm:py-28">
          <div className="ti-rise max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-3 py-1 text-xs font-medium text-brand-200">
              <StarBurstIcon className="h-3.5 w-3.5 text-brand-400" />
              Professional IT training
            </span>
            {greetingName ? (
              <h1 className="mt-6 text-4xl font-semibold leading-[1.05] tracking-tight sm:text-6xl">
                Welcome back, <span className="text-brand-400">{greetingName}</span>.
              </h1>
            ) : (
              <h1 className="mt-6 text-4xl font-semibold leading-[1.05] tracking-tight sm:text-6xl">
                Level up your <span className="text-brand-400">IT career</span>.
              </h1>
            )}
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-zinc-300">
              Instructor-led courses, hands-on labs and 1:1 mentoring in
              programming, AI, data science and cloud — for individuals and
              corporate teams.
            </p>
            <div className="mt-9 flex flex-wrap items-center gap-3">
              <a
                href="#courses"
                className="inline-flex items-center gap-2 rounded-full bg-brand-500 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-brand-500/25 transition hover:bg-brand-600"
              >
                Browse courses
                <ArrowRightIcon className="h-4 w-4" />
              </a>
              <a
                href="#how"
                className="inline-flex items-center gap-2 rounded-full border border-white/20 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
              >
                How it works
              </a>
            </div>
          </div>

          {/* Stats strip */}
          <dl className="ti-rise mt-16 grid grid-cols-2 gap-6 border-t border-white/10 pt-8 sm:grid-cols-4">
            {STATS.map((s) => (
              <div key={s.label}>
                <dt className="text-3xl font-semibold text-white">{s.value}</dt>
                <dd className="mt-1 text-sm text-zinc-400">{s.label}</dd>
              </div>
            ))}
          </dl>
        </div>
      </section>

      {/* ========================== INTRO / ABOUT ======================= */}
      <section className="mx-auto max-w-6xl px-0 py-20">
        <div className="grid items-start gap-12 lg:grid-cols-2">
          <div>
            <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
              What is TrainingIT?
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink sm:text-4xl dark:text-white">
              Practical training that turns skills into careers
            </h2>
          </div>
          <div className="space-y-4 text-[15px] leading-relaxed text-zinc-600 dark:text-zinc-300">
            <p>
              TrainingIT is a training company focused on the technologies that
              power modern software — programming, artificial intelligence, data
              science and cloud. Every course is built around real, hands-on work
              rather than passive lectures.
            </p>
            <p>
              We work with individuals looking to break into tech or advance to
              the next level, and with companies that want to upskill their teams
              through tailored corporate programmes. Learners can also book 1:1
              online sessions with a trainer for personal guidance and code review.
            </p>
          </div>
        </div>
      </section>

      {/* ===================== REVIEWS TICKER (marquee) ================= */}
      <ReviewsTicker />

      {/* ============================ SERVICES ========================== */}
      <section className="full-bleed bg-white py-20 dark:bg-zinc-900/40">
        <div className="mx-auto max-w-6xl px-6">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
              What we offer
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink sm:text-4xl dark:text-white">
              Everything you need to learn and grow
            </h2>
          </div>
          <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {SERVICES.map(({ Icon, title, body }) => (
              <div
                key={title}
                className="group rounded-2xl border border-black/5 bg-canvas p-7 transition hover:border-brand-200 hover:shadow-lg hover:shadow-brand-500/5 dark:border-white/10 dark:bg-zinc-900"
              >
                <span className="inline-flex h-12 w-12 items-center justify-center rounded-xl bg-brand-50 text-brand-600 transition group-hover:bg-brand-500 group-hover:text-white dark:bg-brand-500/10">
                  <Icon className="h-6 w-6" />
                </span>
                <h3 className="mt-5 text-lg font-semibold text-ink dark:text-white">{title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-zinc-600 dark:text-zinc-400">{body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ============================ COURSES =========================== */}
      <section id="courses" className="mx-auto max-w-6xl scroll-mt-20 px-0 py-20">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
            Course catalog
          </p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink sm:text-4xl dark:text-white">
            Find your next course
          </h2>
          <p className="mt-3 text-[15px] leading-relaxed text-zinc-600 dark:text-zinc-400">
            Not sure where to start? Take the quick quiz for a recommendation, or
            browse by category.
          </p>
        </div>

        <div className="mt-8">
          <CourseQuiz />
        </div>

        <div className="mb-6 mt-10 flex flex-wrap gap-2">
          <button
            onClick={() => setActive(null)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition ${
              active === null
                ? "bg-ink text-white dark:bg-white dark:text-ink"
                : "border border-black/10 bg-white text-zinc-600 hover:border-black/20 dark:border-white/10 dark:bg-zinc-900 dark:text-zinc-300"
            }`}
          >
            All
          </button>
          {categories.map((c) => (
            <button
              key={c.name}
              onClick={() => setActive(c.name)}
              className={`rounded-full px-4 py-1.5 text-sm font-medium transition ${
                active === c.name
                  ? "bg-ink text-white dark:bg-white dark:text-ink"
                  : "border border-black/10 bg-white text-zinc-600 hover:border-black/20 dark:border-white/10 dark:bg-zinc-900 dark:text-zinc-300"
              }`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {loading && <p className="text-zinc-500 dark:text-zinc-400">Loading courses…</p>}
        {error && <p className="text-red-600 dark:text-red-400">Could not load courses: {error}</p>}

        <div className="grid items-start gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((c) => (
            <CourseCard
              key={c.id}
              course={c}
              purchased={purchasedIds.has(c.id)}
              onPurchased={markPurchased}
            />
          ))}
        </div>
        {!loading && courses.length === 0 && !error && (
          <p className="text-zinc-500 dark:text-zinc-400">No courses in this category yet.</p>
        )}
      </section>

      {/* ========================= HOW IT WORKS ========================= */}
      <section id="how" className="full-bleed scroll-mt-20 bg-white py-20 dark:bg-zinc-900/40">
        <div className="mx-auto max-w-6xl px-6">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
              How it works
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink sm:text-4xl dark:text-white">
              From first click to your first project
            </h2>
          </div>
          <ol className="mt-12 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
            {STEPS.map((s) => (
              <li key={s.n} className="relative">
                <span className="flex h-12 w-12 items-center justify-center rounded-full bg-ink text-lg font-semibold text-white dark:bg-brand-500">
                  {s.n}
                </span>
                <h3 className="mt-5 text-lg font-semibold text-ink dark:text-white">{s.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-zinc-600 dark:text-zinc-400">{s.body}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      {/* ============================ TRAINERS ========================== */}
      <section className="mx-auto max-w-6xl px-0 py-20">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
            Meet your trainers
          </p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink sm:text-4xl dark:text-white">
            Learn from working engineers
          </h2>
          <p className="mt-3 text-[15px] leading-relaxed text-zinc-600 dark:text-zinc-400">
            Every course is taught by practitioners with years of hands-on
            industry experience — here is the team behind the curriculum.
          </p>
        </div>
        <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {TRAINERS.map((t) => (
            <div
              key={t.name}
              className="flex flex-col rounded-2xl border border-black/5 bg-white p-7 transition hover:border-brand-200 hover:shadow-lg hover:shadow-brand-500/5 dark:border-white/10 dark:bg-zinc-900"
            >
              {/* Anonymous profile picture — SVG silhouette, no photo */}
              <span className="flex h-24 w-24 items-center justify-center self-center rounded-full bg-brand-50 text-brand-500 dark:bg-brand-500/10">
                <UserIcon className="h-12 w-12" />
              </span>
              <h3 className="mt-5 text-center text-lg font-semibold text-ink dark:text-white">
                {t.name}
              </h3>
              <p className="mt-1 text-center text-sm font-medium text-brand-600">{t.role}</p>
              <div className="mt-3 flex flex-wrap justify-center gap-2">
                {t.tags.map((tag) => (
                  <span
                    key={tag}
                    className="rounded-full bg-brand-50 px-3 py-1 text-xs font-medium text-brand-600 dark:bg-brand-500/10"
                  >
                    {tag}
                  </span>
                ))}
              </div>
              <p className="mt-5 text-sm leading-relaxed text-zinc-600 dark:text-zinc-400">
                {t.body}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* ======================= CORPORATE / PRICING ==================== */}
      <section className="mx-auto max-w-6xl px-0 py-20">
        <div className="overflow-hidden rounded-3xl border border-black/5 bg-white dark:border-white/10 dark:bg-zinc-900">
          <div className="grid lg:grid-cols-2">
            <div className="p-10 sm:p-12">
              <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
                For teams
              </p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink dark:text-white">
                Corporate training, built around your stack
              </h2>
              <p className="mt-4 text-[15px] leading-relaxed text-zinc-600 dark:text-zinc-400">
                Enrol your whole team, import employees in bulk and match each
                person to the right learning path. Pricing is tailored to the
                programme and team size — get in touch for an exact quote.
              </p>
              <ul className="mt-6 space-y-3">
                {[
                  "Tailored curriculum for your technologies",
                  "Group enrolment and per-employee tracking",
                  "1:1 mentoring sessions with trainers",
                  "Automatic invoicing for booked sessions",
                ].map((f) => (
                  <li key={f} className="flex items-start gap-3 text-sm text-zinc-700 dark:text-zinc-300">
                    <CheckIcon className="mt-0.5 h-5 w-5 shrink-0 text-brand-500" />
                    {f}
                  </li>
                ))}
              </ul>
            </div>
            <div className="relative flex flex-col justify-center bg-zinc-950 p-10 text-white sm:p-12">
              <div
                aria-hidden="true"
                className="pointer-events-none absolute inset-0 opacity-25 blur-3xl"
                style={{ background: "radial-gradient(circle at 70% 30%, var(--color-brand-500), transparent 55%)" }}
              />
              <div className="relative">
                <p className="text-sm text-zinc-400">Individual courses from</p>
                <p className="mt-1 text-5xl font-semibold">
                  €—<span className="ml-1 align-top text-base font-normal text-zinc-400"></span>
                </p>
                <p className="mt-2 text-sm text-zinc-400">
                  Prices are set per course. Browse the catalog to see the current
                  offering, or contact us for corporate rates.
                </p>
                <a
                  href="#courses"
                  className="mt-8 inline-flex items-center gap-2 rounded-full bg-brand-500 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-600"
                >
                  See courses
                  <ArrowRightIcon className="h-4 w-4" />
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ============================ REVIEWS =========================== */}
      <section className="full-bleed bg-white py-20 dark:bg-zinc-900/40">
        <div className="mx-auto max-w-6xl px-6">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold uppercase tracking-widest text-brand-600">
              Reviews
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink sm:text-4xl dark:text-white">
              What our learners say
            </h2>
          </div>
          <div className="mt-12 grid gap-6 lg:grid-cols-3">
            {TESTIMONIALS.map((t) => (
              <figure
                key={t.name}
                className="flex flex-col rounded-2xl border border-black/5 bg-canvas p-7 dark:border-white/10 dark:bg-zinc-900"
              >
                <QuoteIcon className="h-8 w-8 text-brand-200" />
                <blockquote className="mt-4 flex-1 text-[15px] leading-relaxed text-zinc-700 dark:text-zinc-200">
                  “{t.quote}”
                </blockquote>
                <figcaption className="mt-6 flex items-center gap-3">
                  <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-500 text-sm font-semibold text-white">
                    {t.name.charAt(0)}
                  </span>
                  <span>
                    <span className="block text-sm font-semibold text-ink dark:text-white">{t.name}</span>
                    <span className="block text-xs text-zinc-500 dark:text-zinc-400">{t.role}</span>
                  </span>
                </figcaption>
              </figure>
            ))}
          </div>
        </div>
      </section>

      {/* ========================= FINAL CTA BAND ======================= */}
      <section className="full-bleed relative overflow-hidden bg-zinc-950 text-white">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 opacity-30 blur-3xl"
          style={{ background: "radial-gradient(circle at 50% 120%, var(--color-brand-500), transparent 55%)" }}
        />
        <div className="relative mx-auto max-w-3xl px-6 py-20 text-center">
          <div className="flex justify-center">
            <Logo size={40} tone="light" iconOnly />
          </div>
          <h2 className="mt-6 text-3xl font-semibold tracking-tight sm:text-4xl">
            Ready to level up your IT career?
          </h2>
          <p className="mt-4 text-zinc-300">
            Browse the catalog, pick a course and start learning today.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <a
              href="#courses"
              className="inline-flex items-center gap-2 rounded-full bg-brand-500 px-7 py-3 text-sm font-semibold text-white shadow-lg shadow-brand-500/25 transition hover:bg-brand-600"
            >
              Browse courses
              <ArrowRightIcon className="h-4 w-4" />
            </a>
          </div>
        </div>
      </section>
    </div>
  );
}
