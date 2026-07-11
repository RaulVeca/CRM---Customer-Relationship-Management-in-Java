"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import { SparkleIcon } from "@/components/icons";
import type {
  CourseRecommendation,
  Employee,
  EmployeeImportResult,
  Option,
  PublicCompany,
} from "@/lib/types";

export default function EmployeesPage() {
  const [companies, setCompanies] = useState<PublicCompany[]>([]);
  const [companyId, setCompanyId] = useState<number | null>(null);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [profileAreas, setProfileAreas] = useState<Option[]>([]);
  const [error, setError] = useState<string | null>(null);

  // AI recommendations
  const [aiEnabled, setAiEnabled] = useState(false);
  const [recs, setRecs] = useState<CourseRecommendation[] | null>(null);
  const [recsLoading, setRecsLoading] = useState(false);

  // Excel import
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<EmployeeImportResult | null>(null);

  // new employee form
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    jobTitle: "",
    workProfile: "",
    interestProfiles: [] as string[],
  });

  useEffect(() => {
    api.get<PublicCompany[]>("/api/public/companies").then((c) => {
      setCompanies(c);
      if (c.length) setCompanyId(c[0].id);
    });
    api.get<Option[]>("/api/meta/profile-areas").then(setProfileAreas);
    api.get<{ enabled: boolean }>("/api/ai/status").then((s) => setAiEnabled(s.enabled)).catch(() => {});
  }, []);

  useEffect(() => {
    setRecs(null);
  }, [companyId]);

  async function loadRecommendations() {
    if (companyId == null) return;
    setRecsLoading(true);
    setRecs(null);
    try {
      const data = await api.get<CourseRecommendation[]>(`/api/ai/recommendations/company/${companyId}`);
      setRecs(data);
      setError(null);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setRecsLoading(false);
    }
  }

  function loadEmployees(id: number) {
    api.get<Employee[]>(`/api/employees/by-company/${id}`).then(setEmployees).catch((e) => setError(e.message));
  }

  useEffect(() => {
    if (companyId != null) loadEmployees(companyId);
  }, [companyId]);

  function toggleInterest(name: string) {
    setForm((f) => ({
      ...f,
      interestProfiles: f.interestProfiles.includes(name)
        ? f.interestProfiles.filter((x) => x !== name)
        : [...f.interestProfiles, name],
    }));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (companyId == null) return;
    try {
      await api.post<Employee>("/api/employees", {
        companyId,
        firstName: form.firstName || null,
        lastName: form.lastName || null,
        email: form.email || null,
        jobTitle: form.jobTitle || null,
        workProfile: form.workProfile || null,
        interestProfiles: form.interestProfiles,
      });
      setForm({ firstName: "", lastName: "", email: "", jobTitle: "", workProfile: "", interestProfiles: [] });
      setError(null);
      loadEmployees(companyId);
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function onImportFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    // Reset the input so selecting the same file again still fires onChange.
    e.target.value = "";
    if (!file || companyId == null) return;
    setImporting(true);
    setImportResult(null);
    setError(null);
    try {
      const fd = new FormData();
      fd.append("file", file);
      const res = await api.upload<EmployeeImportResult>(
        `/api/employees/import?companyId=${companyId}`,
        fd,
      );
      setImportResult(res);
      loadEmployees(companyId);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setImporting(false);
    }
  }

  async function remove(id: number) {
    await api.del(`/api/employees/${id}`);
    if (companyId != null) loadEmployees(companyId);
  }

  const label = (areas: Option[], n: string | null) => areas.find((a) => a.name === n)?.label ?? n ?? "—";

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <h2 className="text-lg font-semibold">Employees</h2>
        <select
          value={companyId ?? ""}
          onChange={(e) => setCompanyId(Number(e.target.value))}
          className="rounded-md border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 px-3 py-1.5 text-sm"
        >
          {companies.map((c) => (
            <option key={c.id} value={c.id}>{c.companyName}</option>
          ))}
        </select>

        <input
          ref={fileInputRef}
          type="file"
          accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          onChange={onImportFile}
          className="hidden"
        />
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={importing || companyId == null}
          className="ml-auto rounded-md border border-zinc-300 bg-white px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:opacity-50 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 dark:hover:bg-zinc-700"
        >
          {importing ? "Importing…" : "Import from Excel"}
        </button>

        {aiEnabled && (
          <button
            onClick={loadRecommendations}
            disabled={recsLoading}
            className="rounded-md bg-brand-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-50"
          >
            {recsLoading ? (
              "Thinking…"
            ) : (
              <span className="inline-flex items-center gap-1.5">
                <SparkleIcon className="h-4 w-4" />
                AI course recommendations
              </span>
            )}
          </button>
        )}
      </div>

      {error && <p className="text-red-600">{error}</p>}

      {importResult && (
        <div className="rounded-xl border border-zinc-200 bg-white p-4 text-sm dark:border-zinc-800 dark:bg-zinc-900">
          <p className="font-medium text-zinc-800 dark:text-zinc-100">
            Import finished: {importResult.imported} added
            {importResult.skipped > 0 && `, ${importResult.skipped} skipped`}.
          </p>
          {importResult.errors.length > 0 && (
            <ul className="mt-2 list-disc space-y-0.5 pl-5 text-zinc-500 dark:text-zinc-400">
              {importResult.errors.map((er, i) => (
                <li key={i}>Row {er.row}: {er.message}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      {recs && (
        <div className="rounded-xl border border-brand-200 bg-brand-50 p-5">
          <h3 className="mb-3 font-semibold text-brand-900">AI-recommended courses for this company</h3>
          {recs.length === 0 ? (
            <p className="text-sm text-zinc-500">No recommendations (try adding employee profiles first).</p>
          ) : (
            <ul className="space-y-2">
              {recs.map((r) => (
                <li key={r.courseId} className="flex items-start gap-3 rounded-lg bg-white p-3 shadow-sm dark:bg-zinc-800">
                  <span className="mt-0.5 rounded-full bg-brand-600 px-2 py-0.5 text-xs font-bold text-white">
                    {r.matchScore}
                  </span>
                  <div>
                    <p className="font-medium">{r.courseName}</p>
                    <p className="text-sm text-zinc-500">{r.reason}</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 overflow-hidden rounded-xl border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900 shadow-sm">
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 text-left text-xs uppercase text-zinc-500 dark:bg-zinc-800/50 dark:text-zinc-400">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Role</th>
                <th className="px-4 py-3">Works in</th>
                <th className="px-4 py-3">Interested in</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
              {employees.map((emp) => (
                <tr key={emp.id} className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50">
                  <td className="px-4 py-3 font-medium">{emp.fullName || `${emp.firstName ?? ""} ${emp.lastName ?? ""}`}</td>
                  <td className="px-4 py-3 text-zinc-500">{emp.email ?? "—"}</td>
                  <td className="px-4 py-3 text-zinc-500">{emp.jobTitle ?? "—"}</td>
                  <td className="px-4 py-3">
                    <span className="rounded bg-zinc-100 px-2 py-0.5 text-xs dark:bg-zinc-700 dark:text-zinc-200">{label(profileAreas, emp.workProfile)}</span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-1">
                      {emp.interestProfiles.map((ip) => (
                        <span key={ip} className="rounded bg-brand-50 px-2 py-0.5 text-xs text-brand-700">
                          {label(profileAreas, ip)}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button onClick={() => remove(emp.id)} className="text-xs text-red-500 hover:underline">Remove</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {employees.length === 0 && <p className="p-4 text-zinc-500">No employees yet for this company.</p>}
        </div>

        <form onSubmit={submit} className="space-y-3 rounded-xl border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900 p-5 shadow-sm">
          <h3 className="font-semibold">Add employee</h3>
          <div className="grid grid-cols-2 gap-2">
            <input className="rounded-md border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 px-2 py-1.5 text-sm" placeholder="First name"
              value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
            <input className="rounded-md border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 px-2 py-1.5 text-sm" placeholder="Last name"
              value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          </div>
          <input type="email" className="w-full rounded-md border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 px-2 py-1.5 text-sm" placeholder="Email"
            value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <input className="w-full rounded-md border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 px-2 py-1.5 text-sm" placeholder="Job title"
            value={form.jobTitle} onChange={(e) => setForm({ ...form, jobTitle: e.target.value })} />
          <select className="w-full rounded-md border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 px-2 py-1.5 text-sm"
            value={form.workProfile} onChange={(e) => setForm({ ...form, workProfile: e.target.value })}>
            <option value="">Work profile…</option>
            {profileAreas.map((p) => <option key={p.name} value={p.name}>{p.label}</option>)}
          </select>
          <div>
            <p className="mb-1 text-xs font-medium text-zinc-500">Interested in</p>
            <div className="flex flex-wrap gap-1">
              {profileAreas.map((p) => (
                <button type="button" key={p.name} onClick={() => toggleInterest(p.name)}
                  className={`rounded-full px-2 py-1 text-xs ${form.interestProfiles.includes(p.name) ? "bg-brand-600 text-white" : "bg-zinc-100 text-zinc-600"}`}>
                  {p.label}
                </button>
              ))}
            </div>
          </div>
          <button className="w-full rounded-md bg-brand-600 px-3 py-2 text-sm font-medium text-white hover:bg-brand-700">
            Add employee
          </button>
        </form>
      </div>
    </div>
  );
}
