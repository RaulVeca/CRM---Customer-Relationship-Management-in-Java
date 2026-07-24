"use client";

import { useEffect, useState, type ReactElement, type SVGProps } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { getSession } from "@/lib/auth";
import ThemeToggle from "@/components/ThemeToggle";
import AuthNav from "@/components/AuthNav";
import LanguageSelector from "@/components/LanguageSelector";
import Logo from "@/components/Logo";

/**
 * Line-style SVG glyphs for the sidebar — one per admin section. Crisp vector
 * marks (not font emoji) so they stay sharp at any size and inherit the sidebar
 * text colour via {@code currentColor}.
 */
type Icon = (props: SVGProps<SVGSVGElement>) => ReactElement;

const svg = (paths: React.ReactNode): Icon =>
  function Glyph(props: SVGProps<SVGSVGElement>) {
    return (
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth={1.75}
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
        {...props}
      >
        {paths}
      </svg>
    );
  };

const Icons: Record<string, Icon> = {
  contacts: svg(
    <>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </>,
  ),
  courses: svg(
    <>
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z" />
    </>,
  ),
  purchases: svg(
    <>
      <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
      <rect x="9" y="3" width="6" height="4" rx="1" />
      <path d="M9 12h6M9 16h4" />
    </>,
  ),
  invoices: svg(
    <>
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <path d="M14 2v6h6" />
      <path d="M8 13h8M8 17h5" />
    </>,
  ),
  analytics: svg(
    <>
      <path d="M3 3v18h18" />
      <path d="M7 15l3-4 3 3 4-6" />
    </>,
  ),
  employees: svg(
    <>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <circle cx="9" cy="10" r="2" />
      <path d="M6 16a3 3 0 0 1 6 0" />
      <path d="M15 9h4M15 13h4" />
    </>,
  ),
  issues: svg(
    <>
      <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z" />
      <path d="M12 9v4M12 17h.01" />
    </>,
  ),
};

const tabs = [
  { href: "/admin/contacts", label: "Contacts", icon: "contacts" },
  { href: "/admin/courses", label: "Courses", icon: "courses" },
  { href: "/admin/purchases", label: "Enrollments", icon: "purchases" },
  { href: "/admin/invoices", label: "Invoices", icon: "invoices" },
  { href: "/admin/analytics", label: "Analytics", icon: "analytics" },
  { href: "/admin/employees", label: "Employees", icon: "employees" },
  { href: "/admin/issues", label: "Issues", icon: "issues" },
] as const;

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  // Admin area is reachable only with an ADMIN session — anyone else (signed
  // out, or signed in as a user) is bounced to the login screen.
  const [allowed, setAllowed] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

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
    <div className="flex min-h-screen w-full flex-col bg-slate-50 text-slate-900 dark:bg-zinc-950 dark:text-zinc-100">
      {/* Top bar — brand, console label and the account / preference controls */}
      <header className="flex h-16 shrink-0 items-center gap-3 border-b border-slate-200 bg-white px-5 dark:border-zinc-800 dark:bg-zinc-900">
        <Link href="/admin/contacts" className="flex items-center" aria-label="TrainingIT admin home">
          <Logo size={28} />
        </Link>
        <span className="hidden h-6 w-px bg-slate-200 dark:bg-zinc-700 sm:block" />
        <span className="hidden text-sm font-semibold text-slate-500 dark:text-zinc-400 sm:block">
          Admin Console
        </span>
        <div className="ml-auto flex items-center gap-3">
          <LanguageSelector />
          <ThemeToggle />
          <AuthNav />
        </div>
      </header>

      <div className="flex flex-1">
        {/* Navy sidebar */}
        <aside
          className={`flex shrink-0 flex-col bg-[#111a3c] py-4 text-slate-300 transition-[width] duration-200 dark:bg-[#0c1430] ${
            collapsed ? "w-16" : "w-60"
          }`}
        >
          <nav className="flex-1 space-y-1 px-2">
            {tabs.map((t) => {
              const active = pathname.startsWith(t.href);
              const Glyph = Icons[t.icon];
              return (
                <Link
                  key={t.href}
                  href={t.href}
                  title={collapsed ? t.label : undefined}
                  className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                    collapsed ? "justify-center" : ""
                  } ${
                    active
                      ? "bg-white/10 text-white"
                      : "text-slate-300 hover:bg-white/5 hover:text-white"
                  }`}
                >
                  <Glyph className="h-5 w-5 shrink-0" />
                  {!collapsed && <span className="truncate">{t.label}</span>}
                </Link>
              );
            })}
          </nav>

          <button
            type="button"
            onClick={() => setCollapsed((c) => !c)}
            aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
            className={`mx-2 mt-2 flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-400 transition-colors hover:bg-white/5 hover:text-white ${
              collapsed ? "justify-center" : ""
            }`}
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={2}
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
              className={`h-4 w-4 transition-transform duration-200 ${collapsed ? "rotate-180" : ""}`}
            >
              <path d="m11 17-5-5 5-5M18 17l-5-5 5-5" />
            </svg>
            {!collapsed && <span>Collapse</span>}
          </button>
        </aside>

        {/* Content */}
        <main className="min-w-0 flex-1 overflow-x-auto px-6 py-8 sm:px-8">
          {children}
        </main>
      </div>
    </div>
  );
}
