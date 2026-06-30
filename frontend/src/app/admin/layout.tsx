"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { getSession } from "@/lib/auth";

const tabs = [
  { href: "/admin/contacts", label: "Contacts" },
  { href: "/admin/courses", label: "Courses" },
  { href: "/admin/purchases", label: "Purchases" },
  { href: "/admin/analytics", label: "Analytics" },
  { href: "/admin/employees", label: "Employees" },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  // Admin area is reachable only with an ADMIN session — anyone else (signed
  // out, or signed in as a user) is bounced to the login screen.
  const [allowed, setAllowed] = useState(false);

  useEffect(() => {
    const session = getSession();
    if (session?.role === "ADMIN") {
      setAllowed(true);
    } else {
      window.location.assign("/login");
    }
  }, []);

  if (!allowed) {
    return (
      <p className="py-10 text-center text-sm text-slate-500 dark:text-slate-400">
        Se redirecționează către autentificare…
      </p>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex gap-1 border-b border-slate-200 dark:border-slate-800">
        {tabs.map((t) => {
          const activeTab = pathname.startsWith(t.href);
          return (
            <Link
              key={t.href}
              href={t.href}
              className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
                activeTab
                  ? "border-indigo-600 text-indigo-600 dark:border-indigo-400 dark:text-indigo-400"
                  : "border-transparent text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-100"
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
