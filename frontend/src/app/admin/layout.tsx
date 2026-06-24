"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const tabs = [
  { href: "/admin", label: "Dashboard" },
  { href: "/admin/contacts", label: "Contacts" },
  { href: "/admin/courses", label: "Courses" },
  { href: "/admin/pipeline", label: "Pipeline" },
  { href: "/admin/employees", label: "Employees" },
  { href: "/admin/auctions", label: "Auctions" },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  return (
    <div className="space-y-6">
      <div className="flex gap-1 border-b border-slate-200">
        {tabs.map((t) => {
          const activeTab = t.href === "/admin" ? pathname === "/admin" : pathname.startsWith(t.href);
          return (
            <Link
              key={t.href}
              href={t.href}
              className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
                activeTab
                  ? "border-indigo-600 text-indigo-600"
                  : "border-transparent text-slate-500 hover:text-slate-800"
              }`}
            >
              {t.label}
            </Link>
          );
        })}
      </div>
      {children}
    </div>
  );
}
