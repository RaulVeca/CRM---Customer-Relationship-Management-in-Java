"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, ApiError } from "@/lib/api";
import { setSession } from "@/lib/auth";
import type { AuthSession, Option, RegisterRequest } from "@/lib/types";
import { ArrowRightIcon } from "@/components/icons";
import PasswordInput from "@/components/PasswordInput";

/**
 * Self-service registration. A visitor becomes a client by filling in every
 * "card" of an INDIVIDUAL contact; on success the backend adds a single row to
 * the contacts table and returns a USER session, so the new account is logged
 * in straight away — exactly like the sign-in flow. Only the INDIVIDUAL contact
 * type can register here.
 */

const INPUT_CLASS =
  "mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none ring-indigo-500 transition focus:border-indigo-500 focus:ring-1 dark:border-slate-700 dark:bg-slate-800 dark:text-white";

const LABEL_CLASS = "block text-sm font-medium text-slate-700 dark:text-slate-300";

type FormState = Omit<RegisterRequest, "gdprConsent" | "marketingConsent">;

const EMPTY: FormState = {
  firstName: "",
  lastName: "",
  birthDate: "",
  email: "",
  phone: "",
  addressStreet: "",
  addressCity: "",
  addressCounty: "",
  addressPostalCode: "",
  experienceLevel: "",
  learningGoal: "",
  leadSource: "",
  password: "",
  confirmPassword: "",
};

/** A titled card grouping related fields, matching the login page look. */
function Card({ title, subtitle, children }: { title: string; subtitle?: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white px-6 py-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div>
        <p className="font-semibold text-slate-900 dark:text-white">{title}</p>
        {subtitle && <p className="text-sm text-slate-500 dark:text-slate-400">{subtitle}</p>}
      </div>
      {children}
    </div>
  );
}

export default function RegisterPage() {
  const [form, setForm] = useState<FormState>(EMPTY);
  const [gdprConsent, setGdprConsent] = useState(false);
  const [marketingConsent, setMarketingConsent] = useState(false);
  const [experienceLevels, setExperienceLevels] = useState<Option[]>([]);
  const [leadSources, setLeadSources] = useState<Option[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get<Option[]>("/api/meta/experience-levels").then(setExperienceLevels).catch(() => {});
    api.get<Option[]>("/api/meta/lead-sources").then(setLeadSources).catch(() => {});
  }, []);

  function set<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();

    // All cards are required.
    const missing = (Object.keys(EMPTY) as (keyof FormState)[]).some((k) => !form[k].trim());
    if (missing) {
      setError("Fill in all fields — every card is required.");
      return;
    }
    if (form.password.length < 4) {
      setError("The password must be at least 4 characters.");
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError("The passwords don't match.");
      return;
    }
    if (!gdprConsent) {
      setError("You must accept the data processing (GDPR) to register.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const payload: RegisterRequest = {
        ...form,
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        gdprConsent,
        marketingConsent,
      };
      const session = await api.post<AuthSession>("/api/auth/register", payload);
      setSession(session);
      window.location.assign("/");
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
      <div>
        <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Register
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Create an account by filling in all the cards below. Registration is
          available for individuals.
        </p>
      </div>

      <form onSubmit={submit} className="flex flex-col gap-6">
        <Card title="Personal details">
          <label className={LABEL_CLASS}>
            First name
            <input
              value={form.firstName}
              onChange={(e) => set("firstName", e.target.value)}
              placeholder="John"
              className={INPUT_CLASS}
            />
          </label>
          <label className={LABEL_CLASS}>
            Last name
            <input
              value={form.lastName}
              onChange={(e) => set("lastName", e.target.value)}
              placeholder="Doe"
              className={INPUT_CLASS}
            />
          </label>
          <label className={LABEL_CLASS}>
            Date of birth
            <input
              type="date"
              value={form.birthDate}
              max={new Date().toISOString().slice(0, 10)}
              onChange={(e) => set("birthDate", e.target.value)}
              className={INPUT_CLASS}
            />
          </label>
        </Card>

        <Card title="Contact details">
          <label className={LABEL_CLASS}>
            Email address
            <input
              type="email"
              value={form.email}
              onChange={(e) => set("email", e.target.value)}
              placeholder="name@example.com"
              className={INPUT_CLASS}
            />
          </label>
          <label className={LABEL_CLASS}>
            Phone
            <input
              value={form.phone}
              onChange={(e) => set("phone", e.target.value)}
              placeholder="07xx xxx xxx"
              className={INPUT_CLASS}
            />
          </label>
        </Card>

        <Card title="Address">
          <label className={LABEL_CLASS}>
            Street
            <input
              value={form.addressStreet}
              onChange={(e) => set("addressStreet", e.target.value)}
              placeholder="123 Example Street"
              className={INPUT_CLASS}
            />
          </label>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className={LABEL_CLASS}>
              City
              <input
                value={form.addressCity}
                onChange={(e) => set("addressCity", e.target.value)}
                placeholder="City"
                className={INPUT_CLASS}
              />
            </label>
            <label className={LABEL_CLASS}>
              County
              <input
                value={form.addressCounty}
                onChange={(e) => set("addressCounty", e.target.value)}
                placeholder="County"
                className={INPUT_CLASS}
              />
            </label>
          </div>
          <label className={LABEL_CLASS}>
            Postal code
            <input
              value={form.addressPostalCode}
              onChange={(e) => set("addressPostalCode", e.target.value)}
              placeholder="300000"
              className={INPUT_CLASS}
            />
          </label>
        </Card>

        <Card title="Learning profile">
          <label className={LABEL_CLASS}>
            Experience level
            <select
              value={form.experienceLevel}
              onChange={(e) => set("experienceLevel", e.target.value)}
              className={INPUT_CLASS}
            >
              <option value="">Choose…</option>
              {experienceLevels.map((o) => (
                <option key={o.name} value={o.name}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label className={LABEL_CLASS}>
            Learning goal
            <input
              value={form.learningGoal}
              onChange={(e) => set("learningGoal", e.target.value)}
              placeholder="Career change into IT"
              className={INPUT_CLASS}
            />
          </label>
          <label className={LABEL_CLASS}>
            How did you hear about us?
            <select
              value={form.leadSource}
              onChange={(e) => set("leadSource", e.target.value)}
              className={INPUT_CLASS}
            >
              <option value="">Choose…</option>
              {leadSources.map((o) => (
                <option key={o.name} value={o.name}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
        </Card>

        <Card title="Password" subtitle="Choose a password for your account">
          <label className={LABEL_CLASS}>
            Password
            <PasswordInput
              value={form.password}
              onChange={(v) => set("password", v)}
              placeholder="At least 4 characters"
              inputClassName={INPUT_CLASS}
            />
          </label>
          <label className={LABEL_CLASS}>
            Confirm password
            <PasswordInput
              value={form.confirmPassword}
              onChange={(v) => set("confirmPassword", v)}
              placeholder="Repeat the password"
              inputClassName={INPUT_CLASS}
            />
          </label>
        </Card>

        <Card title="Consent" subtitle="Data processing under GDPR">
          <label className="flex items-start gap-3 text-sm text-slate-700 dark:text-slate-300">
            <input
              type="checkbox"
              checked={gdprConsent}
              onChange={(e) => setGdprConsent(e.target.checked)}
              className="mt-0.5 h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
            />
            <span>
              I agree to the processing of my personal data (required).
            </span>
          </label>
          <label className="flex items-start gap-3 text-sm text-slate-700 dark:text-slate-300">
            <input
              type="checkbox"
              checked={marketingConsent}
              onChange={(e) => setMarketingConsent(e.target.checked)}
              className="mt-0.5 h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
            />
            <span>I want to receive marketing communications (optional).</span>
          </label>
        </Card>

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
          {loading ? "Creating account…" : "Create account"}
          {!loading && <ArrowRightIcon />}
        </button>

        <p className="text-center text-sm text-slate-500 dark:text-slate-400">
          Already have an account?{" "}
          <Link href="/login" className="font-medium text-indigo-600 hover:underline dark:text-indigo-400">
            Log in
          </Link>
        </p>
      </form>
    </div>
  );
}
