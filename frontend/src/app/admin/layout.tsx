"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { getSession } from "@/lib/auth";

const tabs = [
  { href: "/admin/contacts", label: "Contacts" },
  { href: "/admin/courses", label: "Courses" },
  { href: "/admin/purchases", label: "Purchases" },
  { href: "/admin/invoices", label: "Invoices" },
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
      <p className="py-10 text-center text-sm text-zinc-500 dark:text-zinc-400">
        Redirecting to sign in…
      </p>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex gap-1 border-b border-zinc-200 dark:border-zinc-800">
        {tabs.map((t) => {
          const activeTab = pathname.startsWith(t.href);
          return (
            <Link
              key={t.href}
              href={t.href}
              className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
                activeTab
                  ? "border-brand-600 text-brand-600 dark:border-brand-400 dark:text-brand-400"
                  : "border-transparent text-zinc-500 hover:text-zinc-800 dark:text-zinc-400 dark:hover:text-zinc-100"
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
