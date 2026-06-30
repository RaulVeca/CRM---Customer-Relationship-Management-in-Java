"use client";

import { API_BASE } from "@/lib/api";

const FORMATS = [
  { format: "csv", label: "CSV" },
  { format: "xlsx", label: "Excel (.xlsx)" },
  { format: "pdf", label: "PDF" },
];

/**
 * Dropdown that downloads the given report resource in CSV / Excel / PDF.
 * Uses plain anchor links to the backend, which streams each file with a
 * Content-Disposition header so the browser downloads it directly.
 */
export default function ExportMenu({ resource }: { resource: "contacts" }) {
  return (
    <details className="relative">
      <summary className="flex cursor-pointer list-none items-center gap-1 rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800">
        Export
        <svg viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
          <path
            fillRule="evenodd"
            d="M5.23 7.21a.75.75 0 011.06.02L10 11.06l3.71-3.83a.75.75 0 111.08 1.04l-4.25 4.39a.75.75 0 01-1.08 0L5.21 8.27a.75.75 0 01.02-1.06z"
            clipRule="evenodd"
          />
        </svg>
      </summary>
      <div className="absolute right-0 z-20 mt-1 w-40 overflow-hidden rounded-md border border-slate-200 bg-white py-1 shadow-lg dark:border-slate-700 dark:bg-slate-800">
        {FORMATS.map((f) => (
          <a
            key={f.format}
            href={`${API_BASE}/api/reports/${resource}?format=${f.format}`}
            onClick={(e) => e.currentTarget.closest("details")?.removeAttribute("open")}
            className="block px-4 py-2 text-sm text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-700"
          >
            {f.label}
          </a>
        ))}
      </div>
    </details>
  );
}
