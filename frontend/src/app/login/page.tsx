"use client";

import { useState } from "react";
import Link from "next/link";
import { api, ApiError } from "@/lib/api";
import { setSession } from "@/lib/auth";
import type { AuthSession } from "@/lib/types";
import { ArrowRightIcon } from "@/components/icons";
import PasswordInput from "@/components/PasswordInput";

const INPUT_CLASS =
  "mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none ring-indigo-500 transition focus:border-indigo-500 focus:ring-1 dark:border-slate-700 dark:bg-slate-800 dark:text-white";

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
    <div className="mx-auto flex max-w-xl flex-col gap-6 py-6">
      <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
        Login
      </h1>

      {/* Brand card */}
      <div className="flex items-center gap-4">
        <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg border border-slate-200 bg-white text-sm font-bold text-indigo-600 dark:border-slate-700 dark:bg-slate-800 dark:text-indigo-400">
          IT
        </div>
        <div>
          <p className="font-semibold text-slate-900 dark:text-white">TrainingIT</p>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Courses and scheduling platform
          </p>
        </div>
      </div>

      <form
        onSubmit={submit}
        className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white px-6 py-6 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <div>
          <p className="font-semibold text-slate-900 dark:text-white">Login</p>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Sign in with email and password
          </p>
        </div>

        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
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

        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
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
            className="text-sm font-medium text-indigo-600 hover:underline dark:text-indigo-400"
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
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 font-medium text-white transition-colors hover:bg-indigo-700 disabled:opacity-60"
        >
          {loading ? "Checking…" : "Continue"}
          {!loading && <ArrowRightIcon />}
        </button>

        <p className="text-center text-sm text-slate-500 dark:text-slate-400">
          Don&apos;t have an account?{" "}
          <Link href="/register" className="font-medium text-indigo-600 hover:underline dark:text-indigo-400">
            Sign up
          </Link>
        </p>
      </form>
    </div>
  );
}
