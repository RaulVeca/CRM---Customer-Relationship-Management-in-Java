"use client";

import { useState } from "react";
import Link from "next/link";
import { api, ApiError } from "@/lib/api";
import { setSession } from "@/lib/auth";
import type { AuthSession } from "@/lib/types";
import { ArrowRightIcon } from "@/components/icons";
import Logo from "@/components/Logo";
import PasswordInput from "@/components/PasswordInput";

const INPUT_CLASS =
  "mt-1 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-ink outline-none transition focus:border-brand-500 focus:ring-1 focus:ring-brand-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-white";

/**
 * Email + password sign-in. There is one "Login" card: the entered email is
 * resolved by the backend against the contacts, admins and employees tables, the
 * password is verified against that account, and the role it comes back with
 * decides the destination — USER opens the client site, ADMIN opens the admin
 * portal. The roles never cross over: a client email can only ever yield a USER
 * session and an admin email an ADMIN session.
 */
export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const value = email.trim();
    if (!value) {
      setError("Enter your email address.");
      return;
    }
    if (!password) {
      setError("Enter your password.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const session = await api.post<AuthSession>("/api/auth/login", { email: value, password });
      setSession(session);
      window.location.assign(session.role === "ADMIN" ? "/admin" : "/");
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "We couldn't reach the server. Please try again.";
      setError(message);
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 py-6">
      <div>
        <Logo size={34} tone="dark" className="mb-6 dark:hidden" />
        <Logo size={34} tone="light" className="mb-6 hidden dark:inline-flex" />
        <h1 className="text-4xl font-semibold tracking-tight text-ink dark:text-white">
          Welcome back
        </h1>
        <p className="mt-2 text-zinc-500 dark:text-zinc-400">
          Sign in to continue to your learning space.
        </p>
      </div>

      <form
        onSubmit={submit}
        className="flex flex-col gap-4 rounded-2xl border border-black/5 bg-white px-6 py-6 shadow-sm dark:border-white/10 dark:bg-zinc-900"
      >
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
          Email address
          <input
            type="email"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="nume@exemplu.ro"
            className={INPUT_CLASS}
          />
        </label>

        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
          Password
          <PasswordInput
            value={password}
            onChange={setPassword}
            placeholder="Your password"
            inputClassName={INPUT_CLASS}
          />
        </label>

        <div className="-mt-2 text-right">
          <Link
            href="/forgot-password"
            className="text-sm font-medium text-brand-600 hover:underline dark:text-brand-400"
          >
            Forgot password?
          </Link>
        </div>

        {error && (
          <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600 dark:bg-red-950/40 dark:text-red-400">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center justify-center gap-2 rounded-full bg-brand-500 px-4 py-2.5 font-semibold text-white transition hover:bg-brand-600 disabled:opacity-60"
        >
          {loading ? "Checking…" : "Continue"}
          {!loading && <ArrowRightIcon />}
        </button>

        <p className="text-center text-sm text-zinc-500 dark:text-zinc-400">
          Don&apos;t have an account?{" "}
          <Link href="/register" className="font-medium text-brand-600 hover:underline dark:text-brand-400">
            Sign up
          </Link>
        </p>
      </form>
    </div>
  );
}
