"use client";

import { useState } from "react";
import Link from "next/link";
import { api, ApiError } from "@/lib/api";
import { ArrowRightIcon } from "@/components/icons";
import PasswordInput from "@/components/PasswordInput";

const INPUT_CLASS =
  "mt-1 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-zinc-900 outline-none ring-brand-500 transition focus:border-brand-500 focus:ring-1 dark:border-zinc-700 dark:bg-zinc-800 dark:text-white";

/**
 * Password recovery for a client. There is no email delivery, so instead of a
 * mailed reset link the client proves ownership of the account with two facts
 * already stored on it — the birth date and the phone number. When the backend
 * confirms both against the contacts row for that email, the chosen new password
 * replaces the old one and the client is sent back to the login page.
 */
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [phone, setPhone] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!email.trim()) {
      setError("Enter your email address.");
      return;
    }
    if (!birthDate) {
      setError("Enter your date of birth.");
      return;
    }
    if (!phone.trim()) {
      setError("Enter your phone number.");
      return;
    }
    if (!newPassword) {
      setError("Enter a new password.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("The passwords don't match.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await api.post<{ message: string }>("/api/auth/reset-password", {
        email: email.trim(),
        birthDate,
        phone: phone.trim(),
        newPassword,
        confirmPassword,
      });
      setDone(true);
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "We couldn't reach the server. Please try again.";
      setError(message);
      setLoading(false);
    }
  }

  if (done) {
    return (
      <div className="mx-auto flex max-w-xl flex-col gap-6 py-6">
        <h1 className="text-4xl font-extrabold tracking-tight text-zinc-900 dark:text-white">
          Password reset
        </h1>
        <div className="flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white px-6 py-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <p className="rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400">
            Your password has been changed. You can now sign in with your new password.
          </p>
          <Link
            href="/login"
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-brand-600 px-4 py-2.5 font-medium text-white transition-colors hover:bg-brand-700"
          >
            Go to login
            <ArrowRightIcon />
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 py-6">
      <h1 className="text-4xl font-extrabold tracking-tight text-zinc-900 dark:text-white">
        Forgot password
      </h1>

      <form
        onSubmit={submit}
        className="flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white px-6 py-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
      >
        <div>
          <p className="font-semibold text-zinc-900 dark:text-white">Reset your password</p>
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            Confirm your identity with the details on your account, then choose a
            new password.
          </p>
        </div>

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
          Date of birth
          <input
            type="date"
            value={birthDate}
            onChange={(e) => setBirthDate(e.target.value)}
            className={INPUT_CLASS}
          />
        </label>

        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
          Phone number
          <input
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="07xx xxx xxx"
            className={INPUT_CLASS}
          />
        </label>

        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
          New password
          <PasswordInput
            value={newPassword}
            onChange={setNewPassword}
            placeholder="At least 4 characters"
            inputClassName={INPUT_CLASS}
          />
        </label>

        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
          Confirm new password
          <PasswordInput
            value={confirmPassword}
            onChange={setConfirmPassword}
            placeholder="Repeat the new password"
            inputClassName={INPUT_CLASS}
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
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-brand-600 px-4 py-2.5 font-medium text-white transition-colors hover:bg-brand-700 disabled:opacity-60"
        >
          {loading ? "Checking…" : "Reset password"}
          {!loading && <ArrowRightIcon />}
        </button>

        <p className="text-center text-sm text-zinc-500 dark:text-zinc-400">
          Remembered your password?{" "}
          <Link href="/login" className="font-medium text-brand-600 hover:underline dark:text-brand-400">
            Back to login
          </Link>
        </p>
      </form>
    </div>
  );
}
