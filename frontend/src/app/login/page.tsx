"use client";

import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import { setSession } from "@/lib/auth";
import type { AuthRole, AuthSession } from "@/lib/types";
import { UserIcon, UsersIcon, ArrowRightIcon, ArrowLeftIcon } from "@/components/icons";

type Choice = {
  role: AuthRole;
  title: string;
  subtitle: string;
  icon: typeof UserIcon;
  endpoint: string;
  redirect: string;
};

// The two sign-in options shown on the landing screen. Each maps to its own
// backend endpoint and post-login destination — the roles never cross over.
const CHOICES: Choice[] = [
  {
    role: "USER",
    title: "Cont utilizator",
    subtitle: "Autentifică-te pentru a-ți gestiona programările",
    icon: UserIcon,
    endpoint: "/api/auth/login/user",
    redirect: "/",
  },
  {
    role: "ADMIN",
    title: "Cont admin",
    subtitle: "Acces pentru traineri și administratori",
    icon: UsersIcon,
    endpoint: "/api/auth/login/admin",
    redirect: "/admin",
  },
];

export default function LoginPage() {
  const [choice, setChoice] = useState<Choice | null>(null);
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  function select(c: Choice) {
    setChoice(c);
    setEmail("");
    setError(null);
  }

  function back() {
    setChoice(null);
    setError(null);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!choice) return;
    const value = email.trim();
    if (!value) {
      setError("Introdu adresa de email.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const session = await api.post<AuthSession>(choice.endpoint, { email: value });
      setSession(session);
      window.location.assign(choice.redirect);
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "Nu am putut contacta serverul. Încearcă din nou.";
      setError(message);
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 py-6">
      <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
        Selectează o opțiune
      </h1>

      {/* Brand card */}
      <div className="flex items-center gap-4">
        <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg border border-slate-200 bg-white text-sm font-bold text-indigo-600 dark:border-slate-700 dark:bg-slate-800 dark:text-indigo-400">
          IT
        </div>
        <div>
          <p className="font-semibold text-slate-900 dark:text-white">TrainingIT</p>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Platformă de cursuri și programări
          </p>
        </div>
      </div>

      {!choice ? (
        // Step 1 — pick a role
        <div className="flex flex-col gap-4">
          {CHOICES.map((c) => {
            const Icon = c.icon;
            return (
              <button
                key={c.role}
                type="button"
                onClick={() => select(c)}
                className="group flex items-center justify-between rounded-2xl border border-slate-200 bg-white px-6 py-5 text-left shadow-sm transition-colors hover:border-indigo-300 hover:bg-indigo-50/40 dark:border-slate-800 dark:bg-slate-900 dark:hover:border-indigo-700 dark:hover:bg-slate-800"
              >
                <span>
                  <span className="block font-semibold text-slate-900 dark:text-white">
                    {c.title}
                  </span>
                  <span className="mt-0.5 block text-sm text-slate-500 dark:text-slate-400">
                    {c.subtitle}
                  </span>
                </span>
                <Icon className="h-6 w-6 shrink-0 text-slate-400 transition-colors group-hover:text-indigo-600 dark:group-hover:text-indigo-400" />
              </button>
            );
          })}
        </div>
      ) : (
        // Step 2 — email sign-in for the chosen role
        <form
          onSubmit={submit}
          className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white px-6 py-6 shadow-sm dark:border-slate-800 dark:bg-slate-900"
        >
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={back}
              aria-label="Înapoi"
              className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800"
            >
              <ArrowLeftIcon />
            </button>
            <div>
              <p className="font-semibold text-slate-900 dark:text-white">{choice.title}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                Autentificare pe bază de email
              </p>
            </div>
          </div>

          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            Adresă de email
            <input
              type="email"
              autoFocus
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="nume@exemplu.ro"
              className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none ring-indigo-500 transition focus:border-indigo-500 focus:ring-1 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
            />
          </label>

          {error && (
            <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600 dark:bg-red-950/40 dark:text-red-400">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 font-medium text-white transition-colors hover:bg-indigo-700 disabled:opacity-60"
          >
            {loading ? "Se verifică…" : "Continuă"}
            {!loading && <ArrowRightIcon />}
          </button>
        </form>
      )}
    </div>
  );
}
