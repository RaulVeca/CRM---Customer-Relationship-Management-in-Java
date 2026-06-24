"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { CourseRecommendation, Employee, Option, PublicCompany } from "@/lib/types";

export default function EmployeesPage() {
  const [companies, setCompanies] = useState<PublicCompany[]>([]);
  const [companyId, setCompanyId] = useState<number | null>(null);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [profileAreas, setProfileAreas] = useState<Option[]>([]);
  const [levels, setLevels] = useState<Option[]>([]);
  const [error, setError] = useState<string | null>(null);

  // AI recommendations
  const [aiEnabled, setAiEnabled] = useState(false);
  const [recs, setRecs] = useState<CourseRecommendation[] | null>(null);
  const [recsLoading, setRecsLoading] = useState(false);

  // new employee form
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    jobTitle: "",
    workProfile: "",
    experienceLevel: "",
    interestProfiles: [] as string[],
  });

  useEffect(() => {
    api.get<PublicCompany[]>("/api/public/companies").then((c) => {
      setCompanies(c);
      if (c.length) setCompanyId(c[0].id);
    });
    api.get<Option[]>("/api/meta/profile-areas").then(setProfileAreas);
    api.get<Option[]>("/api/meta/experience-levels").then(setLevels);
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
        jobTitle: form.jobTitle || null,
        workProfile: form.workProfile || null,
        experienceLevel: form.experienceLevel || null,
        interestProfiles: form.interestProfiles,
      });
      setForm({ firstName: "", lastName: "", jobTitle: "", workProfile: "", experienceLevel: "", interestProfiles: [] });
      setError(null);
      loadEmployees(companyId);
    } catch (err) {
      setError((err as Error).message);
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
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm"
        >
          {companies.map((c) => (
            <option key={c.id} value={c.id}>{c.companyName}</option>
          ))}
        </select>
        {aiEnabled && (
          <button
            onClick={loadRecommendations}
            disabled={recsLoading}
            className="ml-auto rounded-md bg-violet-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-violet-700 disabled:opacity-50"
          >
            {recsLoading ? "Thinking…" : "✨ AI course recommendations"}
          </button>
        )}
      </div>

      {error && <p className="text-red-600">{error}</p>}

      {recs && (
        <div className="rounded-xl border border-violet-200 bg-violet-50 p-5">
          <h3 className="mb-3 font-semibold text-violet-900">AI-recommended courses for this company</h3>
          {recs.length === 0 ? (
            <p className="text-sm text-slate-500">No recommendations (try adding employee profiles first).</p>
          ) : (
            <ul className="space-y-2">
              {recs.map((r) => (
                <li key={r.courseId} className="flex items-start gap-3 rounded-lg bg-white p-3 shadow-sm">
                  <span className="mt-0.5 rounded-full bg-violet-600 px-2 py-0.5 text-xs font-bold text-white">
                    {r.matchScore}
                  </span>
                  <div>
                    <p className="font-medium">{r.courseName}</p>
                    <p className="text-sm text-slate-500">{r.reason}</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Role</th>
                <th className="px-4 py-3">Works in</th>
                <th className="px-4 py-3">Interested in</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {employees.map((emp) => (
                <tr key={emp.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium">{emp.fullName || `${emp.firstName ?? ""} ${emp.lastName ?? ""}`}</td>
                  <td className="px-4 py-3 text-slate-500">{emp.jobTitle ?? "—"}</td>
                  <td className="px-4 py-3">
                    <span className="rounded bg-slate-100 px-2 py-0.5 text-xs">{label(profileAreas, emp.workProfile)}</span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-1">
                      {emp.interestProfiles.map((ip) => (
                        <span key={ip} className="rounded bg-indigo-50 px-2 py-0.5 text-xs text-indigo-700">
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
          {employees.length === 0 && <p className="p-4 text-slate-500">No employees yet for this company.</p>}
        </div>

        <form onSubmit={submit} className="space-y-3 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="font-semibold">Add employee</h3>
          <div className="grid grid-cols-2 gap-2">
            <input className="rounded-md border border-slate-300 px-2 py-1.5 text-sm" placeholder="First name"
              value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
            <input className="rounded-md border border-slate-300 px-2 py-1.5 text-sm" placeholder="Last name"
              value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          </div>
          <input className="w-full rounded-md border border-slate-300 px-2 py-1.5 text-sm" placeholder="Job title"
            value={form.jobTitle} onChange={(e) => setForm({ ...form, jobTitle: e.target.value })} />
          <select className="w-full rounded-md border border-slate-300 px-2 py-1.5 text-sm"
            value={form.workProfile} onChange={(e) => setForm({ ...form, workProfile: e.target.value })}>
            <option value="">Work profile…</option>
            {profileAreas.map((p) => <option key={p.name} value={p.name}>{p.label}</option>)}
          </select>
          <select className="w-full rounded-md border border-slate-300 px-2 py-1.5 text-sm"
            value={form.experienceLevel} onChange={(e) => setForm({ ...form, experienceLevel: e.target.value })}>
            <option value="">Experience level…</option>
            {levels.map((p) => <option key={p.name} value={p.name}>{p.label}</option>)}
          </select>
          <div>
            <p className="mb-1 text-xs font-medium text-slate-500">Interested in</p>
            <div className="flex flex-wrap gap-1">
              {profileAreas.map((p) => (
                <button type="button" key={p.name} onClick={() => toggleInterest(p.name)}
                  className={`rounded-full px-2 py-1 text-xs ${form.interestProfiles.includes(p.name) ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-600"}`}>
                  {p.label}
                </button>
              ))}
            </div>
          </div>
          <button className="w-full rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700">
            Add employee
          </button>
        </form>
      </div>
    </div>
  );
}
