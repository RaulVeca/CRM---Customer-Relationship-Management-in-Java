"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import ThemeToggle from "@/components/ThemeToggle";
import AuthNav from "@/components/AuthNav";
import AuthGuard from "@/components/AuthGuard";
import PrimaryNav from "@/components/PrimaryNav";
import Logo from "@/components/Logo";
import LanguageSelector from "@/components/LanguageSelector";

/**
 * Site chrome switch. Everything outside the admin area keeps the classic
 * TrainingIT shell — sticky top nav, centred content column and footer. The
 * admin portal, however, is a self-contained console with its own full-bleed
 * sidebar layout (see {@code app/admin/layout.tsx}), so on {@code /admin} routes
 * this component steps aside and renders the admin tree edge-to-edge. The global
 * access guard still wraps both branches so the redirect behaviour is unchanged.
 */
export default function SiteShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isAdmin = pathname === "/admin" || pathname.startsWith("/admin/");

  if (isAdmin) {
    return <AuthGuard>{children}</AuthGuard>;
  }

  return (
    <>
      <header className="sticky top-0 z-40 border-b border-black/5 bg-canvas/80 backdrop-blur-md transition-colors dark:border-white/10 dark:bg-zinc-950/80">
        <nav className="mx-auto flex max-w-6xl items-center gap-6 px-6 py-3.5">
          <Link
            href="/"
            className="flex items-center text-ink transition-colors dark:text-white"
            aria-label="TrainingIT home"
          >
            <Logo size={30} />
          </Link>
          <PrimaryNav />
          <div className="ml-auto flex items-center gap-3">
            <LanguageSelector />
            <ThemeToggle />
            <AuthNav />
          </div>
        </nav>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-10">
        <AuthGuard>{children}</AuthGuard>
      </main>
      <footer className="border-t border-black/5 bg-white text-zinc-600 transition-colors dark:border-white/10 dark:bg-zinc-950 dark:text-zinc-400">
        <div className="mx-auto grid max-w-6xl gap-8 px-6 py-12 sm:grid-cols-2 lg:grid-cols-4">
          <div className="sm:col-span-2 lg:col-span-2">
            <Logo size={30} className="text-ink dark:text-white" />
            <p className="mt-4 max-w-sm text-sm leading-relaxed text-zinc-600 dark:text-zinc-400">
              Professional IT training for individuals and corporate teams —
              instructor-led courses, hands-on labs and 1:1 mentoring that turn
              skills into careers.
            </p>
          </div>
          <div>
            <p className="text-sm font-semibold text-ink dark:text-white">Platform</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li><Link href="/" className="hover:text-ink dark:hover:text-white">Courses</Link></li>
              <li><Link href="/my-courses" className="hover:text-ink dark:hover:text-white">My courses</Link></li>
              <li><Link href="/my-sessions" className="hover:text-ink dark:hover:text-white">My sessions</Link></li>
            </ul>
          </div>
          <div>
            <p className="text-sm font-semibold text-ink dark:text-white">Account</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li><Link href="/login" className="hover:text-ink dark:hover:text-white">Log in</Link></li>
              <li><Link href="/register" className="hover:text-ink dark:hover:text-white">Create account</Link></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-black/5 dark:border-white/10">
          <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-2 px-6 py-5 text-xs text-zinc-500 sm:flex-row">
            <p>© {new Date().getFullYear()} TrainingIT. All rights reserved.</p>
            <p>Level up your IT career.</p>
          </div>
        </div>
      </footer>
    </>
  );
}
